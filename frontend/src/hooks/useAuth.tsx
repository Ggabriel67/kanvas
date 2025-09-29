import React, { useContext } from 'react'
import { AuthContext } from '../providers/AuthProvider'

const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("Invalid use of use Auth");
  }
  return context;
}

export default useAuth;
