import React, { useEffect, useRef, useState } from 'react'
import { FaRegBell } from "react-icons/fa6";
import { IoLogOutOutline } from "react-icons/io5";
import { IoSettingsOutline } from "react-icons/io5";
import useAuth from '../hooks/useAuth';
import appLogo from '../assets/appLogo.png'
import { NavLink } from 'react-router-dom';

const Navbar = () => {
  const [isAccMenuOpen, setIsAccMenuOpen] = useState<boolean>(false);
  const accMenuRef = useRef<HTMLDivElement>(null);

  const { user, logout } = useAuth();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (accMenuRef.current && !accMenuRef.current.contains(event.target as Node)) {
        setIsAccMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div className="h-16 bg-[#1a1a1a] border-b border-gray-600 flex items-center justify-between px-3">
      {/* Logo and app name */}
      <NavLink 
        to="/app"
        className="flex px-2 items-center space-x-3 text-3xl font-bold hover:bg-[#2a2a2a] py-2 rounded-lg"
      >
        <img src={appLogo} width="30" height="30"/>
        <span>Kanvas</span>
      </NavLink>

      {/* Notification bell and avatar icon */}
      <div className="flex items-center relative" ref={accMenuRef}>
        <FaRegBell size={28} className="mr-5 cursor-pointer" />
        {user && (
          <div className="relative">
            <div
              className="w-10 h-10 mr-2 rounded-full flex items-center justify-center text-white text-xl font-bold cursor-pointer"
              style={{ backgroundColor: user.avatarColor }}
              onClick={() => setIsAccMenuOpen((prev) => !prev)}
            >
              {user.firstname[0].toUpperCase()}
            </div>

            {isAccMenuOpen && (
              <div className="absolute right-0 translate-x-2 mt-3 w-50 bg-[#1a1a1a] border border-gray-600 rounded shadow-lg">
                <button className="block w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a] cursor-pointer space-x-2">
                  <IoSettingsOutline size={24}/>
                  <span>Account settings</span>
                </button>
                <button
                  onClick={logout}
                  className="block w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a] cursor-pointer space-x-2"
                >
                  <IoLogOutOutline size={24}/>
                  <span>Logout</span>
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Navbar;
