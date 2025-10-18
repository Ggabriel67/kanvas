import React from 'react'
import BoardHeader from '../components/BoardHeader';
import { useParams } from 'react-router-dom';
import { useBoardQuery } from '../api/boards';
import ColumnsContainer from '../components/ColumnsContainer';
import useBoardSocket from '../hooks/useBoardSocket';

const Board = () => {
  const { boardId } = useParams<{ boardId: string }>();
  const bId = boardId ? parseInt(boardId, 10) : null;

  const { data: board, isLoading } = useBoardQuery(bId);
  useBoardSocket(bId);
  if (isLoading) return <div>Loading board...</div>;
  if (!board) return <div>Board not found</div>;
  
  return (
    <div className="flex flex-col gap-4 h-full">
      <BoardHeader board={board} />
      <ColumnsContainer 
        columns={board.columns}
        boardMembers={board.boardMembers}
        boardId={board.boardId}
        boardName={board.name}
        readonly={board.readonly}
      />
    </div>
  )
}

export default Board;
