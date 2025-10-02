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
