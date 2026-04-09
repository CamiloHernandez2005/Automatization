import { apiContainer } from "@/axios";

// 🔹 Traer todos los contenedores
export const getContainers = async () => {
  const res = await apiContainer.get("/containers");
  return res.data;
};

// 🔹 Logs de un contenedor
export const getContainerLogs = async (name, tail = 100) => {
  const res = await apiContainer.get(`/containers/${name}/logs`, {
    params: { tail },
  });
  return res.data;
};

// 🔹 Ejecutar nuevo contenedor
export const runContainer = async ({ image, name, ports, env, cmd }) => {
  const res = await apiContainer.post("/containers/run", {
    image,
    name,
    ports,
    env,
    cmd,
  });
  return res.data;
};

// 🔹 Start — uno o varios
export const startContainers = async (names) => {
  const res = await apiContainer.post("/containers/start", {
    names: Array.isArray(names) ? names : [names],
  });
  return res.data;
};

// 🔹 Stop — uno o varios
export const stopContainers = async (names) => {
  const res = await apiContainer.post("/containers/stop", {
    names: Array.isArray(names) ? names : [names],
  });
  return res.data;
};

// 🔹 Restart — uno o varios
export const restartContainers = async (names) => {
  const res = await apiContainer.post("/containers/restart", {
    names: Array.isArray(names) ? names : [names],
  });
  return res.data;
};

// 🔹 Kill — uno o varios
export const killContainers = async (names) => {
  const res = await apiContainer.post("/containers/kill", {
    names: Array.isArray(names) ? names : [names],
  });
  return res.data;
};
