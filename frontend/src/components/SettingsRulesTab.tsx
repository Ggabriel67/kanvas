import React from 'react'
import { MdLockOutline } from "react-icons/md";
import { PiUsersThree } from "react-icons/pi";
import { FaCrown } from "react-icons/fa";
import { RiSpyLine } from "react-icons/ri";
import { MdOutlineLocalPolice } from "react-icons/md";
import { LuWrench } from "react-icons/lu";
import { GoLaw } from "react-icons/go";
import { GrUserWorker } from "react-icons/gr";


const SettingsRulesTab = () => {
  return (
    <div className="space-y-5">
      <h3 className="text-lg font-semibold mb-3">Workspace Visibility</h3>
      <div className="text-gray-300">Workspaces are visible only for their members.</div>
      
      <div className="border-t border-gray-700 mb-4"></div>

      <h3 className="text-lg font-semibold mb-3">Workspace Roles</h3>
      <ul className="space-y-1 text-gray-300 text-base">
        <li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0">
						<FaCrown size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Owner:</span>
						<span className="align-middle">
							&nbsp;Full control, can edit, delete and manage roles.
						</span>
					</div>
				</li>
        <li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0">
						<GoLaw size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Administrator:</span>
						<span className="align-middle">
							&nbsp;Can manage members and settings, but cannot delete the workspace.
						</span>
					</div>
				</li>
        <li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0 ">
						<GrUserWorker size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Member:</span>
						<span className="align-middle">
							&nbsp;Can view and collaborate, but cannot manage workspace settings.
						</span>
					</div>
				</li>
      </ul>

      <div className="border-t border-gray-700 mb-4"></div>

      <h3 className="text-lg font-semibold mb-3">Board Visibility</h3>
      <ul className="space-y-3 text-gray-300">
				<li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0 mt-0.5">
						<MdLockOutline size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Private:</span>
						<span className="align-middle">
							&nbsp;Visible only for board members. Workspace administrators can moderate members in a private board or delete it.
						</span>
					</div>
				</li>

				<li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0 mt-0.5">
						<PiUsersThree size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Workspace public:</span>
						<span className="align-middle">
							&nbsp;Visible to anyone in the workspace as readonly (implicit board
							<span className="font-semibold text-purple-400"> Viewer</span>).
						</span>
					</div>
				</li>
			</ul>

      <div className="border-t border-gray-700 mb-4"></div> 

      <h3 className="text-lg font-semibold mb-3">Board Roles</h3>
      <ul className="space-y-1 text-gray-300 text-base">
        <li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0 mt-0.5">
						<MdOutlineLocalPolice size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Administrator:</span>
						<span className="align-middle">
							&nbsp;Full control, can edit or delete board, manages members and roles.
						</span>
					</div>
				</li>
        <li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0 mt-0.5">
						<LuWrench size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Editor:</span>
						<span className="align-middle">
							&nbsp;Can edit contents, but cannot delete the board.
						</span>
					</div>
				</li>
        <li className="flex items-start space-x-2">
					<div className="flex items-center shrink-0 mt-0.5">
						<RiSpyLine size={20} className="text-purple-400" />
					</div>

					<div className="leading-snug">
						<span className="font-semibold text-purple-400 relative top-[1px]">Viewer:</span>
						<span className="align-middle">
							&nbsp;Can display board in readonly mode.
						</span>
					</div>
				</li>
      </ul>
    </div>
  )
}

export default SettingsRulesTab
