import React, { useState } from 'react'
import type { BoardDto, BoardUpdateRequest } from '../types/boards';
import useAuth from '../hooks/useAuth';
import { IoMdClose } from "react-icons/io";
import { CiEdit } from "react-icons/ci";
import { useForm, type SubmitHandler } from 'react-hook-form';
import toast from 'react-hot-toast';
import { deleteBoard, updateBoard } from '../api/boards';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import BoardMembersTab from './BoardMembersTab';

interface WorkspaceSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  board: BoardDto;
}

type FormFields = {
  name: string;
  description: string;
};

const BoardSettingsModal: React.FC<WorkspaceSettingsModalProps> = ({ isOpen, onClose, board }) => {
  if (!isOpen) {return null;}

  const [activeTab, setActiveTab] = useState<"board" | "members">("board");
  const [isEditing, setIsEditing] = useState(false);

  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const isAdmin = board.boardRole === "ADMIN";

  const { 
    register, handleSubmit, reset, formState: { errors },
  } = useForm<FormFields>({ defaultValues: {
    name: board.name,
    description: board.description || "",
  } });

  const onEditSubmit: SubmitHandler<FormFields> = async (data: {name: string; description: string}) => {

    setIsEditing(false);

    let request: BoardUpdateRequest = { 
      name: data.name,
      description: data.description,
      visibility: null,
    };
    try {
      await updateBoard(board.boardId, request);

      queryClient.setQueryData<BoardDto | undefined>(
        ["board", board.boardId],
        (old) => old ? {...old, name: data.name, description: data.description } : old
      );

      toast.success("Board updated");
    } catch(error: any) {
      toast.error(error.message);
    }
    handleReset();
  }

  const handleDeleteBoard = async () => {
    try {
      await deleteBoard(board.boardId);
      toast.success("Board deleted");
      navigate("/app", { replace: true });
    } catch (error: any) {
      toast.error(error.message);
    }
  }
  
  const handleReset = () => {reset();};

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/75"
        onClick={() => {
          onClose();
          setActiveTab("board");
        }}
      />

      {/* Modal */}
      <div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl min-h-[750px] max-h-[750px] overflow-y-auto shadow-xl w-full max-w-4xl p-6 z-10 pb-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-xl font-semibold">Board Settings</h2>
          <IoMdClose
            className="cursor-pointer hover:text-gray-400"
            size={25}
            onClick={() => {
              onClose();
              setActiveTab("board");
            }}
        />
        </div>

        {/* Separator */}
        <div className="border-t border-gray-700 mb-4"></div>

        <div className="flex space-x-6 mb-6">
          {[
            { id: "board", label: "Board details" },
            { id: "members", label: "Members" },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`pb-1 text-base font-medium transition-colors border-b-2 cursor-pointer ${
                activeTab === tab.id
                  ? "border-purple-500 text-white"
                  : "border-transparent text-gray-400 hover:text-gray-200"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        {/* Tabs */}
        <div>
          {activeTab === "board" && (
            <div className="space-y-7">
              {isEditing ? (
                <form onSubmit={handleSubmit(onEditSubmit)} className="space-y-4">
                  <div>
                    <label className="block text-gray-400 mb-1">Name *</label>
                    <input
                      {...register("name", {
                        required: "Name is required",
                        validate: (value) =>
                          value.trim() !== "" || "Name is required"
                      })}
                      type="text"
                      className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                    />
                    {errors.name && (
                      <div className="text-red-500 text-sm mt-1">{errors.name.message}</div>
                    )}
                  </div>
                   
                  <div>
                    <label className="block text-gray-400 mb-1">Description (optional)</label>
                    <textarea
                      {...register("description")}
                      className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500 resize-none"
                      rows={3}
                    />
                    {errors.description && (
                      <div className="text-red-500 text-sm mt-1">{errors.description.message}</div>
                    )}
                  </div>
                   
                  <div className="flex space-x-3">
                    <button
                      type="submit"
                      className="px-4 py-2 bg-purple-600 hover:bg-purple-500 rounded text-white cursor-pointer"                      
                    >
                      Save
                    </button>
                    <button
                      className="px-4 py-2 bg-gray-600 hover:bg-gray-500 rounded text-white cursor-pointer"
                      onClick={() => { 
                        setIsEditing(false);
                        handleReset();}
                      }
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              ) : (
                <div>
                  <div className="flex items-center space-x-1">
                    <h1 className="text-2xl font-bold text-gray-100">{board.name}</h1>
                    {isAdmin && (
                      <button
                        className="cursor-pointer p-1 hover:bg-[#4a4a4a] rounded"
                        onClick={() => setIsEditing(true)}
                      >
                        <CiEdit size={24} />
                      </button>
                    )}
                  </div>
                  <p className="mt-2 text-gray-300 text-md">
                    {board.description || "* This board has no description"}
                  </p>
                </div>
              )}

              {isAdmin && (
                <div>
                  <div className="border-t border-gray-700 mb-4"></div>
                  <button
                    className="flex items-center space-x-1 bg-[#4a4a4a] hover:bg-red-700 text-white px-4 py-2 rounded  cursor-pointer"
                    onClick={handleDeleteBoard}
                  >
                    <IoMdClose size={18}/>
                    <span>Delete Board</span>
                  </button>
                </div>
              )}
             </div>
            )}

            {activeTab === "members" && (
              <BoardMembersTab
                boardId={board.boardId}
                members={board.boardMembers}
                currentRole={board.boardRole}
              />
            )}
        </div>
      </div>
    </div>
  )
}

export default BoardSettingsModal;
