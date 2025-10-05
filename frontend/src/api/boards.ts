import axios from "axios";
import type { BoardRequest } from "../types/boards";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/boards",
  withCredentials: true,
})

export async function createBoard(request: BoardRequest) {
  try {
    const response = await api.post<number>("", request)
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
