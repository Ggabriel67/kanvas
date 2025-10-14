export type BoardMessage =
	| { type: "BOARD_UPDATED"; payload: BoardUpdatedMessage }
	| { type: "MEMBER_JOINED"; payload: MemberJoinedMessage }
	// TODO MEMBER_REMOVED
  | { type: "COLUMN_CREATED"; payload: ColumnCreatedMessage }
  | { type: "COLUMN_UPDATED"; payload: ColumnUpdatedMessage }
  | { type: "COLUMN_MOVED"; payload: ColumnMovedMessage }
	// todo COLUMN_DELETED
	| { type: "TASK_CREATED"; payload: TaskCreatedMessage }
	// todo TASK_UPDATED
	| { type: "TASK_MOVED"; payload: TaskMovedMessage };
	// todo TASK_DELETED
	// TASK_ASSIGNED
	// TASK_UNASSIGNED

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

export interface TaskCreatedMessage {
	columnId: number;
	taskId: number;
	orderIndex: number;
	title: string;
}

export interface TaskMovedMessage {
	beforeColumnId: number;
	targetColumnId: number;
	taskId: number;
	newOrderIndex: number;
}
