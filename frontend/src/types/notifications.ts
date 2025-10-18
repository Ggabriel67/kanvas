export interface Notification {
	notificationId: number;
	userId: number;
	type: "INVITATION" | "ASSIGNMENT" | "REMOVED_FROM_BOARD";
	status: string;
	sentAt: string;
	payload: Record<string, unknown>;
}

export interface ReadNotificationsRequest {
	ids: number[];
}