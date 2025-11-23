import React, { useEffect, useState } from 'react'
import type { BoardProjection } from '../types/workspaces';
import type { BoardMemberRemoveRequest, BoardRoleChangeRequest, Member } from '../types/boards';
import useAuth from '../hooks/useAuth';
import { getAllWorkspaceBoards } from '../api/workspaces';
import toast from 'react-hot-toast';
import { changeBoardMemberRole, deleteBoard, getBoardMembers, leaveBoard, removeBoardMember } from '../api/boards';
import { FaRegTrashAlt } from "react-icons/fa";
import { IoMdReturnLeft } from "react-icons/io";
import { PiUsersThree } from "react-icons/pi";
import { IoMdClose } from "react-icons/io";

interface BoardListTabProps {
  workspaceId: number
  workspaceRole: "OWNER" | "ADMIN" | "MEMBER";
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

const BoardListTab: React.FC<BoardListTabProps> = ({ workspaceId, workspaceRole }) => {
  const [boards, setBoards] = useState<BoardProjection[]>([]);
  const [members, setMembers] = useState<Member[]>([]);
  const [selectedBoard, setSelectedBoard] = useState<BoardProjection | null>(null);
  const [loadingMembers, setLoadingMembers] = useState(false);

  const { user } = useAuth();
  const isAdminOrOwner = ["ADMIN", "OWNER"].includes(workspaceRole);

  const fetchBoards = async () => {
    try {
      const data = await getAllWorkspaceBoards(workspaceId);
      setBoards(data);
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const fetchMembers = async (boardId: number) => {
    try {
      setLoadingMembers(true);
      const data = await getBoardMembers(boardId);
      setMembers(data);
    } catch (error: any) {
      toast.error(error.message);
    } finally {
      setLoadingMembers(false);
    }
  };

  useEffect(() => {
    fetchBoards();
  }, [boards])

  const handleDeleteBoard = async (boardId: number) => {
    try {
      await deleteBoard(boardId);
      toast.success("Board deleted");
      fetchBoards();
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const handleRoleChange = async (memberId: number, newRole: string, boardId: number) => {
    let request: BoardRoleChangeRequest = {
      targetMemberId: memberId,
      boardId: boardId,
      newRole: newRole,
    }
    
    try {
      await changeBoardMemberRole(request);
      fetchMembers(boardId);

    } catch (error: any) {
			toast.error(error.message);
		}
  }

  const handleRemoveMember = async (memberId: number, targetUsername: string, boardId: number) => {
    let request: BoardMemberRemoveRequest = {
      targetMemberId: memberId,
      boardId: boardId,
    }

    try {
      await removeBoardMember(request);
      fetchMembers(boardId);

      toast.success(`User ${targetUsername} has been removed from the board`);
    } catch (error: any) {
			toast.error(error.message);
		}
  }

  const handleLeaveBoard = async (memberId: number, boardId: number, boardName: string) => {
    let request: BoardMemberRemoveRequest = {
      targetMemberId: memberId,
      boardId: boardId
    }

    try {
      await leaveBoard(request);
      fetchMembers(boardId);

      toast.success(`You left ${boardName} board`);
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  return (
    <div className="mt-4">

      {/* Board list */}
      {selectedBoard === null && (
        <ul className="space-y-3">
          {boards.map((b) => (
            <li
              key={b.boardId}
              className="p-3 bg-[#2a2a2a] rounded flex justify-between items-center hover:bg-[#353535]"
            >
              <span className="text-lg font-semibold">{b.name}</span>
              <div className="flex flex-row space-x-3">
                <button
                  className="flex items-center space-x-2 bg-[#4a4a4a] hover:bg-[#454545] text-white px-4 py-2 rounded cursor-pointer"
                  onClick={() => {
                    setSelectedBoard(b);
                    fetchMembers(b.boardId);
                  }}
                >
                  <PiUsersThree size={22}/>
                  <span>Show members</span>
                </button>
                <button
                  className={isAdminOrOwner ? "flex items-center space-x-2 bg-[#4a4a4a] hover:bg-red-700 text-white px-4 py-2 rounded cursor-pointer"
                    : "flex items-center space-x-2 min-w-[130px] text-gray-300 rounded bg-[#3a3a3a] px-3 text-center"
                  }
                  disabled={!isAdminOrOwner}
                  onClick={() => handleDeleteBoard(b.boardId)}
                >
                  <FaRegTrashAlt size={18}/>
                  <span>Delete Board</span>
                </button>
              </div>
              
            </li>
          ))}
        </ul>
      )}

      {/* Members */}
      {selectedBoard !== null && (
        <div>
          <button
            className="text-gray-300 hover:text-white mb-4 flex items-center space-x-1 cursor-pointer"
            onClick={() => setSelectedBoard(null)}
          >
            <IoMdReturnLeft size={18}/>
            <span>Back to list</span>
          </button>

          <h2 className="text-xl font-semibold mb-4">
            Members of <span className="text-purple-400">{selectedBoard.name}</span>
          </h2>

          {loadingMembers ? (
            <div className="text-gray-400">Loading...</div>
          ) : (
            <ul className="space-y-2 max-h-80 overflow-y-auto">
              {members
                .slice()
                .sort((a, b) =>
                  a.userId === user?.id ? -1 : b.userId === user?.id ? 1 : 0
                )
                .map((m) => {
                  const currentMember = members.find((m) => m.userId === user?.id);
                  const currentMemberRole = currentMember?.boardRole || "";

                  return (
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
                            {m.username} &#x2022; Workspace {m.workspaceRole === null ? "Guest" : m.workspaceRole}
                          </p>
                        </div>
                      </div>

                      {/* Role + Remove */}
                      <div className="flex items-center space-x-3">
                        <div className="relative">
                          {(isAdminOrOwner || canChangeRole(currentMemberRole, m.boardRole)) ? (
                            <select
                              className="min-w-[130px] rounded bg-[#4a4a4a] text-white text-base px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-purple-500 cursor-pointer"
                              value={m.boardRole}
                              onChange={(e) => handleRoleChange(m.memberId, e.target.value, selectedBoard.boardId)}
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
                            onClick={() => handleLeaveBoard(m.memberId, selectedBoard.boardId, selectedBoard.name)}
                            className="min-w-[175px] flex items-center justify-center space-x-1 px-3 py-1.5 text-base bg-[#4a4a4a] rounded hover:bg-red-700 cursor-pointer"
                          >
                            <IoMdClose size={18}/>
                            <span>Leave board</span>
                          </button>
                        ) : (
                          (isAdminOrOwner || canRemove(currentMemberRole, m.boardRole, m.userId === user?.id)) && (
                            <button
                              className="min-w-[175px] flex items-center justify-center space-x-1 px-3 py-1.5 text-base bg-[#4a4a4a] rounded hover:bg-red-700  cursor-pointer"
                              onClick={() => handleRemoveMember(m.memberId, m.username, selectedBoard.boardId)}
                            >
                              <IoMdClose size={18}/>
                              <span>Remove member</span>
                            </button>
                          )
                        )}
                              
                      </div>
                    </li>
                  )
                }
              )}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}

export default BoardListTab;