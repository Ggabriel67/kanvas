export interface WorkspaceInvitationRequest {
    inviterId: number;
    inviteeId: number;
    workspaceId: number;
    role: string;
}
