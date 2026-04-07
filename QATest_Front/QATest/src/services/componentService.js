import apiClient from "@/axios";

const resource = "/components";

export default {
  create(componentRequest) {
    return apiClient.post(resource, componentRequest);
  },

  getAll() {
    return apiClient.get(resource);
  },

  getById(id) {
    return apiClient.get(`${resource}/${id}`);
  },

  update(id, componentRequest) {
    return apiClient.put(`${resource}/${id}`, componentRequest);
  },

  delete(id) {
    return apiClient.delete(`${resource}/${id}`);
  }
};
