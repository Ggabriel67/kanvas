import axios from "axios";
import type { User } from "../providers/AuthProvider";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/users",
  withCredentials: true,
})

export async function getUser() {
    try {
        const response = await api.get<User>("/me");
        return response.data;
    } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data.error);
    }
    throw error;
  }
}
