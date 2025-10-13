import React from 'react'
import type { ColumnDto } from '../types/columns';
import { Draggable, Droppable } from '@hello-pangea/dnd';
import { useQueryClient } from '@tanstack/react-query';
import type { BoardDto } from '../types/boards';
import type { TaskProjection } from '../types/tasks';

interface TaskContainerProps {
  column: ColumnDto;
	boardId: number;
	readonly: boolean;
}

const TaskContainer: React.FC<TaskContainerProps> = ({ column, boardId }) => {
	const queryClient = useQueryClient();

  const handleAddMockTask = () => {
    let newTask: TaskProjection = {
      taskId: Date.now(), // temporary ID
      title: `New Task ${column.taskProjections.length + 1}`,
      orderIndex:
        column.taskProjections.length > 0
          ? column.taskProjections[column.taskProjections.length - 1].orderIndex + 1000
          : 1000,
			isExpired: false,
			columnId: 0,
			deadline: null,
			status: "ACTIVE",
			priority: null,
			assigneeIds: []
    };

    queryClient.setQueryData<BoardDto | undefined>(["board", boardId], (old) => {
      if (!old) return old;

      return {
        ...old,
        columns: old.columns.map((col) =>
          col.columnId === column.columnId
            ? { ...col, taskProjections: [...col.taskProjections, newTask] }
            : col
        ),
      };
    });
  };

	// const sortedTasks = [...column.taskProjections].sort(
  //   (a, b) => a.orderIndex - b.orderIndex
  // );

	return (
		<Droppable droppableId={column.columnId.toString()} type="TASK">
      {(provided, snapshot) => (
        <div
          ref={provided.innerRef}
          {...provided.droppableProps}
          className={`flex flex-col gap-2 min-h-[50px] ${
            snapshot.isDraggingOver ? "bg-[#383838]" : ""
          }`}
        >
          {column.taskProjections.map((task, index) => (
            <Draggable
              key={task.taskId.toString()}
              draggableId={task.taskId.toString()}
              index={index}
            >
              {(provided, snapshot) => (
                <div
                  ref={provided.innerRef}
                  {...provided.draggableProps}
                  {...provided.dragHandleProps}
                  className={`bg-[#1f1f1f] rounded-lg p-2 shadow transition-all duration-200 text-gray-200 ${
                    snapshot.isDragging ? "opacity-60 scale-[0.98]" : ""
                  }`}
                >
                  <p className="text-sm font-medium">{task.title}</p>
                </div>
              )}
            </Draggable>
          ))}

          {provided.placeholder}

          <button
            onClick={handleAddMockTask}
            className="text-purple-400 hover:text-purple-300 text-sm mt-2"
          >
            + Add task
          </button>
        </div>
      )}
    </Droppable>
	)
}

export default TaskContainer
