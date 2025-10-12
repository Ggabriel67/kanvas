import type { BoardDto } from "../types/boards";
import type { ColumnDto } from "../types/columns";
import type { ColumnCreatedMessage, ColumnMovedMessage, ColumnUpdatedMessage } from "../types/websocketMessages";

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
