import React, { useEffect, useState } from 'react'
import useAuth from '../hooks/useAuth';
import type { GuestWorkspace } from '../types/workspaces';
import toast from 'react-hot-toast';
import { getGuestWorkspaces } from '../api/workspaces';
import { Link } from 'react-router-dom';

const GuestWorkspaces = () => {
	const [guestWorkspaces, setGuestWorkspaces] = useState<GuestWorkspace[]>([]);

	const { user } = useAuth();

	const fetchGuestWorkspaces = async () => {
		if (!user) return;
		try {
			const data = await getGuestWorkspaces(user.id);
			setGuestWorkspaces(data);
		} catch (error: any) {
			toast.error(error);
		}
	} 

	useEffect(() => {
		fetchGuestWorkspaces();
	}, [])
  
  return (
    <div className="pr-15 pl-15 pt-5">
      <div className="mb-7">
				<h1 className="text-2xl font-bold text-gray-100">Guest workspaces</h1>
				<p className="mt-2 text-gray-400 cursor-default">
					* You are a member of the following boards, but you do not belong to their parent workspace 
				</p>
			</div>

			{/* Separator */}
      <hr className="border-gray-600 mb-5" />
			{guestWorkspaces.length === 0 ? (
				<p className="text-gray-400 text-center mt-10">
					You are not a guest in any workspace
				</p>
			) : (
				<div className="space-y-8">
					{guestWorkspaces.map((w) => (
						<div key={w.workspaceId}>
							<h2 className="text-xl font-semibold text-gray-200 mb-4">
								{w.workspaceName}
							</h2>

							<div className="grid grid-flow-row auto-rows-auto gap-10 grid-cols-[repeat(auto-fill,300px)]">
								{w.boardProjections.map((board) => (
									<Link
										to={`/app/boards/${board.boardId}`}
										key={board.boardId}
										className="bg-[#333333] rounded-lg w-[300px] h-[100px] flex items-center justify-center text-gray-100 font-semibold cursor-pointer hover:bg-[#4a4a4a]"
									>
										{board.name}
									</Link>
								))}
							</div>

							<hr className="border-gray-600 mt-8" />
						</div>
					))}
				</div>
			)}
    </div>
  )
}

export default GuestWorkspaces;
