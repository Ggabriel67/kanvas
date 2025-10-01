import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom';
import { getWorkspace } from '../api/workspaces';
import type { Workspace } from '../types/workspace';
import toast from "react-hot-toast";
import { IoMdAdd } from "react-icons/io";
import { RiKanbanView } from "react-icons/ri";

const Workspace = () => {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const wsId = workspaceId ? parseInt(workspaceId, 10) : null;

  const [workspace, setWorkspace] = useState<Workspace>();

  const navigate = useNavigate();

  const fetchWorkspace = async () => {
    try {
      if (!wsId) return;
      const data = await getWorkspace(wsId);
      setWorkspace(data);
    } catch (error: any) {
      toast.error(error.message);
      navigate("/app");
    }
  };

  useEffect(() => {
    fetchWorkspace();
  }, [wsId])

  if (!workspace) return <div>Loading workspace...</div>;
  
  return (
    <div className="pr-20 pl-20 pt-5">
      {/* Workspace Header */}
      <div className="mb-10">
        <h1 className="text-2xl font-bold text-gray-100">{workspace.name}</h1>
        {workspace.createdAt && (
          <p className="text-sm text-gray-500">
            Created: {new Date(workspace.createdAt).toLocaleDateString()}
          </p>
        )}
        {workspace.description && workspace.description.trim() !== "" && (
          <p className="mt-2 text-gray-300">{workspace.description}</p>
        )}
      </div>

      {/* Separator */}
      <hr className="border-gray-600 mb-5" />

      {/* Boards Grid */}
      <div>
        <div className="flex items-center space-x-2 text-gray-300 text-xl mb-7">
          <RiKanbanView size={25}/>
          <span>Boards</span>
        </div>
        <div className="grid grid-cols-4 gap-20">
          {workspace.boardProjections.map((board) => (
            <div
              key={board.boardId}
              className="bg-[#242323] rounded-lg p-10 flex items-center justify-center text-gray-100 font-semibold cursor-pointer hover:bg-[#2a2a2a]"
            >
              {board.name}
            </div>
          ))}

          {/* Create Board Button */}
          <button className="bg-purple-700 flex items-center justify-center space-x-1 text-white rounded-lg p-10 font-semibold hover:bg-purple-500 cursor-pointer">
            <IoMdAdd size={20}/>
            <span>New Board</span>
          </button>
        </div>
      </div>
    </div>
  );
}

export default Workspace;
