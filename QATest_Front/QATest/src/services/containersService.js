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

// 🔹 Stats de TODOS los contenedores en ejecución
export const getAllContainerStats = async () => {
  const res = await apiContainer.get("/containers/stats");
  return res.data;
};

// 🔹 Stats de un contenedor específico
export const getContainerStats = async (name) => {
  const res = await apiContainer.get(`/containers/${name}/stats`);
  return res.data;
};

// 🔹 Estado del server (memoria, disco, contenedores)
export const getServerInfo = async () => {
  const res = await apiContainer.get("/server");
  return res.data;
};

// 🔹 Ejecutar nuevo contenedor (wizard)
//   payload: { image, tag, name, port, cpus, memory,
//              env_file, logs_folder, container_log_path, pull }
export const runContainer = async (payload) => {
  const res = await apiContainer.post("/containers/run", payload);
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

// 🔹 Docker Hub: repos del usuario
export const getDockerHubRepositories = async (username) => {
  const res = await apiContainer.get("/dockerhub/repositories", {
    params: { username },
  });
  return res.data;
};

// 🔹 Docker Hub: tags de un repo
export const getDockerHubTags = async (username, repo) => {
  const res = await apiContainer.get(
    `/dockerhub/repositories/${repo}/tags`,
    { params: { username } }
  );
  return res.data;
};

// 🔹 Listar /opt/properties/
export const listProperties = async (path = "") => {
  const res = await apiContainer.get("/properties", { params: { path } });
  return res.data;
};

// 🔹 Listar /opt/logs/
export const listLogs = async (path = "") => {
  const res = await apiContainer.get("/logs", { params: { path } });
  return res.data;
};

// 🔹 Crear carpeta dentro de /opt/logs/
export const createLogFolder = async (path) => {
  const res = await apiContainer.post("/logs", { path });
  return res.data;
};
