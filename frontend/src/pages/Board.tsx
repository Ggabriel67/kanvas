import React from 'react'
import BoardHeader from '../components/BoardHeader';
import { useNavigate, useParams } from 'react-router-dom';
import { useBoardQuery } from '../api/boards';
import ColumnsContainer from '../components/ColumnsContainer';
import useBoardSocket from '../hooks/useBoardSocket';
import toast from 'react-hot-toast';
import useAuth from '../hooks/useAuth';

const Board = () => {
  const { boardId } = useParams<{ boardId: string }>();
  const bId = boardId ? parseInt(boardId, 10) : null;

  const navigate = useNavigate();
  const { user } = useAuth();

  const { data: board, isError, error, isLoading } = useBoardQuery(bId);
  if (isError) {
    toast.error(error.message);
    navigate("/app", { replace: true });
  }
  useBoardSocket(bId);
  if (isLoading) return <div>Loading board...</div>;
  if (!board) return <div>Board not found</div>;

  if (!user) return;
  const member = board.boardMembers.find((bm) => bm.userId === user?.id);
  const readonly = !member || member.boardRole === "VIEWER";
  
  return (
    <div className="flex flex-col gap-4 h-full">
      <BoardHeader board={board} />
      <ColumnsContainer 
        columns={board.columns}
        boardMembers={board.boardMembers}
        boardId={board.boardId}
        boardName={board.name}
        readonly={readonly}
      />
    </div>
  )
}

export default Board;
