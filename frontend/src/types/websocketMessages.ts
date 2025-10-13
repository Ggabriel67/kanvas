export type BoardMessage =
	| { type: "BOARD_UPDATED"; payload: BoardUpdatedMessage }
	| { type: "MEMBER_JOINED"; payload: MemberJoinedMessage }
  | { type: "COLUMN_CREATED"; payload: ColumnCreatedMessage }
  | { type: "COLUMN_UPDATED"; payload: ColumnUpdatedMessage }
  | { type: "COLUMN_MOVED"; payload: ColumnMovedMessage };

export interface BoardUpdatedMessage {
	name: string | null;
	description: string | null;
	visibility: "WORKSPACE_PUBLIC" | "PRIVATE" | null;
}

export interface MemberJoinedMessage {
	memberId: number;
	userId: number;
	firstname: string;
	lastname: string;
	username: string;
	avatarColor: string;
	boardRole: "ADMIN" | "EDITOR" | "VIEWER";
}

export interface ColumnCreatedMessage {
	columnId: number;
	orderIndex: number;
	name: string;
}

export interface ColumnUpdatedMessage {
	columnId: number;
	columnName: string;
}

export interface ColumnMovedMessage {
	columnId: number;
	newOrderIndex: number;
}
