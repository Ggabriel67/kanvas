import React from 'react'
import { BsFillKanbanFill } from "react-icons/bs";
import { IoHomeOutline } from "react-icons/io5";
import { FaRegUser } from "react-icons/fa";
import { BsPersonWorkspace } from "react-icons/bs";

const Sidebar = () => {
  return (
    <div className="w-64 bg-[#1a1a1a] border-r border-gray-600 flex flex-col">
      {/* Home button */}
      <div className="pt-3">
        <button className="w-full text-left px-5 py-2 hover:bg-[#2a2a2a] rounded cursor-pointer flex space-x-2  items-center">
          <IoHomeOutline size={20}/>
          <span>Home</span>
        </button>
      </div>
      {/* Divider under Home */}
      <div className="border-b border-gray-600 my-3" />

      {/* My Workspaces Section */}
      <div>
        <div className="px-3 py-2 text-sm font-semibold text-gray-400 uppercase">
          My Workspaces
        </div>
        <nav className="flex-1 overflow-y-auto">
          <ul className="space-y-1">
            <li>
              <button className="w-full text-left px-7 py-2 hover:bg-[#2a2a2a] items-center rounded cursor-pointer space-x-2 flex">
                <BsPersonWorkspace  size={20}/>
                <span>My Workspace</span>
              </button>
            </li>
          </ul>
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
          <ul className="space-y-1">
            <li>
              <button className="w-full text-left px-7 py-2 hover:bg-[#2a2a2a] items-center rounded cursor-pointer space-x-2 flex">
                <BsPersonWorkspace  size={20}/>
                <span>Foregin Workspace 1</span>
              </button>
            </li>
            <li>
              <button className="w-full text-left px-7 py-2 hover:bg-[#2a2a2a] items-center rounded cursor-pointer space-x-2 flex">
                <BsPersonWorkspace  size={20}/>
                <span>Foregin Workspace 2</span>
              </button>
            </li>
          </ul>
        </nav>
      </div>

      {/* Divider under Workspaces */}
      <div className="border-b border-gray-600 my-3" />

      {/* Guest Workspaces */}
      <button className="w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a]  items-center rounded cursor-pointer space-x-2">
        <FaRegUser size={18}/>
        <span>Guest workspaces</span>
      </button>
    </div>
  );
};

export default Sidebar;
