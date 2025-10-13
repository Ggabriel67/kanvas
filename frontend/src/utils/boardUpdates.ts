import toast from "react-hot-toast";
import type { BoardDto, BoardMember } from "../types/boards";
import type { ColumnDto } from "../types/columns";
import type { BoardUpdatedMessage, ColumnCreatedMessage, ColumnMovedMessage, ColumnUpdatedMessage, MemberJoinedMessage } from "../types/websocketMessages";

export function applyColumnCreated(board: BoardDto, payload: ColumnCreatedMessage) {
  const exists = board.columns.some(c => c.columnId === payload.columnId);
  if (exists) return board;

  const newColumn: ColumnDto = {
    columnId: payload.columnId,
    name: payload.name,
    orderIndex: payload.orderIndex,
    taskProjections: [],
  };

  const updatedColumns = [...board.columns, newColumn].sort(
    (a, b) => a.orderIndex - b.orderIndex
  );

  return {
    ...board,
    columns: updatedColumns,
  };
}

export function applyBoardUpdated(board: BoardDto, payload: BoardUpdatedMessage) {
  return {
    ...board, 
    name: payload.name !== null ? payload.name : board.name,
    description: payload.description !== null ? payload.description : board.description,
    visibility: payload.visibility !== null ? payload.visibility : board.visibility,
  };
}

export function applyMemberJoined(board: BoardDto, payload: MemberJoinedMessage) {
  const alreadyExists = board.boardMembers.some(m => m.memberId === payload.memberId);

  if (alreadyExists) {
    return board;
  }

  const newMember: BoardMember = {
    memberId: payload.memberId,
    userId: payload.userId,
    firstname: payload.firstname,
    lastname: payload.lastname,
    username: payload.username,
    avatarColor: payload.avatarColor,
    boardRole: payload.boardRole,
  };

  return {
    ...board,
    boardMembers: [...board.boardMembers, newMember],
  };
}

export function applyColumnUpdated(board: BoardDto, payload: ColumnUpdatedMessage) {
	const updatedColumns = board.columns.map(col =>
    col.columnId === payload.columnId
      ? { ...col, name: payload.columnName }
      : col
  );

  return {
    ...board,
    columns: updatedColumns,
  };
}

export function applyColumnMoved(board: BoardDto, payload: ColumnMovedMessage) {
	const updatedColumns = board.columns.map(col =>
    col.columnId === payload.columnId
      ? { ...col, orderIndex: payload.newOrderIndex }
      : col
  );

  updatedColumns.sort((a, b) => a.orderIndex - b.orderIndex);

  return {
    ...board,
    columns: updatedColumns,
  };
}
