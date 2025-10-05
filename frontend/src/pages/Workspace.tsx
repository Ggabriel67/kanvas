import React, { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getWorkspace } from '../api/workspaces';
import type { WorkspaceDto } from '../types/workspaces';
import toast from "react-hot-toast";
import { IoMdAdd } from "react-icons/io";
import { RiKanbanView } from "react-icons/ri";
import { AiOutlineUserAdd } from "react-icons/ai";
import { IoSettingsOutline } from "react-icons/io5";
import { PiUsersThree } from "react-icons/pi";
import InviteUserModal from '../components/InviteUserModal';
import WorkspaceMembersModal from '../components/WorkspaceMembersModal';
import WorkspaceSettingsModal from '../components/WorkspaceSettingsModal';
import CreateBoardModal from '../components/CreateBoardModal';

const Workspace = () => {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const wsId = workspaceId ? parseInt(workspaceId, 10) : null;

  const [workspace, setWorkspace] = useState<WorkspaceDto>();
  const [isInviteModalOpen, setIsInviteModalOpen] = useState<boolean>(false);
  const [isMembersModalOpen, setisMembersModalOpen] = useState<boolean>(false);
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState<boolean>(false);
  const [isCreateBoardModalOpen, setIsCreateBoardModalOpen] = useState<boolean>(false);

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
    <div className="pr-15 pl-15 pt-5">
      <div className="mb-7">
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-2xl font-bold text-gray-100">{workspace.name}</h1>
          {workspace.createdAt && (
            <p className="text-sm text-gray-500">
              Created: {new Date(workspace.createdAt).toLocaleDateString()}
            </p>
          )}
        </div>

        <div className="flex space-x-3">
          <button
            onClick={() => setIsSettingsModalOpen(true)}
            className="px-4 py-2 bg-[#222222] text-white flex space-x-1 rounded-lg font-medium shadow-md items-center hover:bg-[#2a2a2a] cursor-pointer"
          >
            <IoSettingsOutline size={20} />
            <span>Settings</span>
          </button>
          <WorkspaceSettingsModal
            isOpen={isSettingsModalOpen}
            onClose={() => setIsSettingsModalOpen(false)}
            workspace={workspace}
            setWorkspace={setWorkspace}
          />

          <button
            onClick={() => setisMembersModalOpen(true)}
            className="px-4 py-2 bg-[#222222] text-white flex space-x-1 rounded-lg font-medium shadow-md items-center hover:bg-[#2a2a2a] cursor-pointer"
          >
            <PiUsersThree size={20} />
            <span>Members</span>
          </button>
          <WorkspaceMembersModal
            isOpen={isMembersModalOpen}
            onClose={() => setisMembersModalOpen(false)}
            workspaceId={workspace.id}
            workspaceRole={workspace.workspaceRole}
          />

          {/* Invite User Button (OWNER/ADMIN only) */}
          {(workspace.workspaceRole === "OWNER" || workspace.workspaceRole === "ADMIN") && (
            <>
              <button
                onClick={() => setIsInviteModalOpen(true)}
                className="px-4 py-2 bg-[#222222] text-white flex space-x-1 rounded-lg font-medium shadow-md items-center hover:bg-[#2a2a2a] cursor-pointer"
              >
                <AiOutlineUserAdd size={20} />
                <span>Invite user</span>
              </button>
              <InviteUserModal 
                isOpen={isInviteModalOpen}
                onClose={() => setIsInviteModalOpen(false)}
                workspaceId={workspace.id}
              />
            </>
          )}
        </div>
      </div>

      {/* Description below the header */}
      {workspace.description && workspace.description.trim() !== "" && (
        <p className="mt-3 text-gray-300">{workspace.description}</p>
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
            <Link
              to={`/app/boards/${board.boardId}`}
              key={board.boardId}
              className="bg-[#333333] rounded-lg p-10 flex items-center justify-center text-gray-100 font-semibold cursor-pointer hover:bg-[#4a4a4a] "
            >
              {board.name}
            </Link>
          ))}

          {/* Create Board Button */}
          <button
            onClick={() => setIsCreateBoardModalOpen(true)}
            className="bg-purple-700 flex items-center justify-center space-x-1 text-white rounded-lg p-10 font-semibold hover:bg-purple-600 cursor-pointer"
          >
            <IoMdAdd size={20}/>
            <span>New Board</span>
          </button>
          <CreateBoardModal
            isOpen={isCreateBoardModalOpen}
            workspaceId={workspace.id}
            onClose={() => setIsCreateBoardModalOpen(false)}
          />
        </div>
      </div>
    </div>
  );
}

export default Workspace;
