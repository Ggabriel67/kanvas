import React, { useEffect, useState, type ReactNode } from 'react'
import { createContext } from 'react'
import { authenticateUser, refreshToken, logoutUser, type AuthenticationRequest } from '../api/auth';
import { useNavigate } from 'react-router-dom';

export type User = {
  id: number;
  firstname: string;
  lastname: string;
  email: string;
	username: string;
  avatarColor: string;
};

type AuthContextType = {
  user: User | null;
	accessToken: string | null;
	login: (data: AuthenticationRequest) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

export const AuthContext = createContext<AuthContextType | null>(null);

const AuthProvider = ({ children }: { children: ReactNode }) => {
	const [user, setUser] = useState<User | null>(null);
	const [accessToken, setAccessToken] = useState<string | null>(localStorage.getItem("accessToken"));
  const [loading, setLoading] = useState<boolean>(true);
	const navigate = useNavigate();

  const login = async (data: AuthenticationRequest) => {
    try {
      const accessToken = await authenticateUser(data);
      setAccessToken(accessToken);
      localStorage.setItem("accessToken", accessToken);
    } catch (error: any) {
      throw error instanceof Error ? error : new Error(String(error));
    }
  }

	const logout = async () => {
    try {
      await logoutUser();
      setUser(null);
      setAccessToken(null);
      localStorage.removeItem("accessToken");
      navigate("/");
    } catch (error: any) {
      throw error instanceof Error ? error : new Error(String(error));
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const newToken = await refreshToken();
        setAccessToken(newToken);
        localStorage.setItem("accessToken", newToken);
      } catch {
        await logout();
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();

    const interval = setInterval(async () => {
      try {
        const newToken = await refreshToken();
        setAccessToken(newToken);
        localStorage.setItem("accessToken", newToken);
      } catch {
        (async () => await logout());
      }
    }, 14 * 60 * 1000 + 45 * 1000);
    
    return () => clearInterval(interval);
  }, []);

  return (
    <AuthContext.Provider value={{ user, accessToken, login, logout, loading }}>
			{children}
		</AuthContext.Provider>
  )
}

export default AuthProvider;
