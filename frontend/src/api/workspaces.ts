import axios from "axios";
import type { WorkspaceDto, WorkspaceProjection, WorkspaceRequest } from "../types/workspace";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/workspaces",
  withCredentials: true,
})

export async function createWorkspace(request: WorkspaceRequest) {
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

export async function getWorkspace(workspaceId: number) {
  try {
    const response = await api.get<WorkspaceDto>(`/${workspaceId}`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function getAllUserWorkspaces(userId: number) {
  try {
    const response = await api.get<WorkspaceProjection[]>(`/${userId}/workspaces`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
