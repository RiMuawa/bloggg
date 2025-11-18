import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

export const getBookmarks = () => api.get('/bookmarks');
export const addBookmark = (data) => api.post('/bookmarks', data);
export const deleteBookmark = (id) => api.delete(`/bookmarks/${id}`);
export const markAsRead = (id) => api.post(`/bookmarks/${id}/read`);
