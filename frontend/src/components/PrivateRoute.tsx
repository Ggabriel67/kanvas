import React from 'react'
import useAuth from '../hooks/useAuth';
import { Navigate, Outlet } from 'react-router-dom';

const PrivateRoute = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="text-center text-gray-200">Loading...</div>;
  }

  return user ? <Outlet /> : <Navigate to="/" replace />;
}

export default PrivateRoute;
