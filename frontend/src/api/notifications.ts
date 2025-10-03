import axios from "axios";
import type { Notification, ReadNotificationsRequest } from "../types/notifications";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/notifications",
  withCredentials: true,
})

export async function getNotifications(userId: number) {
  try {
    const response = await api.get<Notification[]>(`/${userId}`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function dismissNotification(notificationId: number) {
  try {
    await api.patch(`/${notificationId}/dismiss`);
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function updateNotificationsStatusToRead(request: ReadNotificationsRequest) {
  try {
    await api.patch("/status/read", request);
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function getUnreadNotificationsCount(userId: number) {
  try {
    const response = await api.get<number>(`${userId}/unread`);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
