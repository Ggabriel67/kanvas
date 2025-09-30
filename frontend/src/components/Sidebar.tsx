import React from 'react'
import { IoHomeOutline } from "react-icons/io5";
import { FaRegUser } from "react-icons/fa";
import { BsPersonWorkspace } from "react-icons/bs";
import { IoMdAdd } from "react-icons/io";
import { NavLink } from "react-router-dom";

const Sidebar = () => {
  return (
    <div className="w-64 bg-[#1a1a1a] border-r border-gray-600 flex flex-col">
      {/* Home button */}
      <NavLink
        to="/app"
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
          <ul className="space-y-1">
            <li>
              <button className="w-full text-left px-7 py-2 hover:bg-[#2a2a2a] items-center rounded-lg cursor-pointer space-x-2 flex">
                <BsPersonWorkspace  size={20}/>
                <span>My Workspace</span>
              </button>
            </li>
          </ul>
          <button className="w-full text-left px-7 py-2 text-purple-400 hover:text-purple-300 hover:bg-[#2a2a2a] rounded-lg cursor-pointer flex items-center space-x-2">
            <IoMdAdd size={20} />
            <span>New Workspace</span>
          </button>
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
              <button className="w-full text-left px-7 py-2 hover:bg-[#2a2a2a] items-center rounded-lg cursor-pointer space-x-2 flex">
                <BsPersonWorkspace  size={20}/>
                <span>Foregin Workspace 1</span>
              </button>
            </li>
            <li>
              <button className="w-full text-left px-7 py-2 hover:bg-[#2a2a2a] items-center rounded-lg cursor-pointer space-x-2 flex">
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
      <button className="w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a]  items-center rounded-lg cursor-pointer space-x-2">
        <FaRegUser size={18}/>
        <span>Guest workspaces</span>
      </button>
    </div>
  );
};

export default Sidebar;
