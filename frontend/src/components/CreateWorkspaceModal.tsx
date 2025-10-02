import React from 'react'
import { useForm, type SubmitHandler } from 'react-hook-form';
import { type WorkspaceRequest } from '../types/workspace';
import useAuth from '../hooks/useAuth';

interface CreateWorkspaceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreate: (request: WorkspaceRequest) => Promise<void>;
}

type FormFields = {
  name: string;
  description: string;
};

const CreateWorkspaceModal: React.FC<CreateWorkspaceModalProps> = ({
	isOpen,
  onClose,
  onCreate,
}) => {

	if (!isOpen) return null;

	const { user } = useAuth();

	const { 
		register, 
		handleSubmit,
		formState: { errors },
	} = useForm<FormFields>();

	const onSubmit: SubmitHandler<FormFields> = async (data) => {
		if (!user) return;
		let request: WorkspaceRequest = { 
			creatorId: user?.id,
			name: data.name,
			description: data.description
		};
		await onCreate(request);
		onClose();
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
        <h2 className="text-xl font-semibold mb-4">Create Workspace</h2>

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
              className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
              rows={3}
            />
					{errors.description && (
            <div className="text-red-500 text-sm mt-1">{errors.description.message}</div>
          )}
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
        </form>
      </div>
    </div>
  );
}

export default CreateWorkspaceModal;
