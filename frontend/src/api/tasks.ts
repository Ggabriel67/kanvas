import axios from "axios";
import type { AssignmentRequest, MoveTaskRequest, TaskDto, TaskRequest, TaskResponse, TaskUpdateRequest } from "../types/tasks";

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

export async function getTask(taskId: number, boardId: number) {
  try {
    const response = await api.get<TaskDto>(`/${taskId}`, {headers: {"X-Board-Id": boardId}});
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function deleteTask(taskId: number, boardId: number) {
  try {
      await api.delete(`/${taskId}`, {headers: {"X-Board-Id": boardId}});
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function updateTask(request: TaskUpdateRequest, boardId: number) {
  try {
    const respone = await api.patch<TaskResponse>("", request, {headers: {"X-Board-Id": boardId}});
    return respone.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function assignTask(request: AssignmentRequest, boardId: number) {
  try {
    await api.patch("/assignees", request, {headers: {"X-Board-Id": boardId}});
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function unassignTask(request: AssignmentRequest, boardId: number) {
  try {
    await api.delete("/assignees", {data: request, headers: {"X-Board-Id": boardId}});
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
