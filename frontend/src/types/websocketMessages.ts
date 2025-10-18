export type BoardMessage =
	| { type: "BOARD_UPDATED"; payload: BoardUpdatedMessage }
	| { type: "MEMBER_JOINED"; payload: MemberJoinedMessage }
	| { type: "MEMBER_REMOVED"; payload: MemberRemovedMessage }
	| { type: "ROLE_CHANGED"; payload: RoleChangedMessage }
  | { type: "COLUMN_CREATED"; payload: ColumnCreatedMessage }
  | { type: "COLUMN_UPDATED"; payload: ColumnUpdatedMessage }
  | { type: "COLUMN_MOVED"; payload: ColumnMovedMessage }
	| { type: "COLUMN_DELETED"; payload: ColumnDeletedMessage }
	| { type: "TASK_CREATED"; payload: TaskCreatedMessage }
	| { type: "TASK_MOVED"; payload: TaskMovedMessage }
	| { type: "TASK_UPDATED"; payload: TaskUpdatedMessage }
	| { type: "TASK_DELETED"; payload: TaskDeletedMessage };
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
	joinedAt: string;
}

export interface RoleChangedMessage {
	memberId: number;
	role: "ADMIN" | "EDITOR" | "VIEWER";
}

export interface MemberRemovedMessage {
	memberId: number;
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

export interface ColumnDeletedMessage {
	columnId: number;
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

export interface TaskUpdatedMessage {
	taskId: number;
	title: string | null;
	deadline: string | null;
	priority: "HIGH" | "MEDIUM" | "LOW" | null;
	taskStatus: "ACTIVE" | "DONE" | null;
	isExpired: boolean;
}

export interface TaskDeletedMessage {
	taskId: number;
}
