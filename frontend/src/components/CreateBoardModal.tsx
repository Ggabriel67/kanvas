import React, { useState } from 'react'
import useAuth from '../hooks/useAuth';
import { useForm, type SubmitHandler } from 'react-hook-form';
import type { BoardRequest } from '../types/boards';
import { createBoard } from '../api/boards';
import toast from 'react-hot-toast';
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";
import { useNavigate } from 'react-router-dom';

interface CreateBoardModalProps {
	isOpen: boolean;
	workspaceId: number;
	onClose: () => void;
}

type FormFields = {
  name: string;
  description: string;
};

const CreateBoardModal: React.FC<CreateBoardModalProps> = ({
  isOpen,
  workspaceId,
  onClose,
}) => {
  if (!isOpen) return null;

  const [visibility, setVisibility] = useState<string>("WORKSPACE_PUBLIC");

	const { user } = useAuth();
  const navigate = useNavigate();

  const { 
      register, 
      handleSubmit,
      setError,
      formState: { errors },
    } = useForm<FormFields>();

  const onSubmit: SubmitHandler<FormFields> = async (data) => {
      if (!user) return;
      let request: BoardRequest = { 
        creatorId: user?.id,
        workspaceId: workspaceId,
        name: data.name,
        description: data.description,
        visibility: visibility
      };
      try {
        const boardId = await createBoard(request);
        toast.success("Board created");
        navigate(`/app/boards/${boardId}`);
        onClose();
      } catch (error: any) {
        setError("root", { message: error.message });
      }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/75"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl shadow-xl w-full max-w-lg p-6 z-10">
        <h2 className="text-xl font-semibold mb-4">Create Board</h2>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Name */}
          <div>
            <label className="block text-sm mb-1">Name *</label>
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

          {/* Description */}
          <div>
            <label className="block text-sm mb-1">Description (optional)</label>
            <textarea
							{...register("description")}
              className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500 resize-none"
              rows={3}
            />
					{errors.description && (
            <div className="text-red-500 text-sm mt-1">{errors.description.message}</div>
          )}
          </div>

          {/* Visibility selector */}
          <div className="mb-5">
            <label className="block text-m mb-1 text-gray-300">Select visibility</label>
            <select
              value={visibility}
              onChange={(event) => setVisibility(event.target.value)}
              className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <option value="WORKSPACE_PUBLIC">Workspace public</option>
              <option value="PRIVATE"><span>Private</span></option>
            </select>
          </div>

          {/* Actions */}
          <div className="flex justify-end space-x-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded bg-gray-600 hover:bg-gray-500 cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 rounded bg-purple-600 hover:bg-purple-500 cursor-pointer"
            >
              Create
            </button>
          </div>
          {errors.root && (
            <div className="text-red-500 text-sm text-center mt-2">{errors.root.message}</div>
          )}
        </form>
      </div>
    </div>
  )
}

export default CreateBoardModal;
