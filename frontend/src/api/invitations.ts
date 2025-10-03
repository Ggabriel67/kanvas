import axios from "axios";
import type { WorkspaceInvitationRequest } from "../types/invitations";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/invitations",
  withCredentials: true,
})

export async function createWorkspaceInvitation(request: WorkspaceInvitationRequest) {
  try {
		await api.post("/workspaces", request);
	} catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
} 

export async function acceptWorkspaceInvitation(invitationId: number) {
  try {
		await api.post(`/workspaces/${invitationId}/accept`);
	} catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function declineWorkspaceInvitation(invitationId: number) {
  try {
		await api.patch(`/workspaces/${invitationId}/decline`);
	} catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

