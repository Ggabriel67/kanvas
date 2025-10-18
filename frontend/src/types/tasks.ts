export interface TaskRequest {
	columnId: number;
	title: string;
}

export interface TaskResponse {
	taskId: number;
	columnId: number;
	orderIndex: number;
	isExpired: boolean;
}

export interface MoveTaskRequest {
	targetColumnId: number;
	taskId: number;
	precedingTaskId: number | null;
	followingTaskId: number | null;
}

export interface TaskUpdateRequest {
  taskId: number;
  title?: string | null;
  description?: string | null;
  deadline?: Date | null;
  priority?: "HIGH" | "MEDIUM" | "LOW" | null;
  status?: "ACTIVE" | "DONE" | null;
}

export interface TaskProjection {
	taskId: number;
	orderIndex: number;
	columnId: number;
	title: string;
	deadline: string | null;
	status: "ACTIVE" | "DONE";
	priority: "HIGH" | "MEDIUM" | "LOW" | null;
	assigneeIds: number[] | null;
	isExpired: boolean;
}

export interface AssignmentRequest {
	taskId: number;
 	memberId: number;
 	userId: number;
 	assignerId: number;
	boardName: string;
}

export interface TaskDto {
	taskId: number;
	orderIndex: number;
	columnId: number;
	title: string;
	description: string | null;
	createdAt: string;
	deadline: string | null;
	status: "ACTIVE" | "DONE";
	priority: "HIGH" | "MEDIUM" | "LOW" | null;
	assigneeIds: number[];
	isExpired: boolean;
}
