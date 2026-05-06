[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("GET", "DELETE")]
        [string]$Method,

        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    Invoke-RestMethod -Method $Method -Uri "$BaseUrl$Path" -ContentType "application/json; charset=utf-8"
}

Write-Host "Removing clients via API: $BaseUrl"

$clients = @(Invoke-Api -Method GET -Path "/api/clients")

if ($clients.Count -eq 0) {
    Write-Host "Clients: nothing to delete"
    exit 0
}

$deleted = 0

foreach ($client in ($clients | Sort-Object id -Descending)) {
    Invoke-Api -Method DELETE -Path "/api/clients/$($client.id)"
    $deleted++
}

Write-Host ("Clients: deleted {0}" -f $deleted)
