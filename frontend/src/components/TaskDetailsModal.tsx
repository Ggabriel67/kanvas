import React, { useEffect, useState } from 'react'
import type { BoardDto, BoardMember } from '../types/boards';
import { IoMdClose } from "react-icons/io";
import toast from 'react-hot-toast';
import type { TaskDto, TaskUpdateRequest } from '../types/tasks';
import { deleteTask, getTask, updateTask } from '../api/tasks';
import { FaRegTrashAlt } from "react-icons/fa";
import { FaRegCalendarTimes } from "react-icons/fa";
import { PiUsers } from "react-icons/pi";
import { MdOutlinePriorityHigh } from "react-icons/md";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { MdOutlineDescription } from "react-icons/md";
import PriorityDropdown from './PriorityDropdown';
import { useQueryClient } from '@tanstack/react-query';

interface TaskDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  boardId: number;
  taskId: number;
  boardMembers: BoardMember[];
  readonly: boolean;
}

const TaskDetailsModal: React.FC<TaskDetailsModalProps> = ({ isOpen, onClose, boardId, taskId, boardMembers, readonly }) => {
  if (!isOpen) { return null; }

  const [task, setTask] = useState<TaskDto>();
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [editedTitle, setEditedTitle] = useState("");
  const [isEditingDescription, setIsEditingDescription] = useState(false);
  const [editedDescription, setEditedDescription] = useState("");
  const [selectedDeadline, setSelectedDeadline] = useState<Date | null>(
    task?.deadline ? new Date(task.deadline) : null
  );

  const queryClient = useQueryClient();

  const fetchTaskDetails = async () => {
    try {
      const data = await getTask(taskId, boardId);
      setTask(data);
      if (data.deadline) setSelectedDeadline(new Date(data?.deadline));
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  useEffect(() => {
    fetchTaskDetails();
  }, [])

  const handleSaveTitle = async () => {
    if (!task || readonly) return;
    setIsEditingTitle(false);

    if (editedTitle.trim() === task.title || editedTitle.trim() === "") return;
    let request: TaskUpdateRequest = {
      taskId: taskId, title: editedTitle, description: null, deadline: null, priority: null, status: null
    }
    try {
      const res = await updateTask(request, boardId);
      setTask((prev) => prev && { ...prev, title: editedTitle });
      queryClient.setQueryData<BoardDto>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) =>
            col.columnId === task.columnId
              ? {
                  ...col,
                  taskProjections: col.taskProjections.map((t) =>
                    t.taskId === task.taskId ? { ...t, title: editedTitle } : t
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

  const handleSaveDescription = async () => {
    if (!task || readonly) return;
    if ((editedDescription || null) === task.description) return;

    let request: TaskUpdateRequest = {
      taskId: taskId, title: null, description: editedDescription, deadline: null, priority: null, status: null
    }
    try {
      const res = await updateTask(request, boardId);

      setTask((prev) => prev && { ...prev, description: editedDescription });
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const handleToggleStatus = async () => {
    if (!task || readonly) return;
    const newStatus = task.status === "ACTIVE" ? "DONE" : "ACTIVE";

    let request: TaskUpdateRequest = {
      taskId: taskId, title: null, description: null, deadline: null, priority: null, status: newStatus
    }
    try {
      const data = await updateTask(request, boardId);
      setTask((prev) => prev && { ...prev, status: newStatus });
      queryClient.setQueryData<BoardDto>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) =>
            col.columnId === task.columnId
              ? {
                  ...col,
                  taskProjections: col.taskProjections.map((t) =>
                    t.taskId === task.taskId ? { ...t, status: newStatus } : t
                  ),
                }
              : col
          ),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const handleSaveDeadline = async (date: Date | null) => {
    if (!task || readonly) return;
    try {
      const isoString = date ? date.toISOString() : null;
      let request: TaskUpdateRequest = {
        taskId: taskId, title: null, description: null, deadline: date, priority: null, status: null
      }
      const data = await updateTask(request, boardId);
      setTask((prev) => (prev ? { ...prev, deadline: isoString } : prev));
      setSelectedDeadline(date);
      
      queryClient.setQueryData<BoardDto>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) =>
            col.columnId === task.columnId
              ? {
                  ...col,
                  taskProjections: col.taskProjections.map((t) =>
                    t.taskId === task.taskId ? { ...t, deadline: isoString } : t
                  ),
                }
              : col
          ),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const handleSavePriority = async (newPriority: "HIGH" | "MEDIUM" | "LOW" | null) => {
    if (!task || readonly) return;
    let request: TaskUpdateRequest = {
      taskId: taskId, title: null, description: null, deadline: null, priority: newPriority, status: null
    }
    try {
      const data = await updateTask(request, boardId);
      setTask((prev) => prev ? { ...prev, priority: newPriority } : prev);

      queryClient.setQueryData<BoardDto>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) =>
            col.columnId === task.columnId
              ? {
                  ...col,
                  taskProjections: col.taskProjections.map((t) =>
                    t.taskId === task.taskId ? { ...t, priority: newPriority } : t
                  ),
                }
              : col
          ),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const handleDelete = async () => {
    if (!task) return;
    try {
      await deleteTask(task.taskId, boardId);
      toast.success("Task deleted");
      onClose();
      queryClient.setQueryData<BoardDto>(["board", boardId], (old) => {
        if (!old) return old;
        return {
          ...old,
          columns: old.columns.map((col) =>
            col.columnId === task.columnId
              ? {
                  ...col,
                  taskProjections: col.taskProjections.filter((t) =>
                    t.taskId !== task.taskId),
                }
              : col
          ),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  if (!task) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center">
        <div className="fixed inset-0 bg-black/75" onClick={onClose} />
        <div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl w-full max-w-2xl p-6 shadow-xl z-10">
          <p>Loading task...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div className="fixed inset-0 bg-black/75" onClick={onClose} />

      {/* Modal content */}
      <div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl min-h-[750px] max-h-[750px] overflow-y-auto shadow-xl w-full max-w-3xl p-6 z-10 pb-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-xl font-semibold">Task Details</h2>
          <IoMdClose
            className="cursor-pointer hover:text-gray-400"
            size={25}
            onClick={onClose}
          />
        </div>

        <div className="border-t border-gray-700 mb-6"></div>

        {/* Task title + status */}
        <div className="flex items-center gap-3 mb-3">
          <input
            type="checkbox"
            checked={task.status === "DONE"}
            onChange={handleToggleStatus}
            disabled={readonly}
            title="Mark task as done"
            className="w-5 h-5 text-green-600 bg-gray-100 border-gray-300 rounded-sm focus:ring-green-500 dark:focus:ring-green-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
          />
          {isEditingTitle && !readonly ? (
            <input
              type="text"
              value={editedTitle}
              onChange={(e) => setEditedTitle(e.target.value)}
              onBlur={handleSaveTitle}
              onKeyDown={(e) => {
                if (e.key === "Enter") handleSaveTitle();
                if (e.key === "Escape") {
                  setIsEditingTitle(false);
                  setEditedTitle(task.title);
                }
              }}
              autoFocus
              className="bg-[#2b2b2b] border border-gray-600 rounded px-2 py-1 w-full text-lg font-semibold outline-none focus:ring-2 focus:ring-purple-500"
            />
          ) : (
            <h3
              className={`text-2xl font-semibold ${
                readonly ? "cursor-default" : "cursor-pointer hover:underline"
              }`}
              title="Edit title"
              onClick={() => {
                !readonly && setIsEditingTitle(true)
                setEditedTitle(task.title)}
              }
            >
              {task.title}
            </h3>
          )}
        </div>

        {/* Created at */}
        <p className="text-sm text-gray-400 mb-8">
          Created at: {new Date(task.createdAt).toLocaleString()}
        </p>

        {/* Description */}
        <div className="mb-8">
          <div className="flex items-center space-x-1 mb-2">
            <MdOutlineDescription size={18}/>
            <span className="font-medium ">Description</span>
          </div>

          {isEditingDescription && !readonly ? (
            <textarea
              value={editedDescription}
              onChange={(e) => setEditedDescription(e.target.value)}
              onBlur={() => {
                handleSaveDescription();
                setIsEditingDescription(false);
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  handleSaveDescription();
                  setIsEditingDescription(false);
                }
                if (e.key === "Escape") {
                  setIsEditingDescription(false);
                  setEditedDescription(task.description || "");
                }
              }}
              rows={3}
              autoFocus
              className="w-full bg-[#2b2b2b] border border-gray-600 rounded p-2 resize-none outline-none focus:ring-2 focus:ring-purple-500 text-gray-200"
            />
          ) : (
            <p
              className={`text-base text-gray-300 ${
                readonly ? "cursor-default" : "cursor-pointer hover:underline"
                }`}
              title="Edit description"
              onClick={() => {
                !readonly && setIsEditingDescription(true)
                if (task.description) setEditedDescription(task.description)}
              }
            >
              {task.description || "* This task has no description"}
            </p>
          )}
        </div>

        {/* Task Meta Section */}
        <div className="mb-10 grid grid-cols-3 gap-5 items-start">
          <div>
            <div className="flex items-center space-x-1 mb-2">
              <MdOutlinePriorityHigh />
              <span className="font-medium ">Priority</span>
            </div>
            {/* Priority */}
            <PriorityDropdown
              value={task.priority}
              readonly={readonly}
              onChange={handleSavePriority}
            />
          </div>
          {/* Deadline */}
          <div>
            <div className="flex items-center space-x-2 mb-2">
              <FaRegCalendarTimes />
              <span className="font-medium ">Deadline</span>
            </div>
            {readonly ? (
              <p className="text-gray-300">
                {task.deadline
                  ? new Date(task.deadline).toLocaleString()
                  : "No deadline set"}
              </p>
            ) : (
              <DatePicker
                selected={selectedDeadline}
                onChange={(date) => handleSaveDeadline(date)}
                showTimeSelect
                dateFormat="Pp"
                placeholderText="No deadline set"
                className="w-full bg-[#2b2b2b] border border-gray-600 rounded p-2 outline-none focus:ring-2 focus:ring-purple-500 text-gray-200"
              />
            )}
          </div>

          {/* Assignees (placeholder) */}
          <div>
            <div className="flex space-x-2 items-center mb-2">
              <PiUsers size={20}/>
              <span className="font-medium">Assignees</span>
            </div>
            
            <div className="bg-[#2b2b2b] border border-gray-700 rounded p-3 text-gray-400">
              (Assignees)
            </div>
          </div>
        </div>

        {/* Delete button */}
        {!readonly && (
          <div className="flex justify-end">
            <button
              onClick={handleDelete}
              className="flex items-center gap-2 bg-[#4a4a4a] hover:bg-red-700 text-white px-4 py-2 rounded font-medium cursor-pointer"
            >
              <FaRegTrashAlt />
              <span>Delete Task</span>
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default TaskDetailsModal;
