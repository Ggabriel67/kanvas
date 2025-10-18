import React, { useMemo, useState } from 'react'
import type { ColumnDto } from '../types/columns';
import { Draggable, Droppable } from '@hello-pangea/dnd';
import { useQueryClient } from '@tanstack/react-query';
import type { BoardDto, BoardMember } from '../types/boards';
import type { TaskProjection, TaskRequest } from '../types/tasks';
import { IoMdAdd } from "react-icons/io";
import toast from 'react-hot-toast';
import { createTask } from '../api/tasks';
import TaskDetailsModal from './TaskDetailsModal';
import { createPortal } from "react-dom"

interface TaskContainerProps {
  column: ColumnDto;
	boardId: number;
	readonly: boolean;
  boardMembers: BoardMember[];
}

const TaskContainer: React.FC<TaskContainerProps> = ({ column, boardId, readonly, boardMembers }) => {
  const [isCreatingTask, setIsCreatingTask] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState<string>("");
  const [isTaskModalOpen, setIsTaskModalOpen] = useState<boolean>(false);
  const [selectedTaskId, setSelectedTaskId] = useState<number>(0);

	const queryClient = useQueryClient();

  const handleTaskClick = (taskId: number) => {
    setSelectedTaskId(taskId);
    setIsTaskModalOpen(true);
  };

  const handleCreateTask = async () => {
    setIsCreatingTask(false);
    if (newTaskTitle.trim() === "") return;
    let request: TaskRequest  = {
      columnId: column.columnId,
      title: newTaskTitle
    }
    try {
      const data = await createTask(request, boardId);

      queryClient.setQueryData<BoardDto | undefined>(
        ["board", boardId],
        (old) => {
          if (!old) return old;
          
          const newTask: TaskProjection = {
            taskId: data.taskId,
            orderIndex: data.orderIndex,
            columnId: data.columnId,
            title: request.title,
            isExpired: data.isExpired,
            status: "ACTIVE", priority: null,
            assigneeIds: null, deadline: null
          }

          return {
            ...old,
            columns: old.columns.map((col) => 
              col.columnId === column.columnId 
                ? { ...col, taskProjections: [...col.taskProjections, newTask] }
                : col
            ),
          };
        }
      );

      toast.success("Task created");
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const handleCancelNewTask = () => {
    setIsCreatingTask(false);
    setNewTaskTitle("");
  };

	return (
    <div>
      <Droppable droppableId={column.columnId.toString()} type="TASK">
        {(provided, snapshot) => (
          <div
            ref={provided.innerRef}
            {...provided.droppableProps}
            className={`flex flex-col gap-2 ${
              snapshot.isDraggingOver ? "bg-[#383838] rounded-md" : ""
            }`}
          >
            {column.taskProjections.map((task, index) => (
              <div>
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
                      onClick={() => handleTaskClick(task.taskId)}
                      className={`bg-[#1f1f1f] border border-2 border-transparent rounded-lg p-2.5 shadow text-gray-200 hover:border-purple-500 ${
                        snapshot.isDragging ? "opacity-60 scale-[0.98]" : ""
                      }`}
                    >
                      <p className="text-sm break-all whitespace-normal font-medium">{task.title}</p>
                    </div>
                  )}
                </Draggable>
              </div>
            ))}

            {provided.placeholder}

            {!readonly && (
              <div>
                {isCreatingTask ? (
                  <div className="bg-[#212121] p-2 rounded-lg flex flex-col gap-2 shadow-md">
                    <input
                      type="text"
                      value={newTaskTitle}
                      onChange={(e) => setNewTaskTitle(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleCreateTask();
                        if (e.key === "Escape") handleCancelNewTask();
                      }}
                      autoFocus
                      className="bg-[#2b2b2b] border border-gray-600 text-gray-100 px-2 py-1 rounded outline-none focus:ring-2 focus:ring-purple-500"
                      placeholder="Task title..."
                    />
                    <div className="flex gap-2">
                      <button
                        onClick={handleCancelNewTask}
                        className="flex-1 bg-gray-600 hover:bg-gray-500 text-white rounded py-1 cursor-pointer"
                      >
                        Cancel
                      </button>
                      <button
                        onClick={handleCreateTask}
                        className="flex-1 bg-purple-600 hover:bg-purple-700 text-white rounded py-1 cursor-pointer"
                      >
                        Create
                      </button>
                    </div>
                  </div>
                ) : (
                  <button
                    onClick={() => {
                      setIsCreatingTask(true);
                      setNewTaskTitle("");
                    }}
                    className="text-purple-400 items-center flex space-x-1 w-full hover:text-purple-300 text-sm p-2 rounded-md cursor-pointer hover:bg-[#262626]"
                  >
                    <IoMdAdd size={20} />
                    <span>New Task</span>
                  </button>
                )}
              </div>
            )}
            
          </div>
        )}
      </Droppable>

      {isTaskModalOpen && createPortal(
        <TaskDetailsModal 
          isOpen={isTaskModalOpen}
          onClose={() => setIsTaskModalOpen(false)}
          boardId={boardId}
          taskId={selectedTaskId}
          boardMembers={boardMembers}
          readonly={readonly}
        />,
        document.body
      )}
    </div>
	)
}

export default TaskContainer;
