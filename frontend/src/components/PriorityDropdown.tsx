import React, { useEffect, useRef, useState } from 'react'
import { IoMdArrowDropdown } from "react-icons/io";
import { FaFlag } from "react-icons/fa";

interface PriorityDropdownProps {
  value: "HIGH" | "MEDIUM" | "LOW" | null;
  readonly?: boolean;
  onChange: (newPriority: "HIGH" | "MEDIUM" | "LOW" | null) => void;
}

const PriorityDropdown: React.FC<PriorityDropdownProps> = ({ value, readonly, onChange }) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const labelMap: Record<string, string> = {
    HIGH: "High",
    MEDIUM: "Medium",
    LOW: "Low",
  };

  const colorMap: Record<string, string> = {
    HIGH: "bg-red-500",
    MEDIUM: "bg-yellow-400",
    LOW: "bg-green-500",
  };

   useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        disabled={readonly}
        onClick={() => !readonly && setIsOpen((prev) => !prev)}
        className={`flex items-center justify-between w-45 px-3 py-2 rounded-md text-gray-200 bg-[#2b2b2b] border focus:ring-2 focus:ring-purple-500 border-gray-600 hover:bg-[#333333] ${
          readonly ? "opacity-50 cursor-default" : "cursor-pointer"
        }`}
      >
        <div className="flex items-center space-x-2">
          {value ? (
            <div className={`w-3 h-3 rounded-sm ${colorMap[value]}`}></div>
          ) : (
            <FaFlag className="text-gray-400" size={14} />
          )}
          <span>{value ? labelMap[value] : "No priority"}</span>
        </div>
        {!readonly && <IoMdArrowDropdown size={18} />}
      </button>

      {/* Dropdown Menu */}
      {isOpen && !readonly && (
        <div className="absolute left-0 mt-2 w-45 bg-[#151515] border border-gray-600 rounded-lg shadow-lg z-50">
          <ul className="py-1 text-gray-200">
            <li
              className="flex items-center px-3 py-2 hover:bg-[#2b2b2b] cursor-pointer space-x-2"
              onClick={() => {
                setIsOpen(false);
                onChange(null);
              }}
            >
              <div className="w-3 h-3 rounded-sm bg-gray-500"></div>
              <span>No priority</span>
            </li>

            <li
              className="flex items-center px-3 py-2 hover:bg-[#2b2b2b] cursor-pointer space-x-2"
              onClick={() => {
                setIsOpen(false);
                onChange("LOW");
              }}
            >
              <div className="w-3 h-3 rounded-sm bg-green-500"></div>
              <span>Low</span>
            </li>

            <li
              className="flex items-center px-3 py-2 hover:bg-[#2b2b2b] cursor-pointer space-x-2"
              onClick={() => {
                setIsOpen(false);
                onChange("MEDIUM");
              }}
            >
              <div className="w-3 h-3 rounded-sm bg-yellow-400"></div>
              <span>Medium</span>
            </li>

            <li
              className="flex items-center px-3 py-2 hover:bg-[#2b2b2b] cursor-pointer space-x-2"
              onClick={() => {
                setIsOpen(false);
                onChange("HIGH");
              }}
            >
              <div className="w-3 h-3 rounded-sm bg-red-500"></div>
              <span>High</span>
            </li>
          </ul>
        </div>
      )}
    </div>
  );
}

export default PriorityDropdown;
