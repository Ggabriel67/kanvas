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
	precedingTaskId: number;
	followingTaskId: number;
}

export interface TaskUpdateRequest {
	taskId: number;
	title: string;
	description: string;
	deadline: string;
	priority: "HIGH" | "MEDIUM" | "LOW";
	status: "ACTIVE" | "DONE";
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
	description: string
	createdAt: string;
	deadline: string;
	status: "ACTIVE" | "DONE";
	priority: "HIGH" | "MEDIUM" | "LOW";
	assigneeIds: number[];
	isExpired: boolean;
}