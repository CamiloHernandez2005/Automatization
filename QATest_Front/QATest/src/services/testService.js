import apiClient from "@/axios";

const resource = "/test/sp";

export default {
  SalesPortal(testRequest) {
    return apiClient.post(resource, testRequest);
  }
};
