import { createRouter, createWebHistory } from 'vue-router';
import HomeView from './views/HomeView.vue';
import SubscriptionView from './views/SubscriptionView.vue';

const routes = [
  { path: '/', name: 'Home', component: HomeView },
  { path: '/subscriptions', name: 'Subscriptions', component: SubscriptionView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
