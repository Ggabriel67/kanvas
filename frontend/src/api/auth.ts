import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8222/api/v1/auth"
})

export type RegistrationRequest = {
  firstname: string;
  lastname: string;
  email: string;
  username: string;
  password: string;
};

export type AuthenticationRequest = {
  email: string;
  password: string;
}

export async function registerUser(data: RegistrationRequest) {
  try {
		const res = await api.post("/register", data);
		return res.data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data.error || "Registration failed");
    }
    throw error;
  }
}

export async function authenticateUser(data: AuthenticationRequest) {
  try {
    const response = await api.post<{accessToken: string;}>("/authenticate", data);
    return response.data.accessToken;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data.error || "Authentication failed");
    }
    throw error;
  }
}
