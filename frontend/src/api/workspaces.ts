import axios from "axios";
import type { WorkspaceDto, WorkspaceMember, WorkspaceProjection, WorkspaceRequest, WorkspaceRoleChangeRequest, WorkspaceMemberRemoveRequest, GuestWorkspace } from "../types/workspaces";

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

export async function getAllWorkspaceMembers(workspaceId: number) {
  try {
    const response = await api.get<WorkspaceMember[]>(`/${workspaceId}/members`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function changeWorkspaceMemberRole(request: WorkspaceRoleChangeRequest) {
  try {
    await api.patch("/members", request);
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function removeWorkspaceMember(request: WorkspaceMemberRemoveRequest) {
  try {
    await api.delete("/members", {data: request})
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function getGuestWorkspaces(userId: number) {
  try {
    const response = await api.get<GuestWorkspace[]>(`/guests/${userId}`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
