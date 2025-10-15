import type { ColumnDto } from "./columns";

export interface BoardRequest {
  creatorId: number;
	workspaceId: number;
	name: string;
	description: string;
	visibility: string;
}

export interface BoardUpdateRequest {
	name: string | null;
	description: string | null;
	visibility: string | null;
}

export interface BoardDto {
	boardId: number;
	name: string;
	description: string;
	createdAt: string;
	visibility: "WORKSPACE_PUBLIC" | "PRIVATE";
	boardRole: "ADMIN" | "EDITOR" | "VIEWER";
	readonly: boolean;
	boardMembers: BoardMember[];
	columns: ColumnDto[];
}

export interface BoardMember {
	memberId: number;
	userId: number;
	firstname: string;
	lastname: string;
	username: string;
	avatarColor: string;
	boardRole: "ADMIN" | "EDITOR" | "VIEWER";
	joinedAt: string;
}

export interface BoardRoleChangeRequest {
	targetMemberId: number;
	boardId: number;
	newRole: string;
}

export interface BoardMemberRemoveRequest {
	targetMemberId: number;
	boardId: number;
}
