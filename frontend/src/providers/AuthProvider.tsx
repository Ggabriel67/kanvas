import React, { useEffect, useState, type ReactNode } from 'react'
import { createContext } from 'react'
import { authenticateUser, refreshToken, logoutUser, type AuthenticationRequest } from '../api/auth';
import { getUser } from '../api/users';
import { jwtDecode } from 'jwt-decode';

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

type DecodedToken = { exp: number };

export const AuthContext = createContext<AuthContextType | null>(null);

const AuthProvider = ({ children }: { children: ReactNode }) => {
	const [user, setUser] = useState<User | null>(null);
	const [accessToken, setAccessToken] = useState<string | null>(localStorage.getItem("accessToken"));
  const [loading, setLoading] = useState<boolean>(true);

  const login = async (data: AuthenticationRequest) => {
    try {
      const accessToken = await authenticateUser(data);
      setAccessToken(accessToken);
      localStorage.setItem("accessToken", accessToken);
      const user: User = await getUser();
      setUser(user);
    } catch (error: any) {
      throw error instanceof Error ? error : new Error(String(error));
    }
  }

	const logout = async () => {
    try {
      await logoutUser();
    } catch (error: any) {
      throw error instanceof Error ? error : new Error(String(error));
    } finally {
      setUser(null);
      setAccessToken(null);
      localStorage.removeItem("accessToken");
      setLoading(false);
    }
  };

  const scheduleRefresh = (token: string) => {
    const decoded: DecodedToken = jwtDecode(token);
    const expiresAt = decoded.exp * 1000;
    const now = Date.now();

    const refreshDelay = Math.max(expiresAt - now - 15_000, 0);

    return setTimeout(async () => {
      try {
        const newToken = await refreshToken();
        setAccessToken(newToken);
        localStorage.setItem("accessToken", newToken);

        scheduleRefresh(newToken);
      } catch (err) {
        console.error("Refresh failed", err);
        await logout();
      }
    }, refreshDelay);
  }

  useEffect(() => {
    if (!accessToken) {
      setLoading(false);
      return;
    }

    const initializeAuth = async () => {
      try {
        const user: User = await getUser();
        setUser(user);
      } catch {
        await logout();
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();

    const timeoutId = scheduleRefresh(accessToken);

    return () => clearTimeout(timeoutId);
  }, [accessToken]);

  return (
    <AuthContext.Provider value={{ user, accessToken, login, logout, loading }}>
			{children}
		</AuthContext.Provider>
  )
}

export default AuthProvider;
