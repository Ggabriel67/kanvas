export interface Notification {
	notificationId: number;
	userId: number;
	type: string;
	status: string;
	sentAt: string;
	payload: Record<string, unknown>;
}

export interface ReadNotificationsRequest {
	ids: number[];
}