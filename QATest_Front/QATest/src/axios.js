import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true, // 🔥 NECESARIO para cookies
  headers: {
    "Content-Type": "application/json",
  },
});

export default apiClient;
