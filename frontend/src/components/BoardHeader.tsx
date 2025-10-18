import React, { useState } from 'react'
import type { BoardDto, BoardUpdateRequest } from '../types/boards';
import { AiOutlineUserAdd } from "react-icons/ai";
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";
import { IoMdMore } from "react-icons/io";
import BoardInviteModal from './BoardInviteModal';
import BoardVisibilitySelector from './BoardVisibilitySelector';
import toast from 'react-hot-toast';
import { updateBoard } from '../api/boards';
import { useQueryClient } from '@tanstack/react-query';
import BoardSettingsModal from './BoardSettingsModal';

interface BoardHeaderProps {
  board: BoardDto;
}

const BoardHeader: React.FC<BoardHeaderProps> = ({ board }) => {
  const [isInviteModalOpen, setIsInviteModalOpen] = useState<boolean>(false);
  const [hoveredMemberId, setHoveredMemberId] = useState<number | null>(null);
  const [isEditingName, setIsEditingName] = useState(false);
  const [editedName, setEditedName] = useState(board.name);

  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState<boolean>(false);

  const isAdmin = board.boardRole === "ADMIN";

  const queryClient = useQueryClient();

  const MAX_VISIBLE_MEMBERS = 5;
  const visibleMembers = board.boardMembers.slice(0, MAX_VISIBLE_MEMBERS);
  const remainingCount = board.boardMembers.length - MAX_VISIBLE_MEMBERS;

  const handleNameSave = async () => {
    setIsEditingName(false);
    if ((editedName.trim() === board.name) || (editedName.trim() === "")) {
      setEditedName(board.name);
      return;
    }
    let request: BoardUpdateRequest = { 
      name: editedName.trim(),
      description: null,
      visibility: null,
    };
    try {
      await updateBoard(board.boardId, request);

      queryClient.setQueryData<BoardDto | undefined>(
        ["board", board.boardId],
        (old) => old ? {...old, name: editedName} : old
      );
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  const cancelEdit = () => {
    setIsEditingName(false);
    setEditedName(board.name);
  };

  return (
    <div className="flex items-center bg-[#151515] justify-between border-b border-gray-600 p-3">
      {/* Left placeholder */}
      <div className="w-1/3" />

      {/* Center — Board name */}
      <div className="flex-1 text-center">
        {isAdmin && isEditingName ? (
          <input
            type="text"
            value={editedName}
            onChange={(e) => setEditedName(e.target.value)}
            onBlur={handleNameSave}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleNameSave();
              if (e.key === "Escape") cancelEdit();
            }}
            autoFocus
            className="bg-[#1f1f1f] border border-gray-600 text-gray-100 text-2xl font-semibold text-center px-2 rounded w-2/3 outline-none focus:ring-2 focus:ring-purple-500"
          />
        ) : (
          <h1
            className={`text-2xl font-semibold text-gray-100 text-center rounded inline-block cursor-${isAdmin ? "pointer hover:px-2 hover:bg-[#2a2a2a]" : "default"} hover:text-gray-200`}
            onClick={() => isAdmin && setIsEditingName(true)}
          >
            {board.name}
          </h1>
        )}
      </div>

      {/* Right icons */}
      <div className="w-1/3 flex items-center justify-end space-x-3 text-gray-300">
        {/* Board Members */}
        <div className="flex items-center space-x-[-6px] mr-5">
          {visibleMembers.map((member) => (
            <div
              key={member.memberId}
              className="relative group"
              onMouseEnter={() => setHoveredMemberId(member.memberId)}
              onMouseLeave={() => setHoveredMemberId(null)}
            >
              <div
                className="w-9 h-9 rounded-full flex items-center justify-center text-white text-base font-bold cursor-pointer border-2 border-[#151515] transition-transform transform hover:scale-105"
                style={{ backgroundColor: member.avatarColor }}
              >
                {member.firstname[0].toUpperCase()}
              </div>

              {/* Hover popup */}
              {hoveredMemberId === member.memberId && (
                <div className="absolute top-12 left-1/2 -translate-x-1/2 bg-[#222222] text-gray-100 text-sm px-3 py-2 rounded shadow-lg z-10 whitespace-nowrap animate-fade-in">
                  <div className="font-semibold">
                    {member.firstname} {member.lastname}
                  </div>
                  <div className="text-gray-400">@{member.username}</div>
                </div>
              )}
            </div>
          ))}

          {/* "+N" indicator if overflow */}
          {remainingCount > 0 && (
            <div className="w-9 h-9 rounded-full bg-[#2a2a2a] flex items-center justify-center text-gray-100 text-sm font-semibold border-2 border-[#151515] cursor-default">
              +{remainingCount}
            </div>
          )}
        </div>

        {/* Visibility */}
        <BoardVisibilitySelector
          boardId={board.boardId}
          visibility={board.visibility}
          boardRole={board.boardRole}
        />

        {/* Add member */}
        <button
          onClick={() => setIsInviteModalOpen(true)}
          disabled={!isAdmin}
          className={`p-1.5 bg-[#222222] px-4 rounded cursor-pointer shadow-md flex items-center space-x-1 hover:bg-[#2a2a2a]
            ${isAdmin ? "text-white" : "pointer-events-none text-gray-400"}
          `}
        >
          <AiOutlineUserAdd size={20} />
          <span>Invite user</span>
        </button>
        <BoardInviteModal
          isOpen={isInviteModalOpen}
          onClose={() => setIsInviteModalOpen(false)}
          boardId={board.boardId}
        />

        {/* More (sidebar/details) */}
        <button
          onClick={() => setIsSettingsModalOpen(true)}
          className="p-1.5 hover:bg-[#2a2a2a] rounded cursor-pointer hover:text-white"
          title="Board details"
        >
          <IoMdMore size={24} />
        </button>
        <BoardSettingsModal
          isOpen={isSettingsModalOpen}
          onClose={() => setIsSettingsModalOpen(false)}
          board={board}
        />

      </div>
    </div>
  );
}

export default BoardHeader;
