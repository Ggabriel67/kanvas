import React from 'react'
import type { Notification } from '../types/notifications';
import toast from 'react-hot-toast';
import { acceptBoardInvitation, acceptWorkspaceInvitation, declineBoardInvitation, declineWorkspaceInvitation } from '../api/invitations';
import { useWorkspaceStore } from '../hooks/useWorkspaceStore';

interface NotificationPanelProps {
  notifications: Notification[];
	onRemove: (id: number) => void;
	onClose: () => void;
}

const timeAgo = (dateString: string) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diff < 60) return "a few seconds ago";
  if (diff < 3600) return `${Math.floor(diff / 60)} minute(s) ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} hour(s) ago`;
  return `${Math.floor(diff / 86400)} day(s) ago`;
};

const NotificationPanel: React.FC<NotificationPanelProps> = ({ notifications, onRemove }) => {

	const { triggerSideBarRefresh } = useWorkspaceStore();

	const handleAcceptWorkspaceInv = async (invitationId: number, targetName: string) => {
		try {
			await acceptWorkspaceInvitation(invitationId);
			toast.success(`You are now a member of ${targetName}!`);
			triggerSideBarRefresh();
		} catch (error: any) {
			toast.error(error.message);
		}
	}

	const handleDeclineWorkspaceInv = async (invitationId: number) => {
		try {
			await declineWorkspaceInvitation(invitationId);
		} catch (error: any) {
			toast.error(error.message);
		}
	}

	const handleAcceptBoardInv = async (invitationId: number, targetName: string) => {
		try {
			await acceptBoardInvitation(invitationId);
			toast.success(`You are now a member of ${targetName}!`);
			triggerSideBarRefresh();
		} catch (error: any) {
			toast.error(error.message);
		}
	}

	const handleDeclineBoardInv = async (invitationId: number) => {
		try {
			await declineBoardInvitation(invitationId);
		} catch (error: any) {
			toast.error(error.message);
		}
	}

  return (
    <div>
			{/* Panel Header */}
			<div className="px-4 pt-3">
				<span className="text-lg font-semibold text-gray-200">Notifications</span>
				<div className="border-t border-gray-700 mt-2" />
			</div>

			{/* Notifications List */}
			<div className="max-h-80 overflow-y-auto py-2">
				{notifications.length === 0 ? (
					<div className="p-4 text-gray-400 text-sm text-center">
						You have no unread notifications
					</div>
				) : (
					<ul className="divide-y divide-gray-700">
						{notifications.map((n) => {
							if (n.type === "INVITATION") {
								const inviterUsername = n.payload["inviterUsername"] as string;
								const targetName = n.payload["targetName"] as string;
								const scope = n.payload["scope"] as string;
								const invitationId = n.payload["invitationId"] as number;

								return (
									<li
										key={n.notificationId}
										className="p-3 pl-4 hover:bg-[#2a2a2a]"
									>
										<p className="text-gray-200 text-sm mb-2">
											<span>User&nbsp;</span>
											<span className="font-semibold text-purple-400">{inviterUsername}</span>{" "}
											invited you to {scope.toLowerCase()}{" "}
											<span className="font-semibold text-purple-400">{targetName}</span>
										</p>

										<div className="flex space-x-3 mb-2">
											<button 
												className="px-3 py-1 text-sm bg-purple-600 rounded hover:bg-purple-500 cursor-pointer"
												onClick={() => {
													if (scope === "WORKSPACE") handleAcceptWorkspaceInv(invitationId, targetName);
													if (scope === "BOARD") handleAcceptBoardInv(invitationId, targetName);
													onRemove(n.notificationId);
												}}
											>
												Accept
											</button>
											<button 
												className="px-3 py-1 text-sm bg-gray-600 rounded hover:bg-gray-500 cursor-pointer"
												onClick={() => {
													if (scope === "WORKSPACE") handleDeclineWorkspaceInv(invitationId);
													if (scope === "BOARD") handleDeclineBoardInv(invitationId);
													onRemove(n.notificationId);
												}}
											>
												Decline
											</button>
										</div>

										<span className="text-xs text-gray-500">
											{timeAgo(n.sentAt)}
										</span>
									</li>
								);
							}

							return <div key={n.notificationId}></div>;
						})}
					</ul>
				)}
			</div>
		</div>
  )
}

export default NotificationPanel
