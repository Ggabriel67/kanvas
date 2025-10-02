import axios from "axios";
import type { User } from "../providers/AuthProvider";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/users",
  withCredentials: true,
})

export interface UserDto {
  id: number;
  firstname: string;
  lastname: string;
  email: string;
  username: string;
  avatarColor: string;
}

export async function getUser() {
  try {
    const response = await api.get<User>("/me");
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function searchUsers(query: string) {
  try {
    const response = await api.get<UserDto[]>(`/search/${query}`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
