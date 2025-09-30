import React from 'react'
import { FaBell } from "react-icons/fa";
import { BsFillKanbanFill } from "react-icons/bs";
import useAuth from '../hooks/useAuth';

const Navbar = () => {
  const { user } = useAuth();

  return (
    <div className="h-16 bg-[#1a1a1a] border-b border-gray-600 flex items-center justify-between px-5">
      {/* Logo on the left */}
      <div className="flex items-center space-x-3 text-3xl font-bold">
        <BsFillKanbanFill size={30}/>
        <span>Kanban</span>
      </div>

      {/* Notifications + Avatar on the right */}
      <div className="flex items-center">
        <FaBell size={26} className="mr-4 cursor-pointer" />
        {user && (
          <div
            className="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold cursor-pointer"
            style={{ backgroundColor: user.avatarColor }}
          >
            {user.firstname[0].toUpperCase()}
          </div>
        )}
      </div>
    </div>
  );
};

export default Navbar;
