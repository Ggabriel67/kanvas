import React from 'react'
import useAuth from '../hooks/useAuth';
import { Navigate, Outlet } from 'react-router-dom';
import LoadingSpinner from './LoadingSpinner';

const PrivateRoute = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner />;
  }

  return user ? <Outlet /> : <Navigate to="/" replace />;
}

export default PrivateRoute;
