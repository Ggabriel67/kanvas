import React from 'react'
import { Outlet } from 'react-router-dom';
import Navbar from '../Navbar';

const BoardLayout = () => {
  return (
    <div className="flex flex-col h-screen bg-[#121212] text-gray-100">
      {/* Top navbar always full width */}
      <Navbar />

      {/* Rest of the app below the navbar */}
      <div className="flex flex-1">
        <main className="flex-1 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default BoardLayout;
