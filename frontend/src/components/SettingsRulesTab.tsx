import React from 'react'
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";

const SettingsRulesTab = () => {
  return (
    <div className="space-y-5">
      <h3 className="text-lg font-semibold mb-3">Workspace Roles</h3>
      <ul className="space-y-1 text-gray-300 text-base">
        <li>
          <span className="font-semibold text-purple-400">Owner:</span> Full control, can edit, delete, and manage roles.
        </li>
        <li>
          <span className="font-semibold text-purple-400">Administrator:</span> Can manage members and settings, but cannot delete the workspace.
        </li>
        <li>
          <span className="font-semibold text-purple-400">Member:</span> Can view and collaborate, but cannot manage workspace settings.
        </li>
      </ul>

      <div className="border-t border-gray-700 mb-4"></div>

      <h3 className="text-lg font-semibold mb-3">Board Roles</h3>
      <ul className="space-y-1 text-gray-300 text-base">
        <li>
          <span className="font-semibold text-purple-400">Administrator:</span> Full control, can edit or delete board, manages members and roles.
        </li>
        <li>
          <span className="font-semibold text-purple-400">Editor:</span> Can edit contents, but cannot delete the board.
        </li>
        <li>
          <span className="font-semibold text-purple-400">Viewer:</span> Can display board in readonly mode.
        </li>
      </ul>

      <div className="border-t border-gray-700 mb-4"></div> 

      <h3 className="text-lg font-semibold mb-3">Workspace Visibility</h3>
      <div>Workspaces are visible only for their members.</div>
      <div className="border-t border-gray-700 mb-4"></div>

      <h3 className="text-lg font-semibold mb-3">Board Visibility</h3>
      <ul>
        <li className="flex">
          <div className="flex items-center space-x-1 text-purple-400">
            <MdLockOutline size={20}/>
            <span className="font-semibold">Private:</span>
          </div>
          <span>&nbsp;Visible only for board members.</span>
        </li>
        <li className="flex">
          <div className="flex items-center space-x-1 text-purple-400">
            <PiUsersThree size={20}/>
            <span className="font-semibold">Workspace public:</span>
          </div>
          <span>&nbsp;Visible to anyone in the workspace as readonly (implicit board <span className="font-semibold text-purple-400">Viever</span>).</span>
        </li>
      </ul>
    </div>
  )
}

export default SettingsRulesTab
