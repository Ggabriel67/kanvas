export interface WorkspaceProjection {
  id: number;
  name: string;
  role: string;
};

export interface WorkspaceDto {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  workspaceRole: "OWNER" | "ADMIN" | "MEMBER";
  boardProjections: BoardProjection[];
};

export interface BoardProjection {
  boardId: number;
  name: string
};

export interface WorkspaceRequest {
  creatorId: number;
	name: string;
	description: string;
}

export interface WorkspaceMember {
  memberId: number;
  userId: number;
  firstname: string;
  lastname: string;
  username: string;
  avatarColor: string;
  role: "OWNER" | "ADMIN" | "MEMBER";
  joinedAt: string
}

export interface WorkspaceRoleChangeRequest {
  targetMemberId: number;
  workspaceId: number;
  newRole: string;
}


export interface WorkspaceMemberRemoveRequest {
  targetMemberId: number;
  workspaceId: number;
}

export interface GuestWorkspace {
  workspaceId: number;
  workspaceName: string;
  boardProjections: BoardProjection[];
}
