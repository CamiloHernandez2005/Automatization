import apiClient from "@/axios";

const resource = "/regions";

export default {
  create(regionRequest) {
    return apiClient.post(resource, regionRequest);
  },

  getAll() {
    return apiClient.get(resource);
  },

  getById(id) {
    return apiClient.get(`${resource}/${id}`);
  },

  update(id, regionRequest) {
    return apiClient.put(`${resource}/${id}`, regionRequest);
  },

  delete(id) {
    return apiClient.delete(`${resource}/${id}`);
  }
};
