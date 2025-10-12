export type BoardMessage =
  | { type: "COLUMN_CREATED"; payload: ColumnCreatedMessage }
  | { type: "COLUMN_UPDATED"; payload: ColumnUpdatedMessage }
  | { type: "COLUMN_MOVED"; payload: ColumnMovedMessage };

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
