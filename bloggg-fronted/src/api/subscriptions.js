import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

export const getSubscriptions = () => api.get('/subscriptions');
export const addSubscription = (data) => api.post('/subscriptions', data);
export const deleteSubscription = (id) => api.delete(`/subscriptions/${id}`);
export const checkSubscriptionNow = (id) => api.post(`/subscriptions/${id}/check-now`);
