import React, { useState } from 'react'
import type { BoardDto } from '../types/boards';
import { AiOutlineUserAdd } from "react-icons/ai";
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";
import { IoMdMore } from "react-icons/io";
import BoardInviteModal from './BoardInviteModal';

interface BoardHeaderProps {
  board: BoardDto;
}

const BoardHeader: React.FC<BoardHeaderProps> = ({ board }) => {
  const [isInviteModalOpen, setIsInviteModalOpen] = useState<boolean>(false);

  const isPublic = board.visibility === "WORKSPACE_PUBLIC";

  return (
    <div className="flex items-center bg-[#151515] justify-between border-b border-gray-600 p-3">
      {/* Left placeholder (can later be breadcrumbs or back button) */}
      <div className="w-1/3" />

      {/* Center — Board name */}
      <h1 className="text-2xl font-semibold text-gray-100 text-center flex-1">
        {board.name}
      </h1>

      {/* Right icons */}
      <div className="w-1/3 flex items-center justify-end space-x-4 text-gray-300">
        {/* Visibility */}
        <div
          className="flex items-center space-x-1 cursor-pointer hover:text-gray-100"
          title={isPublic ? "Workspace Public" : "Private"}
        >
          {isPublic ? <PiUsersThree size={20} /> : <MdLockOutline size={20} />}
        </div>

        {/* Add member */}
        <button
          onClick={() => setIsInviteModalOpen(true)}
          className="p-1.5 hover:bg-[#2a2a2a] px-4 text-white rounded cursor-pointer shadow-md flex items-center space-x-1 bg-[#222222]"
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
          className="p-1.5 hover:bg-[#2a2a2a] rounded cursor-pointer"
          title="Board details"
        >
          <IoMdMore size={24} />
        </button>
      </div>
    </div>
  );
}

export default BoardHeader
