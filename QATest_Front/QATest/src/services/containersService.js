import { apiContainer } from "@/axios";

// 🔹 traer todos
export const getContainers = async () => {
  const res = await apiContainer.get("/containers");
  return res.data;
};
