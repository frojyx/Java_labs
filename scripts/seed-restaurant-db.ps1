[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function U {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Escaped
    )

    return (ConvertFrom-Json ('"' + $Escaped + '"'))
}

function Invoke-ApiJson {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("GET", "POST")]
        [string]$Method,

        [Parameter(Mandatory = $true)]
        [string]$Path,

        [object]$Body
    )

    $params = @{
        Method      = $Method
        Uri         = "$BaseUrl$Path"
        ContentType = "application/json; charset=utf-8"
    }

    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }

    Invoke-RestMethod @params
}

function Get-HttpStatusCode {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Exception
    )

    if ($Exception -is [System.Management.Automation.ErrorRecord]) {
        if ($Exception.Exception) {
            return Get-HttpStatusCode -Exception $Exception.Exception
        }
        return $null
    }

    if ($Exception -is [System.Exception] -and
        $Exception.PSObject.Properties.Name -contains "Response" -and
        $null -ne $Exception.Response) {
        return [int]$Exception.Response.StatusCode
    }

    if ($Exception.PSObject.Properties.Name -contains "Response" -and $null -ne $Exception.Response) {
        return [int]$Exception.Response.StatusCode
    }

    return $null
}

function Get-ExistingMap {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [scriptblock]$KeySelector
    )

    $items = @(Invoke-ApiJson -Method GET -Path $Path)
    $map = @{}

    foreach ($item in $items) {
        $key = & $KeySelector $item
        if (-not [string]::IsNullOrWhiteSpace($key)) {
            $map[$key] = $item
        }
    }

    return $map
}

function Ensure-Entities {
    param(
        [Parameter(Mandatory = $true)]
        [string]$EntityName,

        [Parameter(Mandatory = $true)]
        [string]$GetPath,

        [Parameter(Mandatory = $true)]
        [string]$PostPath,

        [Parameter(Mandatory = $true)]
        [object[]]$Items,

        [Parameter(Mandatory = $true)]
        [scriptblock]$KeySelector
    )

    $existing = Get-ExistingMap -Path $GetPath -KeySelector $KeySelector
    $created = 0
    $skipped = 0

    foreach ($item in $Items) {
        $key = & $KeySelector $item
        if ($existing.ContainsKey($key)) {
            $skipped++
            continue
        }

        try {
            $createdItem = Invoke-ApiJson -Method POST -Path $PostPath -Body $item
            $existing[(& $KeySelector $createdItem)] = $createdItem
            $created++
        } catch {
            $statusCode = Get-HttpStatusCode -Exception $_
            if ($statusCode -eq 409) {
                $existing[$key] = $item
                $skipped++
                continue
            }

            $payload = $item | ConvertTo-Json -Depth 10 -Compress
            throw ("Failed to create {0} via {1}. Payload: {2}. Error: {3}" -f $EntityName, $PostPath, $payload, $_.Exception.Message)
        }
    }

    Write-Host ("{0}: created {1}, skipped {2}" -f $EntityName, $created, $skipped)
}

function Get-OrderSignature {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Order
    )

    $dishes = @($Order.dishNames | ForEach-Object { "$_".Trim() } | Sort-Object)
    return ("{0}|{1}|{2}" -f
        "$($Order.clientFirstName)".Trim().ToLowerInvariant(),
        "$($Order.clientLastName)".Trim().ToLowerInvariant(),
        ($dishes -join ",").ToLowerInvariant())
}

function Ensure-Orders {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Orders
    )

    $existingOrders = @(Invoke-ApiJson -Method GET -Path "/api/orders")
    $signatures = @{}

    foreach ($order in $existingOrders) {
        $signatures[(Get-OrderSignature -Order $order)] = $true
    }

    $created = 0
    $skipped = 0

    foreach ($order in $Orders) {
        $signature = Get-OrderSignature -Order $order
        if ($signatures.ContainsKey($signature)) {
            $skipped++
            continue
        }

        try {
            Invoke-ApiJson -Method POST -Path "/api/orders" -Body $order | Out-Null
            $signatures[$signature] = $true
            $created++
        } catch {
            $statusCode = Get-HttpStatusCode -Exception $_
            if ($statusCode -eq 409) {
                $signatures[$signature] = $true
                $skipped++
                continue
            }

            $payload = $order | ConvertTo-Json -Depth 10 -Compress
            throw ("Failed to create order. Payload: {0}. Error: {1}" -f $payload, $_.Exception.Message)
        }
    }

    Write-Host ("Orders: created {0}, skipped {1}" -f $created, $skipped)
}

function Ensure-ReferenceItem {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [object]$Body
    )

    try {
        Invoke-ApiJson -Method POST -Path $Path -Body $Body | Out-Null
    } catch {
        $statusCode = Get-HttpStatusCode -Exception $_
        if ($statusCode -eq 409) {
            return
        }
        throw
    }
}

function Ensure-Dishes {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Dishes
    )

    $existing = Get-ExistingMap -Path "/api/dishAll" -KeySelector { param($item) "$($item.name)".Trim().ToLowerInvariant() }
    $created = 0
    $skipped = 0

    foreach ($dish in $Dishes) {
        $key = "$($dish.name)".Trim().ToLowerInvariant()
        if ($existing.ContainsKey($key)) {
            $skipped++
            continue
        }

        Ensure-ReferenceItem -Path "/api/categories" -Body @{ name = $dish.category }
        foreach ($ingredientName in $dish.ingredients) {
            Ensure-ReferenceItem -Path "/api/ingredients" -Body @{ name = $ingredientName }
        }

        try {
            $createdDish = Invoke-ApiJson -Method POST -Path "/api/dish" -Body $dish
            $existing["$($createdDish.name)".Trim().ToLowerInvariant()] = $createdDish
            $created++
        } catch {
            $statusCode = Get-HttpStatusCode -Exception $_
            if ($statusCode -eq 409) {
                $skipped++
                continue
            }

            $payload = $dish | ConvertTo-Json -Depth 10 -Compress
            throw ("Failed to create Dishes via /api/dish. Payload: {0}. Error: {1}" -f $payload, $_.Exception.Message)
        }
    }

    Write-Host ("Dishes: created {0}, skipped {1}" -f $created, $skipped)
}

$catSignature = U '\u0424\u0438\u0440\u043c\u0435\u043d\u043d\u043e\u0435'
$catChef = U '\u0412\u044b\u0431\u043e\u0440 \u0448\u0435\u0444\u0430'
$catReserve = U '\u0420\u0435\u0437\u0435\u0440\u0432'

$dishSalmon = U '\u041b\u043e\u0441\u043e\u0441\u044c \u0441 \u0446\u0438\u0442\u0440\u0443\u0441\u0430\u043c\u0438'
$dishRisotto = U '\u0420\u0438\u0437\u043e\u0442\u0442\u043e \u0441 \u0442\u0440\u044e\u0444\u0435\u043b\u0435\u043c \u0438 \u0433\u0440\u0438\u0431\u0430\u043c\u0438'
$dishLamb = U '\u0422\u043e\u043c\u043b\u0451\u043d\u0430\u044f \u0431\u0430\u0440\u0430\u043d\u0438\u043d\u0430'

$ingSalmon = U '\u041b\u043e\u0441\u043e\u0441\u044c'
$ingYuzu = U '\u042e\u0434\u0437\u0443'
$ingFennel = U '\u0424\u0435\u043d\u0445\u0435\u043b\u044c'
$ingHerbsRu = U '\u0422\u0440\u0430\u0432\u044b'
$ingArborio = U '\u0410\u0440\u0431\u043e\u0440\u0438\u043e'
$ingTruffle = U '\u0422\u0440\u044e\u0444\u0435\u043b\u044c'
$ingParmesanRu = U '\u041f\u0430\u0440\u043c\u0435\u0437\u0430\u043d'
$ingPorcini = U '\u0411\u0435\u043b\u044b\u0435 \u0433\u0440\u0438\u0431\u044b'
$ingLamb = U '\u0411\u0430\u0440\u0430\u043d\u0438\u043d\u0430'
$ingRosemaryRu = U '\u0420\u043e\u0437\u043c\u0430\u0440\u0438\u043d'
$ingDateGlaze = U '\u0424\u0438\u043d\u0438\u043a\u043e\u0432\u0430\u044f \u0433\u043b\u0430\u0437\u0443\u0440\u044c'
$ingCarrotRu = U '\u041c\u043e\u0440\u043a\u043e\u0432\u044c'

$categories = @(
    @{ name = $catSignature }
    @{ name = $catChef }
    @{ name = $catReserve }
    @{ name = "Soups" }
    @{ name = "Pasta" }
    @{ name = "Salads" }
    @{ name = "Desserts" }
    @{ name = "Grill" }
    @{ name = "Seafood" }
    @{ name = "Sides" }
    @{ name = "Breakfast" }
    @{ name = "Seasonal" }
)

$ingredients = @(
    @{ name = $ingSalmon }
    @{ name = $ingYuzu }
    @{ name = $ingFennel }
    @{ name = $ingHerbsRu }
    @{ name = $ingArborio }
    @{ name = $ingTruffle }
    @{ name = $ingParmesanRu }
    @{ name = $ingPorcini }
    @{ name = $ingLamb }
    @{ name = $ingRosemaryRu }
    @{ name = $ingDateGlaze }
    @{ name = $ingCarrotRu }
    @{ name = "Chicken" }
    @{ name = "Noodles" }
    @{ name = "Onion" }
    @{ name = "Cream" }
    @{ name = "Champignons" }
    @{ name = "Potato" }
    @{ name = "Beef" }
    @{ name = "Tomato" }
    @{ name = "Garlic" }
    @{ name = "Bacon" }
    @{ name = "Egg" }
    @{ name = "Shrimp" }
    @{ name = "Mussels" }
    @{ name = "Lemon" }
    @{ name = "Olive Oil" }
    @{ name = "Lettuce" }
    @{ name = "Cucumber" }
    @{ name = "Croutons" }
    @{ name = "Mozzarella" }
    @{ name = "Basil" }
    @{ name = "Beetroot" }
    @{ name = "Goat Cheese" }
    @{ name = "Walnut" }
    @{ name = "Chocolate" }
    @{ name = "Flour" }
    @{ name = "Sugar" }
    @{ name = "Butter" }
    @{ name = "Mascarpone" }
    @{ name = "Milk" }
    @{ name = "Strawberry" }
    @{ name = "Rice" }
    @{ name = "Duck" }
    @{ name = "Orange" }
    @{ name = "Veal" }
    @{ name = "Mashed Potato" }
    @{ name = "Asparagus" }
    @{ name = "Broccoli" }
    @{ name = "Bulgur" }
    @{ name = "Avocado" }
    @{ name = "Tuna" }
    @{ name = "Capers" }
    @{ name = "Paprika" }
    @{ name = "Quinoa" }
    @{ name = "Honey" }
    @{ name = "Pear" }
    @{ name = "Blue Cheese" }
    @{ name = "Vanilla" }
    @{ name = "Raspberry" }
    @{ name = "Spinach" }
    @{ name = "Feta" }
    @{ name = "Couscous" }
    @{ name = "Turkey" }
    @{ name = "Parmesan" }
    @{ name = "Rosemary" }
    @{ name = "Carrot" }
    @{ name = "Herbs" }
)

$dishes = @(
    @{
        name = $dishSalmon
        category = $catSignature
        price = 28.00
        weight = 320
        ingredients = @($ingSalmon, $ingYuzu, $ingFennel, $ingHerbsRu)
    }
    @{
        name = $dishRisotto
        category = $catChef
        price = 24.00
        weight = 360
        ingredients = @($ingArborio, $ingTruffle, $ingParmesanRu, $ingPorcini)
    }
    @{
        name = $dishLamb
        category = $catReserve
        price = 34.00
        weight = 410
        ingredients = @($ingLamb, $ingRosemaryRu, $ingDateGlaze, $ingCarrotRu)
    }
    @{
        name = "Chicken Noodle Soup"
        category = "Soups"
        price = 12.50
        weight = 350
        ingredients = @("Chicken", "Noodles", "Onion")
    }
    @{
        name = "Mushroom Cream Soup"
        category = "Soups"
        price = 13.90
        weight = 330
        ingredients = @("Champignons", "Cream", "Onion")
    }
    @{
        name = "Cheese Soup"
        category = "Soups"
        price = 14.80
        weight = 340
        ingredients = @("Cream", "Parmesan", "Potato")
    }
    @{
        name = "Carbonara"
        category = "Pasta"
        price = 18.50
        weight = 400
        ingredients = @("Bacon", "Parmesan", "Cream", "Egg")
    }
    @{
        name = "Bolognese"
        category = "Pasta"
        price = 19.20
        weight = 420
        ingredients = @("Beef", "Tomato", "Garlic", "Parmesan")
    }
    @{
        name = "Shrimp Pasta"
        category = "Pasta"
        price = 21.00
        weight = 390
        ingredients = @("Shrimp", "Cream", "Garlic", "Parmesan")
    }
    @{
        name = "Caesar Salad"
        category = "Salads"
        price = 15.00
        weight = 280
        ingredients = @("Chicken", "Lettuce", "Croutons", "Parmesan", "Tomato")
    }
    @{
        name = "Greek Salad"
        category = "Salads"
        price = 13.20
        weight = 260
        ingredients = @("Tomato", "Cucumber", "Feta", "Basil")
    }
    @{
        name = "Beetroot Goat Salad"
        category = "Salads"
        price = 14.60
        weight = 250
        ingredients = @("Beetroot", "Goat Cheese", "Walnut", "Lettuce")
    }
    @{
        name = "Chocolate Fondant"
        category = "Desserts"
        price = 9.50
        weight = 180
        ingredients = @("Chocolate", "Flour", "Sugar", "Egg", "Butter")
    }
    @{
        name = "Tiramisu"
        category = "Desserts"
        price = 10.20
        weight = 170
        ingredients = @("Mascarpone", "Sugar", "Egg", "Milk")
    }
    @{
        name = "Strawberry Mousse"
        category = "Desserts"
        price = 8.90
        weight = 160
        ingredients = @("Strawberry", "Cream", "Sugar", "Vanilla")
    }
    @{
        name = "Veal Steak"
        category = "Grill"
        price = 29.90
        weight = 380
        ingredients = @("Veal", "Rosemary", "Garlic", "Asparagus")
    }
    @{
        name = "Grilled Chicken with Bulgur"
        category = "Grill"
        price = 20.40
        weight = 410
        ingredients = @("Chicken", "Bulgur", "Paprika", "Broccoli")
    }
    @{
        name = "Duck Breast with Orange"
        category = "Grill"
        price = 27.60
        weight = 350
        ingredients = @("Duck", "Orange", "Honey", "Carrot")
    }
    @{
        name = "Lemon Shrimp"
        category = "Seafood"
        price = 23.50
        weight = 300
        ingredients = @("Shrimp", "Lemon", "Garlic", "Olive Oil")
    }
    @{
        name = "Mussels in Cream Sauce"
        category = "Seafood"
        price = 22.80
        weight = 320
        ingredients = @("Mussels", "Cream", "Garlic", "Herbs")
    }
    @{
        name = "Tuna Tartare"
        category = "Seafood"
        price = 24.70
        weight = 220
        ingredients = @("Tuna", "Avocado", "Capers", "Lemon")
    }
    @{
        name = "Mashed Potato"
        category = "Sides"
        price = 6.50
        weight = 200
        ingredients = @("Potato", "Butter", "Milk")
    }
    @{
        name = "Grilled Vegetables"
        category = "Sides"
        price = 7.20
        weight = 210
        ingredients = @("Broccoli", "Carrot", "Paprika", "Olive Oil")
    }
    @{
        name = "Herbed Quinoa"
        category = "Sides"
        price = 7.90
        weight = 190
        ingredients = @("Quinoa", "Herbs", "Olive Oil")
    }
    @{
        name = "Pear with Blue Cheese"
        category = $catChef
        price = 16.40
        weight = 230
        ingredients = @("Pear", "Blue Cheese", "Honey", "Walnut")
    }
    @{
        name = "Duck Risotto"
        category = "Seasonal"
        price = 25.30
        weight = 370
        ingredients = @("Rice", "Duck", "Parmesan", "Tomato")
    }
    @{
        name = "Salmon with Quinoa"
        category = "Seasonal"
        price = 30.10
        weight = 340
        ingredients = @("Salmon", "Quinoa", "Lemon", "Herbs")
    }
    @{
        name = "Spinach Omelette"
        category = "Breakfast"
        price = 11.40
        weight = 240
        ingredients = @("Egg", "Spinach", "Milk", "Butter")
    }
    @{
        name = "Turkey Couscous Bowl"
        category = "Breakfast"
        price = 14.10
        weight = 300
        ingredients = @("Turkey", "Couscous", "Tomato", "Lettuce")
    }
)

$clients = @(
    @{ firstName = "Ivan"; lastName = "Petrov" }
    @{ firstName = "Anna"; lastName = "Sidorova" }
    @{ firstName = "Maksim"; lastName = "Orlov" }
    @{ firstName = "Elena"; lastName = "Kovaleva" }
    @{ firstName = "Dmitry"; lastName = "Smirnov" }
    @{ firstName = "Olga"; lastName = "Melnik" }
    @{ firstName = "Artem"; lastName = "Vlasov" }
    @{ firstName = "Natalia"; lastName = "Egorova" }
    @{ firstName = "Sergey"; lastName = "Gromov" }
    @{ firstName = "Marina"; lastName = "Belova" }
    @{ firstName = "Alexey"; lastName = "Trofimov" }
    @{ firstName = "Yulia"; lastName = "Zaitseva" }
    @{ firstName = "Viktor"; lastName = "Lebedev" }
    @{ firstName = "Daria"; lastName = "Sokolova" }
    @{ firstName = "Pavel"; lastName = "Rudenko" }
    @{ firstName = "Irina"; lastName = "Frolova" }
    @{ firstName = "Georgy"; lastName = "Morozov" }
    @{ firstName = "Svetlana"; lastName = "Bykova" }
    @{ firstName = "Kirill"; lastName = "Nosov" }
    @{ firstName = "Tatiana"; lastName = "Anisimova" }
    @{ firstName = "Ruslan"; lastName = "Zhukov" }
    @{ firstName = "Alena"; lastName = "Kravtsova" }
    @{ firstName = "Vadim"; lastName = "Kiselev" }
    @{ firstName = "Lilia"; lastName = "Guseva" }
    @{ firstName = "Roman"; lastName = "Dyakov" }
    @{ firstName = "Nina"; lastName = "Maslova" }
    @{ firstName = "Oleg"; lastName = "Voronin" }
    @{ firstName = "Milana"; lastName = "Rybak" }
    @{ firstName = "Denis"; lastName = "Karpov" }
    @{ firstName = "Kristina"; lastName = "Borisova" }
)

$orders = @(
    @{ clientFirstName = "Ivan"; clientLastName = "Petrov"; dishNames = @("Carbonara", "Caesar Salad") }
    @{ clientFirstName = "Anna"; clientLastName = "Sidorova"; dishNames = @("Mushroom Cream Soup", "Tiramisu") }
    @{ clientFirstName = "Maksim"; clientLastName = "Orlov"; dishNames = @("Bolognese") }
    @{ clientFirstName = "Elena"; clientLastName = "Kovaleva"; dishNames = @("Greek Salad", "Chocolate Fondant") }
    @{ clientFirstName = "Dmitry"; clientLastName = "Smirnov"; dishNames = @("Cheese Soup", "Shrimp Pasta") }
    @{ clientFirstName = "Olga"; clientLastName = "Melnik"; dishNames = @("Beetroot Goat Salad", "Strawberry Mousse") }
    @{ clientFirstName = "Artem"; clientLastName = "Vlasov"; dishNames = @("Chicken Noodle Soup", "Carbonara") }
    @{ clientFirstName = "Natalia"; clientLastName = "Egorova"; dishNames = @("Greek Salad", "Tiramisu") }
    @{ clientFirstName = "Sergey"; clientLastName = "Gromov"; dishNames = @("Bolognese", "Caesar Salad", "Chocolate Fondant") }
    @{ clientFirstName = "Marina"; clientLastName = "Belova"; dishNames = @("Mushroom Cream Soup", "Tuna Tartare") }
    @{ clientFirstName = "Alexey"; clientLastName = "Trofimov"; dishNames = @($dishSalmon, "Herbed Quinoa") }
    @{ clientFirstName = "Yulia"; clientLastName = "Zaitseva"; dishNames = @($dishRisotto, "Pear with Blue Cheese") }
    @{ clientFirstName = "Viktor"; clientLastName = "Lebedev"; dishNames = @($dishLamb, "Mashed Potato") }
    @{ clientFirstName = "Daria"; clientLastName = "Sokolova"; dishNames = @("Duck Breast with Orange", "Grilled Vegetables") }
    @{ clientFirstName = "Pavel"; clientLastName = "Rudenko"; dishNames = @("Veal Steak", "Herbed Quinoa") }
    @{ clientFirstName = "Irina"; clientLastName = "Frolova"; dishNames = @("Lemon Shrimp", "Greek Salad") }
    @{ clientFirstName = "Georgy"; clientLastName = "Morozov"; dishNames = @("Mussels in Cream Sauce", "Chocolate Fondant") }
    @{ clientFirstName = "Svetlana"; clientLastName = "Bykova"; dishNames = @("Duck Risotto", "Strawberry Mousse") }
    @{ clientFirstName = "Kirill"; clientLastName = "Nosov"; dishNames = @("Salmon with Quinoa", "Caesar Salad") }
    @{ clientFirstName = "Tatiana"; clientLastName = "Anisimova"; dishNames = @("Grilled Chicken with Bulgur", "Tiramisu") }
    @{ clientFirstName = "Ruslan"; clientLastName = "Zhukov"; dishNames = @("Tuna Tartare", "Pear with Blue Cheese") }
    @{ clientFirstName = "Alena"; clientLastName = "Kravtsova"; dishNames = @($dishSalmon, "Beetroot Goat Salad") }
    @{ clientFirstName = "Vadim"; clientLastName = "Kiselev"; dishNames = @($dishLamb, "Grilled Vegetables", "Chocolate Fondant") }
    @{ clientFirstName = "Lilia"; clientLastName = "Guseva"; dishNames = @($dishRisotto, "Strawberry Mousse") }
    @{ clientFirstName = "Roman"; clientLastName = "Dyakov"; dishNames = @("Spinach Omelette", "Turkey Couscous Bowl") }
    @{ clientFirstName = "Nina"; clientLastName = "Maslova"; dishNames = @("Cheese Soup", "Duck Risotto") }
    @{ clientFirstName = "Oleg"; clientLastName = "Voronin"; dishNames = @("Veal Steak", "Mashed Potato") }
    @{ clientFirstName = "Milana"; clientLastName = "Rybak"; dishNames = @("Shrimp Pasta", "Greek Salad", "Tiramisu") }
    @{ clientFirstName = "Denis"; clientLastName = "Karpov"; dishNames = @("Mussels in Cream Sauce", "Herbed Quinoa") }
    @{ clientFirstName = "Kristina"; clientLastName = "Borisova"; dishNames = @("Duck Breast with Orange", "Pear with Blue Cheese") }
)

try {
    Invoke-ApiJson -Method GET -Path "/api/categories" | Out-Null
} catch {
    throw "Could not connect to API at $BaseUrl. Start the Spring Boot app first."
}

Write-Host ("Seeding database via API: {0}" -f $BaseUrl)

Ensure-Entities -EntityName "Categories" -GetPath "/api/categories" -PostPath "/api/categories" `
    -Items $categories -KeySelector { param($item) "$($item.name)".Trim().ToLowerInvariant() }

Ensure-Entities -EntityName "Ingredients" -GetPath "/api/ingredients" -PostPath "/api/ingredients" `
    -Items $ingredients -KeySelector { param($item) "$($item.name)".Trim().ToLowerInvariant() }

Ensure-Dishes -Dishes $dishes

Ensure-Entities -EntityName "Clients" -GetPath "/api/clients" -PostPath "/api/clients" `
    -Items $clients -KeySelector { param($item) "$($item.firstName) $($item.lastName)".Trim().ToLowerInvariant() }

Ensure-Orders -Orders $orders

Write-Host ""
Write-Host "Done."
Write-Host ("Category seed items: {0}" -f $categories.Count)
Write-Host ("Ingredient seed items: {0}" -f $ingredients.Count)
Write-Host ("Dish seed items: {0}" -f $dishes.Count)
Write-Host ("Client seed items: {0}" -f $clients.Count)
Write-Host ("Order seed items: {0}" -f $orders.Count)
