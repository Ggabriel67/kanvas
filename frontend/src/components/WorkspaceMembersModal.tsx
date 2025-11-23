import React, { useEffect, useState } from 'react'
import type { WorkspaceMember, WorkspaceMemberRemoveRequest, WorkspaceRoleChangeRequest } from '../types/workspaces';
import toast from 'react-hot-toast';
import { changeWorkspaceMemberRole, getAllWorkspaceMembers, leaveWorkspace, removeWorkspaceMember } from '../api/workspaces';
import { IoMdClose } from "react-icons/io";
import useAuth from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { useWorkspaceStore } from '../hooks/useWorkspaceStore';

interface WorkspaceMembersModalProps {
	isOpen: boolean;
  onClose: () => void;
  workspaceId: number;
  workspaceRole: string;
}

const canChangeRole = (currentRole: string, targetRole: string) => {
  if (targetRole === "OWNER") return false;
  if (currentRole === "OWNER") return true;
  if (currentRole === "ADMIN" && targetRole === "MEMBER") return true;
  return false;
};

const canRemove = (currentRole: string, targetRole: string, isSelf: boolean) => {
  if (isSelf) return false;
  if (targetRole === "OWNER") return false;
  if (currentRole === "OWNER") return true;
  if (currentRole === "ADMIN" && targetRole === "MEMBER") return true;
  return false;
};

const WorkspaceMembersModal: React.FC<WorkspaceMembersModalProps> = ({ 
	isOpen,
	onClose,
	workspaceId,
	workspaceRole
 }) => {
	const [members, setMembers] = useState<WorkspaceMember[]>([]);

	const navigate = useNavigate();
	const { user } = useAuth();

	const { triggerSideBarRefresh } = useWorkspaceStore();

	const fetchMembers = async () => {
		try {
			const data = await getAllWorkspaceMembers(workspaceId);
			setMembers(data);
		} catch (error: any) {
			toast.error(error.message)
		}
	}

	useEffect(() => {
		if (isOpen) {
    	fetchMembers();
  	}
	}, [isOpen])

	const handleRoleChange = async (memberId: number, newRole: string) => {
		try {
			let request: WorkspaceRoleChangeRequest = {
				targetMemberId: memberId,
				workspaceId: workspaceId,
				newRole: newRole
			}
			await changeWorkspaceMemberRole(request);
			fetchMembers();
		} catch (error: any) {
			toast.error(error.message);
		}
	}

	const handleLeaveWorkspace = async (memberId: number) => {
		try {
			let request: WorkspaceMemberRemoveRequest = {
				targetMemberId: memberId,
				workspaceId: workspaceId,
			}
			await leaveWorkspace(request);
			triggerSideBarRefresh();
			navigate("/app", { replace: true });
		} catch (error: any) {
			toast.error(error.message);
		}
	}

	const handleRemoveMember = async (memberId: number, targetUsername: string) => {
		try {
			let request: WorkspaceMemberRemoveRequest = {
				targetMemberId: memberId,
				workspaceId: workspaceId,
			}
			await removeWorkspaceMember(request);
			toast.success(`User ${targetUsername} has been removed from the workspace`);
			fetchMembers();
		} catch (error: any) {
			toast.error(error.message);
		}
	}

	if (!isOpen) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
    	{/* Overlay */}
			<div
				className="fixed inset-0 bg-black/75"
				onClick={onClose}
			/>

    	{/* Modal */}
    	<div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl shadow-xl w-full max-w-4xl p-6 z-10 pb-8">
				{/* Header */}
				<div className="flex items-center justify-between mb-4">
					<h2 className="text-xl font-semibold">Workspace Members</h2>
					<IoMdClose
						className="cursor-pointer hover:text-gray-400"
						size={25}
						onClick={onClose}
					/>
				</div>

				{/* Separator */}
				<div className="border-t border-gray-700 mb-5"></div>

      {/* Members list */}
      <ul className="space-y-2 max-h-80 overflow-y-auto mb-10">
        {members
          .slice()
          .sort((a, b) => (a.userId === user?.id ? -1 : b.userId === user?.id ? 1 : 0))
          .map((m) => (
          <li
            key={m.memberId}
            className="flex items-center justify-between p-3 bg-[#2a2a2a] rounded"
          >
            {/* Avatar + details */}
            <div className="flex items-center space-x-3">
              <div
                className="w-10 h-10 rounded-full flex items-center justify-center font-bold text-white"
                style={{ backgroundColor: m.avatarColor }}
              >
                {m.firstname[0].toUpperCase()}
              </div>
              <div>
                <p className="font-semibold">
                  {m.firstname} {m.lastname}
                  {user?.id === m.userId ? " (You)" : ""}
                </p>
                <p className="text-sm text-gray-400">
                  {m.username} &#x2022; Joined: {new Date(m.joinedAt).toLocaleDateString()}
                </p>
              </div>
            </div>

            {/* Role + Remove */}
            <div className="flex items-center space-x-3">
              <div className="relative">
								{canChangeRole(workspaceRole, m.role) ? (
									<select
										className="min-w-[130px] rounded bg-[#4a4a4a] text-white text-base px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-purple-500 cursor-pointer"
										value={m.role}
										onChange={(e) => handleRoleChange(m.memberId, e.target.value)}
									>
										<option value="MEMBER">Member</option>
										<option value="ADMIN">Administrator</option>
									</select>
								) : (
									<button disabled className="min-w-[130px] text-gray-300 rounded bg-[#3a3a3a] text-base px-3 py-1.5 text-center">
										{m.role === "OWNER"
											? "Owner"
											: m.role === "ADMIN"
											? "Administrator"
											: "Member"}
									</button>
								)}
							</div>

							{user?.id === m.userId ? (
								<button
									className="min-w-[150px] flex items-center justify-center space-x-1 px-3 py-1.5 text-base bg-[#4a4a4a] rounded hover:bg-red-700 cursor-pointer"
									onClick={() => handleLeaveWorkspace(m.memberId)}
									disabled={m.role === "OWNER"}
								>
									<IoMdClose size={18}/>
									<span>Leave workspace</span>
								</button>
							) : (
								canRemove(workspaceRole, m.role, m.userId === user?.id) && (
									<button
										className="min-w-[150px] flex items-center justify-center space-x-1 px-3 py-1.5 text-base bg-[#4a4a4a] rounded hover:bg-red-700  cursor-pointer"
										onClick={() => handleRemoveMember(m.memberId, m.username)}
									>
										<IoMdClose size={18}/>
                  	<span>Remove member</span>
									</button>
								)
							)}

            </div>
          </li>
        ))}
      </ul>
    </div>
  </div>
  )
}

export default WorkspaceMembersModal;
