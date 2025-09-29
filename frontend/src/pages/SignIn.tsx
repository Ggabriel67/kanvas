import React from 'react'
import { useForm, type SubmitHandler } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { MdLockOutline } from "react-icons/md";
import useAuth from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';

type FormFields = {
  email: string;
  password: string;
};

const SignIn = () => {
  const { 
    register, 
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormFields>();

  const auth = useAuth();
  const navigate = useNavigate();

  const onSubmit: SubmitHandler<FormFields> = async (data) => {  
    try {
      await auth.login(data);
      navigate("/home", { replace: true });
    } catch (error: any) {
      setError("root", { message: error.message });
    }
  };

  useEffect(() => {
    if (auth.accessToken) {
      navigate("/home", { replace: true });
    }
  }, [auth.accessToken, navigate]);
  
  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="bg-[#1e1e1e] text-gray-100 p-10 rounded-lg shadow-lg w-[25vw] max-w-3xl space-y-5"
    >
      <div className="flex items-center justify-center">
        <MdLockOutline size={30} />
      </div>
      <h1 className="text-2xl font-semibold text-center">Sign in</h1>

      {/* Email */}
      <div>
          <input
            {...register("email", {
              required: "Email address is required",
              validate: (value) =>
                value.includes("@") || "Incorrect email address",
            })}
            type="text"
            placeholder="Email Address *"
            className="w-full p-3 rounded bg-[#2a2a2a] border border-gray-600 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
          {errors.email && (
            <div className="text-red-500 text-sm mt-1">{errors.email.message}</div>
          )}
        </div>

      {/* Password */}
      <div>
        <input
          {...register("password", {
            required: "Password is required",
            minLength: {
              value: 8,
              message: "Password must have at least 8 characters",
            },
          })}
          type="password"
          placeholder="Password *"
          className="w-full p-3 rounded bg-[#2a2a2a] border border-gray-600 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500"
        />
        {errors.password && (
          <div className="text-red-500 text-sm mt-1">{errors.password.message}</div>
        )}
      </div>
      <button
        disabled={isSubmitting}
        type="submit"
        className="w-full bg-purple-700 text-white font-semibold p-3 rounded hover:bg-purple-800 disabled:bg-gray-500 transition-colors"
      >
        {isSubmitting ? "Submitting..." : "SIGN IN"}
      </button>
      
      <div className="text-right text-sm underline text-blue-400 hover:text-blue-600 visited:text-blue-700">
        <Link to={"/signup"}>Don't have an account? Sign up</Link>
      </div>
      
      {errors.root && (
        <div className="text-red-500 text-sm text-center mt-2">{errors.root.message}</div>
      )}
    </form>
  )
}

export default SignIn;
