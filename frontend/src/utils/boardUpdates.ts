import type { BoardDto, BoardMember } from "../types/boards";
import type { ColumnDto } from "../types/columns";
import type { BoardUpdatedMessage, ColumnCreatedMessage, ColumnDeletedMessage, ColumnMovedMessage, ColumnUpdatedMessage, MemberJoinedMessage, MemberRemovedMessage, RoleChangedMessage, TaskAssignment, TaskCreatedMessage, TaskDeletedMessage, TaskMovedMessage, TaskUpdatedMessage } from "../types/websocketMessages";
import type { TaskProjection } from "../types/tasks";

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
    joinedAt: payload.joinedAt
  };

  return {
    ...board,
    boardMembers: [...board.boardMembers, newMember],
  };
}

export function applyMemberRemoved(board: BoardDto, payload: MemberRemovedMessage) {
  const { memberId } = payload;

  return {
    ...board,
    boardMembers: board.boardMembers.filter((m) => m.memberId !== memberId),
  };
}

export function applyRoleChanged(board: BoardDto, payload: RoleChangedMessage) {
  const { memberId, role } = payload;

  return {
    ...board,
    boardMembers: board.boardMembers.map((m) =>
      m.memberId === memberId ? { ...m, boardRole: role } : m
    ),
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

export function applyColumnDeleted(board: BoardDto, payload: ColumnDeletedMessage) {
  const { columnId } = payload;

  return {
    ...board,
    columns: board.columns.filter((col) => col.columnId !== columnId),
  };
}

export function applyTaskCreated(board: BoardDto, payload: TaskCreatedMessage) {
  const { columnId, taskId, orderIndex, title } = payload;

  const newTask: TaskProjection = {
    taskId,
    orderIndex,
    columnId,
    title,
    deadline: null,
    status: "ACTIVE",
    priority: null,
    assigneeIds: null,
    isExpired: false,
  };

  return {
    ...board,
    columns: board.columns.map((col) => {
      if (col.columnId !== columnId) return col;

      const alreadyExists = col.taskProjections.some(
        (t) => t.taskId === taskId
      );
      if (alreadyExists) return col;

      const updatedTasks = [...col.taskProjections, newTask].sort(
        (a, b) => a.orderIndex - b.orderIndex
      );

      return {
        ...col,
        taskProjections: updatedTasks,
      };
    }),
  };
}

export function applyTaskMoved(board: BoardDto, payload: TaskMovedMessage) {
  const { beforeColumnId, targetColumnId, taskId, newOrderIndex } = payload;

  const sourceCol = board.columns.find((col) => col.columnId === beforeColumnId);
  const targetCol = board.columns.find((col) => col.columnId === targetColumnId);

  if (!sourceCol || !targetCol) return board;

  if (beforeColumnId === targetColumnId) {
    const updatedTasks = sourceCol.taskProjections.map((t) =>
      t.taskId === taskId ? { ...t, orderIndex: newOrderIndex } : t
    );

    const sortedTasks = [...updatedTasks].sort((a, b) => a.orderIndex - b.orderIndex);

    return {
      ...board,
      columns: board.columns.map((col) =>
        col.columnId === sourceCol.columnId
          ? { ...col, taskProjections: sortedTasks }
          : col
      ),
    };
  }

  const movedTask = sourceCol.taskProjections.find((t) => t.taskId === taskId);
  if (!movedTask) return board;

  const newTask: TaskProjection = {
    ...movedTask,
    columnId: targetColumnId,
    orderIndex: newOrderIndex,
  };

  const newSourceTasks = sourceCol.taskProjections.filter((t) => t.taskId !== taskId);

  const newTargetTasks = [...targetCol.taskProjections, newTask].sort(
    (a, b) => a.orderIndex - b.orderIndex
  );

  return {
    ...board,
    columns: board.columns.map((col) => {
      if (col.columnId === beforeColumnId)
        return { ...col, taskProjections: newSourceTasks };
      if (col.columnId === targetColumnId)
        return { ...col, taskProjections: newTargetTasks };
      return col;
    }),
  };
}

export function applyTaskUpdated(board: BoardDto, payload: TaskUpdatedMessage) {
  const { taskId, title, deadline, priority, taskStatus, isExpired } = payload;

  return {
    ...board,
    columns: board.columns.map((col) => ({
      ...col,
      taskProjections: col.taskProjections.map((task) => {
        if (task.taskId !== taskId) return task;

        return {
          ...task,
          ...(title !== null && { title }),
          ...(deadline !== null && { deadline }),
          ...(priority !== null && { priority }),
          ...(taskStatus !== null && { status: taskStatus }),
          isExpired,
        };
      }),
    })),
  };
}

export function applyTaskDeleted(board: BoardDto, payload: TaskDeletedMessage) {
  const { taskId } = payload;

  return {
    ...board,
    columns: board.columns.map((col) => ({
      ...col,
      taskProjections: col.taskProjections.filter((task) => task.taskId !== taskId),
    })),
  };
}

export function applyTaskAssigned(board: BoardDto, payload: TaskAssignment) {
  const { taskId, boardMemberId } = payload;

  return {
    ...board,
    columns: board.columns.map((col) => ({
      ...col,
      taskProjections: col.taskProjections.map((t) =>
        t.taskId === taskId
          ? {
              ...t,
              assigneeIds: t.assigneeIds
                ? t.assigneeIds.includes(boardMemberId)
                  ? t.assigneeIds
                  : [...t.assigneeIds, boardMemberId]
                : [boardMemberId],
            }
          : t
      ),
    })),
  };
}

export function applyTaskUnassigned(board: BoardDto, payload: TaskAssignment) {
  const { taskId, boardMemberId } = payload;

  return {
    ...board,
    columns: board.columns.map((col) => ({
      ...col,
      taskProjections: col.taskProjections.map((t) =>
        t.taskId === taskId
          ? {
              ...t,
              assigneeIds: (t.assigneeIds || []).filter(
                (id) => id !== boardMemberId
              ),
            }
          : t
      ),
    })),
  };
}
