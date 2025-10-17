import React from 'react'
import type { BoardDto, BoardMember, BoardMemberRemoveRequest, BoardRoleChangeRequest } from '../types/boards';
import useAuth from '../hooks/useAuth';
import { IoMdClose } from "react-icons/io";
import toast from 'react-hot-toast';
import { changeBoardMemberRole, leaveBoard, removeBoardMember } from '../api/boards';
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';

interface BoardMembersTabProps {
  boardId: number;
  boardName: string;
  members: BoardMember[];
  currentRole: "ADMIN" | "EDITOR" | "VIEWER"; 
}

const canChangeRole = (currentRole: string, targetRole: string) => {
  if (targetRole === "ADMIN") return false;
  if (currentRole === "ADMIN") return true;
  if (currentRole === "ADMIN" && (targetRole === "EDITOR" || targetRole === "VIEWER")) return true;
  return false;
};

const canRemove = (currentRole: string, targetRole: string, isSelf: boolean) => {
  if (isSelf) return false;
  if (targetRole === "ADMIN") return false;
  if (currentRole === "ADMIN" && (targetRole === "EDITOR" || targetRole === "VIEWER")) return true;
  return false;
};

const BoardMembersTab: React.FC<BoardMembersTabProps> = ({ boardId, members, currentRole, boardName }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const handleRoleChange = async (memberId: number, newRole: string) => {
    let request: BoardRoleChangeRequest = {
      targetMemberId: memberId,
      boardId: boardId,
      newRole: newRole,
    }
    
    try {
      await changeBoardMemberRole(request);

      queryClient.setQueryData(['board', boardId], (old: BoardDto | undefined) => {
        if (!old) return old;
        return {
          ...old,
          boardMembers: old.boardMembers.map((m) =>
            m.memberId === memberId ? { ...m, boardRole: newRole } : m
          )
        };
      });

    } catch (error: any) {
			toast.error(error.message);
		}
  }

  const handleRemoveMember = async (memberId: number, targetUsername: string) => {
    let request: BoardMemberRemoveRequest = {
      targetMemberId: memberId,
      boardId: boardId,
    }

    try {
      await removeBoardMember(request);

      queryClient.setQueryData(['board', boardId], (old: BoardDto | undefined) => {
        if (!old) return old;
        return {
          ...old,
          boardMembers: old.boardMembers.filter((m) => m.memberId !== memberId),
        };
      });

      toast.success(`User ${targetUsername} has been removed from the board`);
    } catch (error: any) {
			toast.error(error.message);
		}
  }

  const handleLeaveBoard = async (memberId: number) => {
    let request: BoardMemberRemoveRequest = {
      targetMemberId: memberId,
      boardId: boardId
    }

    try {
      await leaveBoard(request);

      toast.success(`You left ${boardName} board`);      
      navigate("/app", { replace: true});
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  return (
    <div className="h-80">
      <h3 className="text-xl font-semibold mb-4">Board Members ({members.length})</h3>
      <ul className="space-y-2 max-h-full overflow-y-auto mb-10">
        {members
          .slice()
          .sort((a, b) => (a.userId === user?.id ? -1 : b.userId === user?.id ? 1 : 0))
          .map((m) => (
          <li
            key={m.memberId}
            className="flex items-center justify-between p-3 bg-[#2a2a2a] rounded"
          >
            {/* Avatar + details */}
            <div className="flex items-center space-x-3">
              <div
                className="w-10 h-10 rounded-full flex items-center justify-center font-bold text-white"
                style={{ backgroundColor: m.avatarColor }}
              >
                {m.firstname[0].toUpperCase()}
              </div>
              <div>
                <p className="font-semibold">
                  {m.firstname} {m.lastname}
                  {user?.id === m.userId ? " (You)" : ""}
                </p>
                <p className="text-sm text-gray-400">
                  {m.username} &#x2022; Joined: {new Date(m.joinedAt).toLocaleDateString()}
                </p>
              </div>
            </div>
      
            {/* Role + Remove */}
            <div className="flex items-center space-x-3">
              <div className="relative">
                {canChangeRole(currentRole, m.boardRole) ? (
                  <select
                    className="min-w-[130px] rounded bg-[#4a4a4a] text-white text-base px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-purple-500 cursor-pointer"
                    value={m.boardRole}
                    onChange={(e) => handleRoleChange(m.memberId, e.target.value)}
                  >
                    <option value="ADMIN">Administrator</option>
                    <option value="EDITOR">Editor</option>
                    <option value="VIEWER">Viewer</option>
                  </select>
                ) : (
                  <button disabled className="min-w-[130px] text-gray-300 rounded bg-[#3a3a3a] text-base px-3 py-1.5 text-center">
                    {m.boardRole === "ADMIN"
                      ? "Administrator"
                      : m.boardRole === "EDITOR"
                      ? "Editor"
                      : "Viewer"}
                  </button>
                )}
              </div>
      
              {user?.id === m.userId ? (
                <button
                  onClick={() => handleLeaveBoard(m.memberId)}
                  className="min-w-[175px] flex items-center justify-center space-x-1 px-3 py-1.5 text-base bg-[#4a4a4a] rounded hover:bg-red-700 cursor-pointer"
                >
                  <IoMdClose size={18}/>
                  <span>Leave board</span>
                </button>
              ) : (
                canRemove(currentRole, m.boardRole, m.userId === user?.id) && (
                  <button
                    className="min-w-[175px] flex items-center justify-center space-x-1 px-3 py-1.5 text-base bg-[#4a4a4a] rounded hover:bg-red-700  cursor-pointer"
                    onClick={() => handleRemoveMember(m.memberId, m.username)}
                  >
                    <IoMdClose size={18}/>
                    <span>Remove member</span>
                  </button>
                )
              )}
      
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}

export default BoardMembersTab;
