import React from 'react'
import useAuth from '../hooks/useAuth';

const HomePage = () => {
  const { user } = useAuth();

  return (
    <div>
      Welcome {user?.username}!
    </div>
  )
}

export default HomePage;
