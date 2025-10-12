import React, { useEffect } from 'react'
import useWebSocket from './useWebSocket'
import { useQueryClient } from '@tanstack/react-query';
import type { BoardMessage } from '../types/websocketMessages';
import type { BoardDto } from '../types/boards';
import toast from 'react-hot-toast';
import { applyColumnCreated, applyColumnMoved, applyColumnUpdated } from '../utils/boardUpdates';

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
					}
				} catch (error) {
          toast.error(`Failed to parse WS Board Message: ${error}`);
        }
			});
		return () => sub.unsubscribe();
	}, [client, connected, boardId])
}

export default useBoardSocket;
