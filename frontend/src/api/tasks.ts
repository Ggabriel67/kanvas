import axios from "axios";
import type { MoveTaskRequest, TaskRequest, TaskResponse } from "../types/tasks";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/tasks",
  withCredentials: true,
})

export async function createTask(request: TaskRequest, boardId: number) {
  try {
      const response = await api.post<TaskResponse>("", request, {headers: {"X-Board-Id": boardId}});
      return response.data;
    } catch (error: any) {
      if (error.response) {
        throw new Error(error.response.data);
      }
      throw error;
    }
}

export async function moveTask(request: MoveTaskRequest, boardId: number) {
  try {
      const response = await api.patch<TaskResponse>("/move", request, {headers: {"X-Board-Id": boardId}});
      return response.data;
    } catch (error: any) {
      if (error.response) {
        throw new Error(error.response.data);
      }
      throw error;
    }
}
