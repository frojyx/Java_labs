export interface Client {
  id: number;
  firstName: string;
  lastName: string;
}

export interface Order {
  id: number;
  clientFirstName: string;
  clientLastName: string;
  dishNames: string[];
}

export interface Dish {
  id: number;
  name: string;
  category: string;
  ingredients: string[];
  price: number;
  weight: number;
}

export interface Category {
  id: number;
  name: string;
}

export interface Ingredient {
  id: number;
  name: string;
}

export interface DashboardSummary {
  clients: number;
  orders: number;
  dishes: number;
  categories: number;
  ingredients: number;
}

export interface SearchableOption {
  id: string;
  label: string;
}
