export interface WorkspaceInvitationRequest {
    inviterId: number;
    inviteeId: number;
    workspaceId: number;
    role: string;
}

export interface BoardInvitationRequest {
    inviterId: number;
    inviteeId: number;
    boardId: number;
    role: string;
}
