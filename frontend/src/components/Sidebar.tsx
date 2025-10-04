import React, { use, useEffect, useState } from 'react'
import { IoHomeOutline } from "react-icons/io5";
import { FaRegUser } from "react-icons/fa";
import { BsPersonWorkspace } from "react-icons/bs";
import { IoMdAdd } from "react-icons/io";
import { NavLink, useNavigate } from "react-router-dom";
import type { WorkspaceProjection } from '../types/workspaces';
import { createWorkspace, getAllUserWorkspaces } from '../api/workspaces';
import useAuth from '../hooks/useAuth';
import toast from "react-hot-toast";
import CreateWorkspaceModal from './CreateWorkspaceModal';
import { type WorkspaceRequest } from '../types/workspaces';

const Sidebar = () => {
  const [workspaces, setWorkspaces] = useState<WorkspaceProjection[]>([]);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const { user } = useAuth();

  const navigate = useNavigate();

  const fetchWorkspaces = async () => {
    try {
      if (!user){
        toast.error("No user");
        return
      }
      const data = await getAllUserWorkspaces(user?.id);
      setWorkspaces(data);
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  useEffect(() => {
    fetchWorkspaces();
  }, [user]);

  const handleCreate = async (request: WorkspaceRequest) => {
    try {
      const workspaceId: number = await createWorkspace(request);
      toast.success(`Workspace created`);
      fetchWorkspaces();
      navigate(`/app/workspaces/${workspaceId}`)
    } catch(err: any) {
      toast.error(err.message || "Failed to create workspace");
    }
  };

  const owned = workspaces.filter(w => w.role === "OWNER");
  const others = workspaces.filter(w => w.role !== "OWNER");

  return (
    <div className="w-66 bg-[#1a1a1a] border-r border-gray-600 flex flex-col">
      {/* Home button */}
      <NavLink
        to="/app"
        end
        className={({ isActive }) =>
          `w-full text-left mt-3 px-3 py-2 rounded-lg cursor-pointer flex space-x-2 items-center ${
            isActive ? "bg-purple-900" : "hover:bg-[#2a2a2a]"
          }`
        }
      >
        <IoHomeOutline size={20} />
        <span>Home</span>
      </NavLink> 
      
      {/* My Workspaces Section */}
      <div>
        <div className="px-3 mt-3 py-2 text-sm font-semibold text-gray-400 uppercase">
          My Workspaces
        </div>
        <nav className="flex-1 overflow-y-auto">
          <ul>
            {owned.map(ws => (
              <li key={ws.id}>
                <NavLink
                  to={`/app/workspaces/${ws.id}`}
                  className={({ isActive }) =>
                    `w-full text-left px-7 py-2 rounded-lg cursor-pointer flex space-x-2 items-center ${
                      isActive ? "bg-purple-900" : "hover:bg-[#2a2a2a]"
                    }`
                  }
                >
                  <BsPersonWorkspace  size={20}/>
                  <span>{ws.name}</span>
                </NavLink>
              </li>
            ))}
          </ul>
          <button 
            onClick={() => setIsModalOpen(true)}
            className="w-full text-left px-7 py-2 text-purple-400 hover:text-purple-300 hover:bg-[#2a2a2a] rounded-lg cursor-pointer flex items-center space-x-1"
          >
            <IoMdAdd size={20} />
            <span>New Workspace</span>
          </button>
          <CreateWorkspaceModal 
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            onCreate={handleCreate}
          />

        </nav>
      </div>

      {/* Divider under Workspaces */}
      <div className="border-b border-gray-600 my-3" />

      {/* Other Workspaces Section */}
      <div>
        <div className="px-3 py-2 text-sm font-semibold text-gray-400 uppercase">
          Other Workspaces
        </div>
        <nav className="flex-1 overflow-y-auto">
          <ul>
            {others.map(ws => (
              <li key={ws.id}>
                <NavLink
                  to={`/app/workspaces/${ws.id}`}
                  className={({ isActive }) =>
                    `w-full text-left px-7 py-2 rounded-lg cursor-pointer flex space-x-2 items-center ${
                      isActive ? "bg-purple-900" : "hover:bg-[#2a2a2a]"
                    }`
                  }
                >
                  <BsPersonWorkspace  size={20}/>
                  <span>{ws.name}</span>
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>
      </div>

      {/* Divider under Workspaces */}
      <div className="border-b border-gray-600 my-3" />

      {/* Guest Workspaces */}
      <button className="w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a]  items-center rounded-lg cursor-pointer space-x-2">
        <FaRegUser size={18}/>
        <span>Guest workspaces</span>
      </button>
    </div>
  );
};

export default Sidebar;
