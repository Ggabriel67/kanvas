import React, { useEffect, useRef, useState } from 'react'
import { FaRegBell } from "react-icons/fa6";
import { IoLogOutOutline } from "react-icons/io5";
import { IoSettingsOutline } from "react-icons/io5";
import useAuth from '../../hooks/useAuth';
import appLogo from "../../assets/appLogo.png";
import { NavLink } from 'react-router-dom';
import { type Notification, type ReadNotificationsRequest } from '../../types/notifications';
import NotificationPanel from '../NotificationPanel';
import toast from 'react-hot-toast';
import { getNotifications, getUnreadNotificationsCount, updateNotificationsStatusToRead } from '../../api/notifications';
import useWebSocket from '../../hooks/useWebSocket';

interface NotificationMessage {
	notificationId: number;
	userId: number;
	type: "INVITATION" | "ASSIGNMENT" | "REMOVED_FROM_BOARD";
	status: string;
	sentAt: string;
	payload: Record<string, unknown>;
}

const Navbar = () => {
  const [isAccMenuOpen, setIsAccMenuOpen] = useState<boolean>(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [hasUnread, setHasUnread] = useState(false);

  const accMenuRef = useRef<HTMLDivElement>(null);
  const notifRef = useRef<HTMLDivElement>(null);

  const { user, logout } = useAuth();
  const { client, connected } = useWebSocket();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        accMenuRef.current &&
        !accMenuRef.current.contains(event.target as Node)
      ) {
        setIsAccMenuOpen(false);
      }
      if (
        notifRef.current &&
        !notifRef.current.contains(event.target as Node)
      ) {
        setIsNotifOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const checkUnread = async () => {
    if (!user) return;
    try {
      const count: number = await getUnreadNotificationsCount(user.id);
      if (count > 0) setHasUnread(true);
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  useEffect(() => {
    checkUnread();
  }, [])

  useEffect(() => {
    if (!connected || !client || !user) return;

    const sub = client.subscribe(
      `/user/${user.id}/notifications`,
      (message) => {
        try {
          const newNotification: NotificationMessage = JSON.parse(message.body);

          if (isNotifOpen) {
            setNotifications((prev) => [newNotification, ...prev]);
          } else {
            setHasUnread(true);
          }
        } catch (error) {
          toast.error(`Failed to parse WS notification: ${error}`);
        }
      }
    );

    return () => {
      sub.unsubscribe();
    }
  }, [connected, client, isNotifOpen])

  const loadNotifications = async () => {
    try {
      if (!user) return;
      const data = await getNotifications(user.id);
      setNotifications(data);

      let notificationIds = data.map(n => n.notificationId);
      let request: ReadNotificationsRequest = {
        ids: notificationIds
      } 
      await updateNotificationsStatusToRead(request);
    } catch (error: any) {
      toast.error(error.message);
    }
  }

  const removeNotification = (id: number) => {
    setNotifications((prev) => prev.filter((n) => n.notificationId !== id));
  };

  return (
    <div className="h-16 bg-[#1a1a1a] border-b border-gray-600 flex items-center justify-between px-2">
      {/* Logo */}
      <NavLink
        to="/app"
        className="flex px-2 items-center space-x-3 text-3xl font-bold hover:bg-[#2a2a2a] py-1 rounded-lg"
      >
        <img src={appLogo} width="30" height="30" />
        <span>Kanvas</span>
      </NavLink>

      {/* Right side: notifications + avatar */}
      <div className="flex items-center relative">
        {/* Notifications */}
        <div ref={notifRef} className="relative">
          
          <FaRegBell
            size={30}
            className="mr-5 cursor-pointer text-gray-300 hover:text-white"
            onClick={() => {
                setIsNotifOpen((prev) => !prev);
                setHasUnread(false);
                if (!isNotifOpen) loadNotifications();
              }
            }
          />
          <div className="relative">
            {hasUnread && (
              <span className="absolute -top-2 right-4 w-3 h-3 bg-purple-600 rounded-full" />
            )}
          </div>
          {isNotifOpen && (
            <div className="absolute right-0 translate-x-15 mt-4 w-100 bg-[#1a1a1a] border border-gray-600 rounded-lg shadow-lg z-50">
              <NotificationPanel
                notifications={notifications}
                onRemove={removeNotification}
                onClose={() => setIsNotifOpen(false)}
              />
            </div>
          )}
        </div>

        {/* Account menu */}
        <div className="relative" ref={accMenuRef}>
          {user && (
            <div
              className="w-10 h-10 mr-3 rounded-full flex items-center justify-center text-white text-xl font-bold hover:bg-[#2a2a2a] cursor-pointer"
              style={{ backgroundColor: user.avatarColor }}
              onClick={() => setIsAccMenuOpen((prev) => !prev)}
            >
              {user.firstname[0].toUpperCase()}
            </div>
          )}
          {isAccMenuOpen && (
            <div className="absolute right-0 translate-x-2 mt-3 w-50 bg-[#1a1a1a] border border-gray-600 rounded shadow-lg">
              <button className="block w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a] cursor-pointer space-x-2">
                <IoSettingsOutline size={24} />
                <span>Account settings</span>
              </button>
              <button
                onClick={logout}
                className="block w-full flex text-left px-3 py-2 hover:bg-[#2a2a2a] cursor-pointer space-x-2"
              >
                <IoLogOutOutline size={24} />
                <span>Logout</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Navbar;
