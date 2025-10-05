import React, { useState } from 'react'
import useAuth from '../hooks/useAuth';
import type { WorkspaceDto, WorkspaceRequest } from '../types/workspaces';
import { IoMdClose } from "react-icons/io";
import { CiEdit } from "react-icons/ci";
import SettingsRulesTab from './SettingsRulesTab';
import { useForm, type SubmitHandler } from 'react-hook-form';
import { updateWorkspace } from '../api/workspaces';
import toast from 'react-hot-toast';

interface WorkspaceSettingsModalProps {
	isOpen: boolean;
  onClose: () => void;
  workspace: WorkspaceDto;
  setWorkspace: React.Dispatch<React.SetStateAction<WorkspaceDto | undefined>>;
}

type FormFields = {
  name: string;
  description: string;
};

const WorkspaceSettingsModal: React.FC<WorkspaceSettingsModalProps> = ({
  isOpen, onClose, workspace, setWorkspace
}) => {
  if (!isOpen) {return null;}

  const [activeTab, setActiveTab] = useState<"workspace" | "boards" | "permissions">("workspace");
  const [isEditing, setIsEditing] = useState(false);
  
  const { user } = useAuth();

  const isAdminOrOwner = ["ADMIN", "OWNER"].includes(workspace.workspaceRole);
  const isOwner = workspace.workspaceRole === "OWNER";

  const { 
    register, handleSubmit,reset, formState: { errors },
  } = useForm<FormFields>({ defaultValues: {
    name: workspace.name,
    description: workspace.description || "",
  } });

  const onEditSubmit: SubmitHandler<FormFields> = async (data: {name: string; description: string}) => {
    if (!user) return;
    const updatedWorkspace = { ...workspace, ...data };

    setIsEditing(false);

    let request: WorkspaceRequest = { 
      creatorId: user?.id,
      name: data.name,
      description: data.description
    };
    try {
      await updateWorkspace(workspace.id, request);
      toast.success("Workspace updated");
    } catch(error: any) {
      toast.error(error.message);
    }
    setWorkspace(updatedWorkspace);
    handleReset();
  }

  const handleReset = () => {reset();};
  
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/75"
        onClick={() => {
          onClose();
          setActiveTab("workspace");
        }}
      />
      
      {/* Modal */}
      <div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl min-h-[700px] max-h-[700px] overflow-y-auto shadow-xl w-full max-w-4xl p-6 z-10 pb-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-xl font-semibold">Workspace Settings</h2>
          <IoMdClose
            className="cursor-pointer hover:text-gray-400"
            size={25}
            onClick={() => {
              onClose();
              setActiveTab("workspace");
            }}
        />
        </div>
        
        {/* Separator */}
        <div className="border-t border-gray-700 mb-4"></div>

        {/* Tabs */}
        <div className="flex space-x-6 mb-6">
          {[
            { id: "workspace", label: "Workspace" },
            { id: "boards", label: "Boards" },
            { id: "permissions", label: "Rules & Permissions" },
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

        {/* Tab content */}
        <div>
          {activeTab === "workspace" && (
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
                      // value={workspace.name}
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
                      // value={workspace.description}
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
                    <h1 className="text-2xl font-bold text-gray-100">{workspace.name}</h1>
                    {isAdminOrOwner && (
                      <button
                        className="cursor-pointer p-1 hover:bg-[#4a4a4a] rounded"
                        onClick={() => setIsEditing(true)}
                      >
                        <CiEdit size={24} />
                      </button>
                    )}
                  </div>
                  <p className="mt-2 text-gray-300 text-md">
                    {workspace.description || "* This workspace has no description"}
                  </p>
                </div>
              )}

              {isOwner && (
                <div>
                  <div className="border-t border-gray-700 mb-4"></div>
                  <button
                    className="flex items-center space-x-1 bg-[#4a4a4a] hover:bg-red-700 text-white px-4 py-2 rounded  cursor-pointer"
                    // onClick={handleDeleteWorkspace} TODO
                  >
                    <IoMdClose size={18}/>
                    <span>Delete Workspace</span>
                  </button>
                </div>
              )}
            </div>
          )}

          {activeTab === "boards" && (
            <div>
              <h3 className="text-xl font-semibold mb-4">Workspace Boards</h3>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                {[1, 2, 3].map((i) => (
                  <div
                    key={i}
                    className="bg-[#2a2a2a] p-6 rounded-lg text-center hover:bg-[#3a3a3a] transition-colors"
                  >
                    Board Placeholder {i}
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === "permissions" && (
            <SettingsRulesTab />
          )}
        </div>
      </div>
    </div>
  )
}
export default WorkspaceSettingsModal;
