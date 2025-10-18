import React, { useEffect } from 'react'
import useWebSocket from './useWebSocket'
import { useQueryClient } from '@tanstack/react-query';
import type { BoardMessage } from '../types/websocketMessages';
import type { BoardDto } from '../types/boards';
import toast from 'react-hot-toast';
import { applyBoardUpdated, applyColumnCreated, applyColumnDeleted, applyColumnMoved, applyColumnUpdated, applyMemberJoined,
	 applyMemberRemoved, applyRoleChanged, applyTaskCreated, applyTaskDeleted, applyTaskMoved, applyTaskUpdated } from '../utils/boardUpdates';

const useBoardSocket = (boardId: number | null) => {
	const { client, connected } = useWebSocket();
	const queryClient = useQueryClient();

	useEffect(() => {
		if (!client || !boardId || !connected) return;

		const sub = client.subscribe(
			`/topic/board.${boardId}`,
			(message) => {
				try {
					const boardMessage: BoardMessage = JSON.parse(message.body);

					switch (boardMessage.type) {
						case "BOARD_UPDATED": 
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyBoardUpdated(old, boardMessage.payload);
							});
							break;
						case "MEMBER_JOINED": 
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyMemberJoined(old, boardMessage.payload);
							});
							break;
						case "MEMBER_REMOVED": 
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyMemberRemoved(old, boardMessage.payload);
							});
							break;
						case "ROLE_CHANGED": 
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyRoleChanged(old, boardMessage.payload);
							});
							break;
						case "COLUMN_CREATED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyColumnCreated(old, boardMessage.payload);
							});
							break;
						case "COLUMN_UPDATED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyColumnUpdated(old, boardMessage.payload);
							});
							break;
						case "COLUMN_MOVED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyColumnMoved(old, boardMessage.payload);
							});
							break;
						case "COLUMN_DELETED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyColumnDeleted(old, boardMessage.payload);
							});
							break;
						case "TASK_CREATED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyTaskCreated(old, boardMessage.payload);
							});
							break;
						case "TASK_MOVED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyTaskMoved(old, boardMessage.payload);
							});
							break;
						case "TASK_UPDATED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyTaskUpdated(old, boardMessage.payload);
							});
							break;
						case "TASK_DELETED":
							queryClient.setQueryData(["board", boardId], (old: BoardDto | undefined) => {
								if (!old) return old;
								return applyTaskDeleted(old, boardMessage.payload);
							});
							break;
					}
				} catch (error) {
          toast.error(`Failed to parse WS Board Message: ${error}`);
        }
			});
		return () => sub.unsubscribe();
	}, [client, connected, boardId])
}

export default useBoardSocket;
