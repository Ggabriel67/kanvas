import React, { useEffect, useRef, useState } from 'react'
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";
import type { BoardDto, BoardRequest, BoardUpdateRequest } from '../types/boards';
import toast from 'react-hot-toast';
import { updateBoard } from '../api/boards';
import { useQueryClient } from '@tanstack/react-query';

interface VisibilitySelectorProps {
  boardId: number;
  visibility: "WORKSPACE_PUBLIC" | "PRIVATE";
  boardRole: "ADMIN" | "EDITOR" | "VIEWER";
}

const BoardVisibilitySelector: React.FC<VisibilitySelectorProps> = ({ boardId, visibility, boardRole }) => {
	const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const queryClient = useQueryClient();
	const isPublic = visibility === "WORKSPACE_PUBLIC";
  const isAdmin = boardRole === "ADMIN";

  const onChange = async (newVisibility: "WORKSPACE_PUBLIC" | "PRIVATE") => {
    let request: BoardUpdateRequest = { 
      name: null,
      description: null,
      visibility: newVisibility
    };
    try {
      await updateBoard(boardId, request);

      queryClient.setQueryData(["board", boardId], (old: BoardDto) => {
        return { ...old, visibility: newVisibility};
      })
    } catch (error: any) {
      toast.error(error.message)
    }
  }

	useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Toggle Button */}
      <button
        className={`flex items-center space-x-1 cursor-pointer hover:text-white p-2 hover:bg-[#2a2a2a] rounded
          ${isOpen ? "bg-[#3a3a3a]" : ""}
        `}
        title={isPublic ? "Workspace Public" : "Private"}
        onClick={() => setIsOpen((prev) => !prev)}
      >
        {isPublic ? <PiUsersThree size={20} /> : <MdLockOutline size={20} />}
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div className="absolute left-1/2 -translate-x-1/2 mt-3 w-60 bg-[#151515] border border-gray-600 rounded-lg shadow-lg z-50">
          {/* Header */}
          <div className="px-4 pt-3 pb-2">
            <span className="text-lg font-semibold text-gray-200">Select visibility</span>
            <div className="border-t border-gray-700 mt-2" />
          </div>

          {/* Options */}
          <ul className="py-1 text-gray-200">
            <li
              className={`flex items-center space-x-2 px-3 py-2 ${
                isAdmin ? "cursor-pointer hover:bg-[#3a3a3a]" : "opacity-50 pointer-events-none"
              } ${isPublic ? "bg-[#3a3a3a]" : ""}`}
              onClick={() => {
                setIsOpen(false);
                if (!isPublic) onChange("WORKSPACE_PUBLIC");
              }}
            >
              <PiUsersThree size={18} />
              <span>Workspace Public</span>
            </li>

            <li
              className={`flex items-center space-x-2 px-3 py-2 ${
                isAdmin ? "cursor-pointer hover:bg-[#3a3a3a]" : "opacity-50 pointer-events-none"
              } ${!isPublic ? "bg-[#3a3a3a]" : ""}`}
              onClick={() => {
                setIsOpen(false);
                if (isPublic) onChange("PRIVATE");
              }}
            >
              <MdLockOutline size={18} />
              <span>Private</span>
            </li>
          </ul>
        </div>
      )}
    </div>
  )
}

export default BoardVisibilitySelector
