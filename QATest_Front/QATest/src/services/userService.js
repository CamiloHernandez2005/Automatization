import apiClient from "@/axios";

const resource = "/api/users";

export default {
  create(userRequest) {
    return apiClient.post(resource, userRequest);
  },

  getAll() {
    return apiClient.get(resource);
  },

  getById(id) {
    return apiClient.get(`${resource}/${id}`);
  },

  update(id, userRequest) {
    return apiClient.put(`${resource}/${id}`, userRequest);
  },

  delete(id) {
    return apiClient.delete(`${resource}/${id}`);
  }
};
