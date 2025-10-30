import React from 'react'
import { Link } from 'react-router-dom';
import { FaArrowLeft } from "react-icons/fa";

const NotFoundPage = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-[#121212] text-gray-200 px-4">
      <h1 className="text-8xl font-extrabold text-purple-600 mb-4">404</h1>
      <h2 className="text-2xl font-semibold mb-2">Page Not Found</h2>
      <p className="text-gray-400 mb-8 text-center max-w-md">
        The page you’re looking for doesn’t exist.
      </p>
      <Link
        to="/"
        className="flex items-center gap-2 bg-purple-700 hover:bg-purple-800 text-white px-5 py-3 rounded-lg font-medium"
      >
        <FaArrowLeft className="text-sm" />
        Go back to Sign in
      </Link>
    </div>
  );
}

export default NotFoundPage;
