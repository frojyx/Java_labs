import axios from "axios";

const baseURL = import.meta.env.VITE_API_BASE_URL || "/api";

export const api = axios.create({
  baseURL,
  timeout: 30000,
});

export function getApiErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message;
    return message || "The request could not be completed.";
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "Something went wrong.";
}
