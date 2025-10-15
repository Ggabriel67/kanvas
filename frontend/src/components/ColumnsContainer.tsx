import React, { useEffect, useState } from 'react'
import type { ColumnDto, ColumnRequest, MoveColumnRequest } from '../types/columns'
import type { BoardDto, BoardMember } from '../types/boards';
import { DragDropContext, Draggable, Droppable, type DropResult } from "@hello-pangea/dnd"
import { IoMdAdd } from "react-icons/io";
import toast from 'react-hot-toast';
import { createColumn, deleteColumn, moveColumn, updateColumnName } from '../api/columns';
import { useQueryClient } from '@tanstack/react-query';
import TaskContainer from './TaskContainer';
import type { MoveTaskRequest, TaskResponse } from '../types/tasks';
import { moveTask } from '../api/tasks';
import { IoMdClose } from "react-icons/io";

interface ColumnsContainerProps {
	columns: ColumnDto[];
	boardMembers: BoardMember[];
	boardId: number;
  readonly: boolean;
}

const reorder = (list: number[], startIndex: number, endIndex: number): number[] => {
  const result = Array.from(list);
  const [removed] = result.splice(startIndex, 1);
  result.splice(endIndex, 0, removed);
  return result;
};

const ColumnsContainer: React.FC<ColumnsContainerProps> = ({ columns: backendColumns, boardId, readonly }) => {
  const [columnsMap, setColumnsMap] = useState<Record<number, ColumnDto>>({});
  const [ordered, setOrdered] = useState<number[]>([]);

  const [isCreatingColumn, setIsCreatingColumn] = useState(false);
  const [newColumnName, setNewColumnName] = useState<string>("");

  const [editingColumnId, setEditingColumnId] = useState<number | null>(null);
  const [editedColumnName, setEditedColumnName] = useState<string>("");

  const queryClient = useQueryClient();
  
  useEffect(() => {
    const sorted = [...backendColumns].sort((a, b) => a.orderIndex - b.orderIndex);

    const map: Record<number, ColumnDto> = {};
    sorted.forEach((col) => {
      map[col.columnId] = col;
    });

    setColumnsMap(map);
    setOrdered(sorted.map((c) => c.columnId));
  }, [backendColumns]);

  const saveMoveColumn = async (request: MoveColumnRequest) => {
    try {
      const updatedColumn = await moveColumn(boardId, request);

      queryClient.setQueryData<BoardDto>(["board", boardId], (old) => {
        if (!old) return old;

        const updatedColumns = old.columns.map((col) =>
          col.columnId === updatedColumn.columnId
            ? { ...col, orderIndex: updatedColumn.orderIndex }
            : col
        );

        return { ...old, columns: updatedColumns };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const saveMoveTaskSameColumn = async (request: MoveTaskRequest, sourceColId: number) => {
    try {
      const response: TaskResponse = await moveTask(request, boardId);  

      queryClient.setQueryData<BoardDto | undefined>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) =>
            col.columnId === sourceColId
              ? {
                  ...col,
                  taskProjections: col.taskProjections.map((t) =>
                    t.taskId === response.taskId
                      ? { ...t, orderIndex: response.orderIndex }
                      : t
                  ),
                }
              : col
          ),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const saveMoveTaskDifferentColumn = async (request: MoveTaskRequest, sourceColId: number, destColId: number) => {
    try {
      const response: TaskResponse = await moveTask(request, boardId);

      queryClient.setQueryData<BoardDto | undefined>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) => {
            if (col.columnId === sourceColId)
              return {
                ...col,
                taskProjections: col.taskProjections.filter(
                  (t) => t.taskId !== response.taskId
                ),
              };
            if (col.columnId === destColId)
              return {
                ...col,
                taskProjections: col.taskProjections.map((t) =>
                  t.taskId === response.taskId
                    ? {
                        ...t,
                        columnId: response.columnId,
                        orderIndex: response.orderIndex,
                      }
                    : t
                ),
              };
            return col;
          }),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const onDragEnd = async (result: DropResult) => {
    const { destination, source, type } = result;
    if (!destination) return;
    
    if (type === "COLUMN") {
      if (destination.index === source.index) return;
      const newOrdered = reorder(ordered, source.index, destination.index);
      setOrdered(newOrdered);

      const movedId = newOrdered[destination.index];
      const precedingId = destination.index > 0 ? newOrdered[destination.index - 1] : null;
      const followingId = destination.index < newOrdered.length - 1 ? newOrdered[destination.index + 1] : null;

      const request: MoveColumnRequest = {
        columnId: movedId,
        precedingColumnId: precedingId,
        followingColumnId: followingId,
      };

      saveMoveColumn(request);
      return;
    }

    if (type === "TASK") {
      const sourceColId = parseInt(source.droppableId);
      const destColId = parseInt(destination.droppableId);

      if (sourceColId === destColId && source.index === destination.index) return;

      const sourceCol = columnsMap[sourceColId];
      const destCol = columnsMap[destColId];
      if (!sourceCol || !destCol) return;

      const newSourceTasks = Array.from(sourceCol.taskProjections);
      const [movedTask] = newSourceTasks.splice(source.index, 1);
      if (!movedTask) return;

      // --- move inside same column ---
      if (sourceColId === destColId) {
        newSourceTasks.splice(destination.index, 0, movedTask);
        const updatedCol = { ...sourceCol, taskProjections: newSourceTasks };
        setColumnsMap((prev) => ({ ...prev, [sourceColId]: updatedCol }));

        queryClient.setQueryData<BoardDto | undefined>(["board", boardId], (old) => {
          if (!old) return old;
          return {
            ...old,
            columns: old.columns.map((col) =>
              col.columnId === sourceColId
                ? { ...col, taskProjections: newSourceTasks }
                : col
            ),
          };
        });

        const precedingTask = destination.index > 0 ? newSourceTasks[destination.index - 1] : null;
        const followingTask = destination.index < newSourceTasks.length - 1 ? newSourceTasks[destination.index + 1] : null;

        const request: MoveTaskRequest = {
          targetColumnId: sourceColId,
          taskId: movedTask.taskId,
          precedingTaskId: precedingTask ? precedingTask.taskId : null,
          followingTaskId: followingTask ? followingTask.taskId : null,
        };

        saveMoveTaskSameColumn(request, sourceColId);
      }

      // --- move between columns ---
      else {
        const newDestTasks = Array.from(destCol.taskProjections);
        newDestTasks.splice(destination.index, 0, movedTask);

        const updatedColumns = {
          ...columnsMap,
          [sourceColId]: { ...sourceCol, taskProjections: newSourceTasks },
          [destColId]: { ...destCol, taskProjections: newDestTasks },
        };
        setColumnsMap(updatedColumns);

        queryClient.setQueryData<BoardDto | undefined>(["board", boardId], (old) => {
          if (!old) return old;
          return {
            ...old,
            columns: old.columns.map((col) => {
              if (col.columnId === sourceColId)
                return { ...col, taskProjections: newSourceTasks };
              if (col.columnId === destColId)
                return { ...col, taskProjections: newDestTasks };
              return col;
            }),
          };
        });

        const precedingTask = destination.index > 0 ? newDestTasks[destination.index - 1] : null;
        const followingTask = destination.index < newDestTasks.length - 1 ? newDestTasks[destination.index + 1] : null;

        const request: MoveTaskRequest = {
          targetColumnId: destColId,
          taskId: movedTask.taskId,
          precedingTaskId: precedingTask ? precedingTask.taskId : null,
          followingTaskId: followingTask ? followingTask.taskId : null,
        };

        saveMoveTaskDifferentColumn(request, sourceColId, destColId)
      }
    }
  };

  const handleCreateColumn = async () => {
    setIsCreatingColumn(false);
    if (newColumnName.trim() === "") return;
    let request: ColumnRequest = { 
      boardId: boardId,
      name: newColumnName,
    };
    try {
      const data = await createColumn(request);

      queryClient.setQueryData<BoardDto | undefined>(
        ["board", boardId],
        (old) => {
          if (!old) return old;

          const newColumn: ColumnDto = {
            columnId: data.columnId,
            orderIndex: data.orderIndex,
            name: data.name,
            taskProjections: [],
          };

          return {
            ...old,
            columns: [...old.columns, newColumn],
          };
        }
      );

      toast.success(`Column "${newColumnName}" created!`);
      setNewColumnName("");
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const handleCancelNewCol = () => {
    setIsCreatingColumn(false);
  };

  const handleSaveColumnName = async (columnId: number, oldName: string) => {
    setEditingColumnId(null);
    if ((editedColumnName.trim() === oldName) || (editedColumnName.trim() === "")) {
      return;
    }
    let request: ColumnRequest = { 
      boardId: boardId,
      name: editedColumnName,
    };
    try {
      await updateColumnName(columnId, request);

      queryClient.setQueryData<BoardDto | undefined>(
        ["board", boardId],
        (old) => {
          if (!old) return old;
          return {
            ...old,
            columns: old.columns.map((col) => 
              col.columnId === columnId ? {...col, name: editedColumnName.trim() } : col),
          };
        }
      );
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const handleDeleteColumn = async (columnId: number) => {
    const prevColumnsMap = columnsMap;
    const prevOrdered = ordered;

    setColumnsMap((prev) => {
      const newMap = { ...prev };
      delete newMap[columnId];
      return newMap;
    });

    setOrdered((prev) => prev.filter((id) => id !== columnId));

    try {
      await deleteColumn(columnId, boardId);
      toast.success("Column deleted");
    } catch (error: any) {
      toast.error(error.message);
      setColumnsMap(prevColumnsMap);
      setOrdered(prevOrdered);
    }
  }

  return (
    <div className="flex flex-col gap-4 h-full">
      <DragDropContext onDragEnd={onDragEnd}>
        <Droppable droppableId="board" direction="horizontal" type="COLUMN">
          {(provided) => (
            <div
              ref={provided.innerRef}
              {...provided.droppableProps}
              className="flex overflow-x-auto p-4 gap-4 scrollbar-thin scrollbar-thumb-[#444] scrollbar-track-[#222]"
            >
              {ordered.map((columnId, index) => {
                const column = columnsMap[columnId];
                if (!column) return null;

                return (
                  readonly ? (
                    <div
                      key={column.columnId}
                      className="bg-[#2b2b2b] rounded-xl p-2 w-[280px] flex-shrink-0 shadow-md text-gray-100"
                    >
                      <div className="flex items-center justify-between mb-2 pt-2 px-2">
                        <h3 className="font-semibold break-all whitespace-normal flex-1">
                          {column.name}
                        </h3>
                      </div>

                      <div className="border-t border-gray-500 mb-4"></div>

                      <TaskContainer column={column} boardId={boardId} readonly={readonly} />
                    </div>
                  ) : (
                    <Draggable
                      key={column.columnId.toString()}
                      draggableId={column.columnId.toString()}
                      index={index}
                    >
                      {(provided, snapshot) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          {...provided.dragHandleProps}
                          className={`bg-[#2b2b2b] rounded-xl p-2 min-w-[280px] max-w-[280px] flex-shrink-0
                            cursor-grab select-none shadow-md text-gray-100
                            ${
                              snapshot.isDragging
                                ? "opacity-50 scale-[0.98]"
                                : "hover:bg-[#333333]"
                            }
                          `}
                          style={{ ...provided.draggableProps.style, height: "fit-content" }}
                        >
                          <div className="flex items-center justify-between mb-2 pt-2 px-2">
                            {editingColumnId === column.columnId ? (
                              <input
                                type="text"
                                value={editedColumnName}
                                onChange={(e) => setEditedColumnName(e.target.value)}
                                onBlur={() => handleSaveColumnName(column.columnId, column.name)}
                                onKeyDown={(e) => {
                                  if (e.key === "Enter")
                                    handleSaveColumnName(column.columnId, column.name);
                                  if (e.key === "Escape") setEditingColumnId(null);
                                }}
                                autoFocus
                                className="bg-[#2b2b2b] border border-gray-600 text-gray-100 font-semibold px-2 rounded w-full outline-none focus:ring-2 focus:ring-purple-500"
                              />
                            ) : (
                              <h3
                                className="font-semibold break-all whitespace-normal flex-1 cursor-pointer"
                                onClick={() => {
                                  setEditingColumnId(column.columnId);
                                  setEditedColumnName(column.name);
                                }}
                              >
                                {column.name}
                              </h3>
                            )}

                            <button
                              onClick={() => handleDeleteColumn(column.columnId)}
                              className="ml-2 text-gray-400 hover:text-red-500 font-bold"
                              title="Delete column"
                            >
                              ×
                            </button>
                          </div>

                          <div className="border-t border-gray-500 mb-4"></div>

                          <TaskContainer column={column} boardId={boardId} readonly={readonly} />
                        </div>
                      )}
                    </Draggable>
                  )
                );
              })}

              {provided.placeholder}
              
              {!readonly && (
                <div className="min-w-[280px] flex-shrink-0">
                  {isCreatingColumn ? (
                    <div className="bg-[#1f1f1f] p-2 rounded-xl flex flex-col gap-2 shadow-md">
                      <input
                        type="text"
                        value={newColumnName}
                        onChange={(e) => setNewColumnName(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") handleCreateColumn();
                          if (e.key === "Escape") handleCancelNewCol();
                        }}
                        autoFocus
                        className="bg-[#2b2b2b] border border-gray-600 text-gray-100 px-2 py-1 rounded outline-none focus:ring-2 focus:ring-purple-500"
                        placeholder="Column name..."
                      />
                      <div className="flex gap-2">
                        <button
                          onClick={handleCancelNewCol}
                          className="flex-1 bg-gray-600 hover:bg-gray-500 text-white rounded py-1 cursor-pointer"
                        >
                          Cancel
                        </button>
                        <button
                          onClick={handleCreateColumn}
                          className="flex-1 bg-purple-600 hover:bg-purple-700 text-white rounded py-1 cursor-pointer"
                        >
                          Create
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      onClick={() => {
                        setIsCreatingColumn(true);
                        setNewColumnName("");
                      }}
                      className="min-h-[50px] cursor-pointer flex w-full items-center justify-center bg-[#1f1f1f] hover:bg-[#272727] border border-purple-500 text-purple-400 font-medium rounded-xl gap-1"
                    >
                      <IoMdAdd size={22} />
                      <span>New Column</span>
                    </button>
                  )}
                </div>
              )}
              
            </div>
          )}
        </Droppable>
      </DragDropContext>
    </div>
  );
};

export default ColumnsContainer;
