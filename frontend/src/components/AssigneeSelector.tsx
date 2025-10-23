import React, { useEffect, useRef, useState } from 'react'
import type { BoardDto, BoardMember } from '../types/boards'
import { IoMdAdd } from "react-icons/io";
import useAuth from '../hooks/useAuth';
import type { AssignmentRequest, TaskDto } from '../types/tasks';
import toast from 'react-hot-toast';
import { assignTask, unassignTask } from '../api/tasks';
import { useQueryClient } from '@tanstack/react-query';

interface AssigneeSelectorProps {
  boardId: number;
  boardName: string
  taskId: number;
  boardMembers: BoardMember[];
  assigneeIds: number[];
  readonly: boolean;
  setTask: React.Dispatch<React.SetStateAction<TaskDto | undefined>>;
}

const AssigneeSelector: React.FC<AssigneeSelectorProps> = ({ boardId, boardName, taskId, boardMembers, assigneeIds, readonly, setTask }) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const { user } = useAuth();
  const queryClient = useQueryClient();

  const validMembers = boardMembers.filter(bm => bm.boardRole !== "VIEWER");
  const assignedMembers = validMembers.filter((m) => assigneeIds.includes(m.memberId));
  const unassignedMembers = validMembers.filter((m) => !assigneeIds.includes(m.memberId));

  const MAX_VISIBLE_MEMBERS = 5;
  const visibleMembers = assignedMembers.slice(0, MAX_VISIBLE_MEMBERS);
  const remainingCount = assignedMembers.length - MAX_VISIBLE_MEMBERS;

  const handleAssign = async (memberId: number, userId: number) => {
    if (!user) { return; }
    let request: AssignmentRequest = {
      taskId: taskId,
      memberId: memberId,
      userId: userId,
      assignerId: user?.id,
      boardName: boardName
    }

    try {
      await assignTask(request, boardId);

      setTask((prev) =>
        prev ? { ...prev, assigneeIds: [...prev.assigneeIds, memberId] } : prev
      );

      queryClient.setQueryData(["board", boardId], (oldData: BoardDto | undefined) => {
        if (!oldData) return oldData;

        return {
          ...oldData,
          columns: oldData.columns.map((col) => ({
            ...col,
            taskProjections: col.taskProjections.map((t) =>
              t.taskId === taskId
                ? { ...t, assigneeIds: [...(t.assigneeIds || []), memberId] }
                : t
            ),
          })),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const handleUnassign = async (memberId: number, userId: number) => {
    if (!user) { return; }
    let request: AssignmentRequest = {
      taskId: taskId,
      memberId: memberId,
      userId: userId,
      assignerId: user?.id,
      boardName: boardName
    }

    try {
      await unassignTask(request, boardId);

      setTask((prev) =>
        prev
          ? {
              ...prev,
              assigneeIds: prev.assigneeIds.filter((id) => id !== memberId),
            }
          : prev
      );
      queryClient.setQueryData(["board", boardId], (oldData: BoardDto | undefined) => {
        if (!oldData) return oldData;

        return {
          ...oldData,
          columns: oldData.columns.map((col) => ({
            ...col,
            taskProjections: col.taskProjections.map((t) =>
              t.taskId === taskId
                ? {
                    ...t,
                    assigneeIds: (t.assigneeIds || []).filter(
                      (id) => id !== memberId
                    ),
                  }
                : t
            ),
          })),
        };
      });
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);
  
  return (
    <div className="relative my-3" ref={dropdownRef}>
      <div className="flex items-center">
        {assignedMembers.length === 0 ? (
          <p className="text-gray-400 text-sm italic mr-2">No users assigned</p>
        ) : (
          <div className="flex -space-x-2">
            {visibleMembers.map((m) => (
              <div
                key={m.memberId}
                className="w-9 h-9 rounded-full border-2 border-[#151515] flex items-center justify-center text-white font-bold text-base cursor-default"
                style={{ backgroundColor: m.avatarColor }}
                title={`${m.firstname} ${m.lastname} (${m.username})`}
              >
                {m.firstname[0].toUpperCase()}
              </div>
            ))}
            {remainingCount > 0 && (
              <div className="w-9 h-9 rounded-full bg-[#2a2a2a] flex items-center justify-center text-gray-100 text-sm font-semibold cursor-default">
                +{remainingCount}
              </div>
            )}
          </div>
        )}

        {!readonly && (
          <button
            onClick={() => setIsOpen((prev) => !prev)}
            className="ml-1 w-9 h-9 rounded-full bg-[#2a2a2a] hover:bg-[#3a3a3a] flex items-center justify-center text-gray-200 cursor-pointer"
            title="Manage assignees"
          >
            <IoMdAdd size={20} />
          </button>
        )}
      </div>

      {/* Dropdown */}
      {isOpen && !readonly && (
        <div className="absolute right-0 mt-2 w-90 bg-[#151515] border border-gray-700 rounded-lg shadow-lg z-50">
          <div className="px-4 py-2 text-gray-200 font-semibold text-center">
            Manage Assignees
          </div>

          <ul className="max-h-60 overflow-y-auto text-gray-200">
            {assignedMembers.length > 0 && (
              <li className="px-3 py-3 text-xs uppercase text-gray-400 border-t border-gray-700">
                Assigned
              </li>
            )}
            {assignedMembers.map((m) => (
              <li
                key={m.memberId}
                className="flex items-center justify-between p-2 px-3 hover:bg-[#2b2b2b]"
              >
                <div className="flex items-center space-x-3">
                  <div
                    className="w-8 h-8 rounded-full flex items-center justify-center font-bold text-white text-base"
                    style={{ backgroundColor: m.avatarColor }}
                  >
                    {m.firstname[0].toUpperCase()}
                  </div>
                  <span>{`${m.firstname} ${m.lastname}`}</span>
                </div>
                <button
                  className="px-3 py-1 text-sm bg-gray-600 hover:bg-gray-500 rounded cursor-pointer"
                  onClick={() => handleUnassign(m.memberId, m.userId)}
                >
                  Unassign
                </button>
              </li>
            ))}

            {unassignedMembers.length > 0 && (
              <li className="px-3 py-3 text-xs uppercase text-gray-400 border-t border-gray-700">
                Available
              </li>
            )}
            {unassignedMembers.map((m) => (
              <li
                key={m.memberId}
                className="flex items-center justify-between p-2 px-3 hover:bg-[#2a2a2a]"
              >
                <div className="flex items-center space-x-3">
                  <div
                    className="w-8 h-8 rounded-full flex items-center justify-center font-bold text-white text-base"
                    style={{ backgroundColor: m.avatarColor }}
                  >
                    {m.firstname[0].toUpperCase()}
                  </div>
                  <span>{`${m.firstname} ${m.lastname}`}</span>
                </div>
                <button
                  className="px-3 py-1 text-sm bg-purple-600 hover:bg-purple-500 rounded cursor-pointer"
                  onClick={() => handleAssign(m.memberId, m.userId)}
                >
                  Assign
                </button>
              </li>
            ))}

            {assignedMembers.length === 0 && unassignedMembers.length === 0 && (
              <li className="p-3 text-gray-400 text-sm text-center">
                No available members
              </li>
            )}
          </ul>
        </div>
      )}
    </div>
  )
}

export default AssigneeSelector;
