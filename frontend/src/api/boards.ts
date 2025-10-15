import axios from "axios";
import type { BoardDto, BoardRequest, BoardUpdateRequest } from "../types/boards";
import { useQuery } from "@tanstack/react-query"

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/boards",
  withCredentials: true,
})

export async function createBoard(request: BoardRequest) {
  try {
    const response = await api.post<number>("", request)
    return response.data;
  } catch (error: any) {  
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function updateBoard(boardId: number, request: BoardUpdateRequest) {
  try {
    await api.patch(`/${boardId}`, request)
  } catch (error: any) {  
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export async function deleteBoard(boardId: number) {
  try {
    await api.delete(`/${boardId}`);
  } catch (error: any) {  
    if (error.response) {
      throw new Error(error.response.data);
    }
    throw error;
  }
}

export const useBoardQuery = (boardId: number | null) => {
  return useQuery<BoardDto>({
    queryKey: ["board", boardId],
    queryFn: async () => {
      try {
        const response = await api.get<BoardDto>(`/${boardId}`);
        return response.data;
      } catch (error: any) {  
        if (error.response) {
          throw new Error(error.response.data);
        }
        throw error;
      }
    },
    enabled: !!boardId,
  });
}
