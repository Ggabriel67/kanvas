export interface Notification {
	notificationId: number;
	userId: number;
	type: string;
	status: string;
	sentAt: string;
	payload: Record<string, string>;
}

export interface ReadNotificationsRequest {
	ids: number[];
}