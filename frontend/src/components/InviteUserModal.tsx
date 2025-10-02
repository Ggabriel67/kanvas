import React, { useCallback, useEffect, useState } from 'react'
import debounce from 'lodash.debounce';
import { searchUsers } from '../api/users';
import toast from 'react-hot-toast';
import type { UserDto } from '../api/users';
import useAuth from '../hooks/useAuth';
import { useForm } from 'react-hook-form';
import { IoMdClose } from "react-icons/io";
import type { WorkspaceInvitationRequest } from '../types/invitations';
import { createWorkspaceInvitation } from '../api/invitations';

interface InviteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  workspaceId: number;
}

const InviteUserModal: React.FC<InviteUserModalProps> = ({ isOpen, onClose, workspaceId }) => {
  const [results, setResults] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(false);
  const { user: currentUser } = useAuth();
  const [role, setRole] = useState<string>("MEMBER");

  const { register, watch, reset } = useForm<{ query: string }>();
  const query = watch("query");

  const doSearch = useCallback(
    debounce(async (q: string) => {
      if (!q.trim()) {
        setResults([]);
        return;
      }
      try {
        setLoading(true);
        const data = await searchUsers(q);
        setResults(data);
      } catch (err: any) {
        toast.error(err.message);
      } finally {
        setLoading(false);
      }
    }, 500),
    []
  );

  const handleReset = () => {
    reset();
  };

  const handleInvite = async (inviteeId: number) => {
    if (!currentUser) return;
    let request: WorkspaceInvitationRequest = {
      inviterId: currentUser?.id,
      inviteeId: inviteeId,
      workspaceId: workspaceId,
      role: role
    }
    try {
      await createWorkspaceInvitation(request);
      toast.success("Invitation sent!");
    } catch (error: any) {
      toast.error(error.message);
    }
  };

  useEffect(() => {
    doSearch(query || "");
  }, [query, doSearch]);

  if (!isOpen) {
    return null;
  }
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/75"
        onClick={() => {
          onClose();
          handleReset();
        }}
      />

      {/* Modal */}
      <div className="relative bg-[#1e1e1e] text-gray-200 rounded-xl shadow-xl w-full max-w-lg p-6 z-10 pb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">Invite user to workspace</h2>
          <IoMdClose 
            className="cursor-pointer hover:text-gray-400" 
            size={25}
            onClick={() => {
              onClose();
              handleReset();
            }}
          />
        </div>

        {/* Role selector */}
        <div className="mb-5">
          <label className="block text-m mb-1 text-gray-300">Select role</label>
          <select
            value={role}
            onChange={(event) => setRole(event.target.value)}
            className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option value="MEMBER">Member</option>
            <option value="ADMIN">Administrator</option>
          </select>
        </div>

        <div className="relative">
          <input
            {...register("query")}
            placeholder="Search by username or email..."
            className="w-full px-3 py-2 rounded bg-[#2a2a2a] text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
          />

          {/* Dropdown results */}
          {query && results.length > 0 && (
            <ul className="absolute z-50 mt-1 w-full max-h-60 overflow-y-auto bg-[#1e1e1e] rounded shadow-lg">
              {results.map(u => {
                const isCurrent = u.id === currentUser?.id;
                return (
                  <li
                    key={u.id}
                    className={`flex items-center justify-between p-2 rounded ${
                      isCurrent 
                        ? "bg-[#2a2a2a] text-gray-400 cursor-not-allowed" 
                        : "bg-[#1a1a1a] hover:bg-[#2a2a2a]"
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <div
                        className="w-8 h-8 rounded-full flex items-center justify-center font-bold text-white"
                        style={{ backgroundColor: u.avatarColor }}
                      >
                        {u.firstname[0].toUpperCase()}
                      </div>
                      <span>{isCurrent ? `(You) ${u.username} (${u.email})` : `${u.username} (${u.email})`}</span>
                    </div>
                    {}
                    {!isCurrent && (
                      <button
                        className="px-3 py-1 text-sm bg-purple-600 rounded hover:bg-purple-500 cursor-pointer"
                        onClick={() => handleInvite(u.id)}
                      >
                        Send
                      </button>
                    )}
                  </li>
                );
              })}
            </ul>
          )}

          {loading && (
            <p className="absolute mt-1 text-sm text-gray-400">Searching...</p>
          )}
        </div>
      </div>
    </div>
  )
}

export default InviteUserModal;
