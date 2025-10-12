import type { TaskProjection } from "./tasks";

export interface ColumnRequest {
	boardId: number;
	name: string
}

export interface ColumnResponse {
	columnId: number;
	name: string;
	orderIndex: number;
}

export interface MoveColumnRequest {
	columnId: number;
	precedingColumnId: number | null;
	followingColumnId: number | null;
}

export interface ColumnDto {
  columnId: number;
	orderIndex: number;
	name: string;
	taskProjections: TaskProjection[];
}
