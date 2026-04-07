import apiClient from "@/axios";

export const login = (email, password) => {
  return apiClient.post("/api/auth/login", {
    email,
    password,
  });
};

export const verifySession = () => {
  return apiClient.get("/api/auth/verify");
};


export const logout = () => {
  return apiClient.post("/api/auth/logout");
};



