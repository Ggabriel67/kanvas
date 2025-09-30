import React from 'react'
import useAuth from '../../hooks/useAuth';
import Sidebar from '../Sidebar';
import Navbar from '../Navbar';
import { Outlet } from 'react-router-dom';

const AppLayout: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  return (
    <div className="flex flex-col h-screen bg-[#121212] text-gray-100">
      {/* Top navbar always full width */}
      <Navbar />

      {/* Rest of the app below the navbar */}
      <div className="flex flex-1">
        <Sidebar />

        <main className="flex-1 p-4 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
