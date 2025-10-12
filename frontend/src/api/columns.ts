import axios from "axios";
import type { ColumnRequest, ColumnResponse } from "../types/columns";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/columns",
  withCredentials: true,
})

export async function createColumn(request: ColumnRequest) {
  try {
    const response = await api.post<ColumnResponse>("", request, {headers: {"X-Board-Id": request.boardId}})
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function updateColumnName(columnId: number, request: ColumnRequest) {
  try {
    await api.patch(`/${columnId}`, request, {headers: {"X-Board-Id": request.boardId}})
  } catch (error: any) {  
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}
