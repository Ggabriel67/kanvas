import React from "react";

interface LoadingSpinnerProps {
  size?: "sm" | "md" | "lg";
  message?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ size = "md",message }) => {
  const sizeClasses: Record<"sm" | "md" | "lg", string> = {
    sm: "h-5 w-5 border-2",
    md: "h-10 w-10 border-4",
    lg: "h-16 w-16 border-[6px]",
  };

  return (
    <div className="flex flex-col items-center justify-center w-full h-full py-10">
      <div
        className={`${sizeClasses[size]} border-gray-300 border-t-purple-600 rounded-full animate-spin`}
      />
      {message && (
        <p className="mt-4 text-gray-400 text-sm">{message}</p>
      )}
    </div>
  );
};

export default LoadingSpinner;
