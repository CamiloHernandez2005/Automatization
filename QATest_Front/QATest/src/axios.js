import axios from "axios";

export const apiClient = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

export const apiContainer = axios.create({
  baseURL: "http://192.168.2.64:5999",
  headers: {
    "Content-Type": "application/json",
  },
});
