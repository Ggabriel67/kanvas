import React from 'react'
import { useParams } from 'react-router-dom';

const Workspace = () => {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const wsId = workspaceId ? parseInt(workspaceId, 10) : null;
  
  return (
    <div>
      Welcome to workspace {wsId}
    </div>
  )
}

export default Workspace;
