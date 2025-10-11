import React, { useState } from 'react'
import type { BoardDto } from '../types/boards';
import { AiOutlineUserAdd } from "react-icons/ai";
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";
import { IoMdMore } from "react-icons/io";
import BoardInviteModal from './BoardInviteModal';
import BoardVisibilitySelector from './BoardVisibilitySelector';

interface BoardHeaderProps {
  board: BoardDto;
}

const BoardHeader: React.FC<BoardHeaderProps> = ({ board }) => {
  const [isInviteModalOpen, setIsInviteModalOpen] = useState<boolean>(false);

  const isAdmin = board.boardRole === "ADMIN";

  return (
    <div className="flex items-center bg-[#151515] justify-between border-b border-gray-600 p-3">
      {/* Left placeholder (can later be breadcrumbs or back button) */}
      <div className="w-1/3" />

      {/* Center — Board name */}
      <h1 className="text-2xl font-semibold text-gray-100 text-center flex-1">
        {board.name}
      </h1>

      {/* Right icons */}
      <div className="w-1/3 flex items-center justify-end space-x-3 text-gray-300">
        {/* Visibility */}
        <BoardVisibilitySelector
          boardId={board.boardId}
          visibility={board.visibility}
          boardRole={board.boardRole}
        />

        {/* Add member */}
        <button
          onClick={() => setIsInviteModalOpen(true)}
          className={`p-1.5 bg-[#222222] px-4 text-white rounded cursor-pointer shadow-md flex items-center space-x-1
            ${isAdmin ? "hover:bg-[#2a2a2a]" : "opacity-50 pointer-events-none"}
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
          className="p-1.5 hover:bg-[#2a2a2a] rounded cursor-pointer hover:text-white"
          title="Board details"
        >
          <IoMdMore size={24} />
        </button>
      </div>
    </div>
  );
}

export default BoardHeader
