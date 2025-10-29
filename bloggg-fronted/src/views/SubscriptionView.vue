<template>
  <div class="p-6 bg-gray-50 min-h-screen">
    <h1 class="text-2xl font-bold mb-4">订阅管理</h1>

    <div class="bg-white p-4 rounded-xl shadow mb-6">
      <h2 class="text-lg font-semibold mb-2">添加订阅</h2>
      <form @submit.prevent="add">
        <input v-model="url" placeholder="博客地址" class="border p-2 rounded mr-2 w-1/3" />
        <input v-model="email" placeholder="通知邮箱" class="border p-2 rounded mr-2 w-1/3" />
        <input v-model.number="period" type="number" placeholder="周期(小时)" class="border p-2 rounded mr-2 w-24" />
        <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded">添加订阅</button>
      </form>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div v-for="sub in subscriptions" :key="sub.id" class="bg-white p-4 rounded-xl shadow">
        <h3 class="text-lg font-semibold">{{ sub.url }}</h3>
        <p class="text-sm text-gray-500">通知邮箱：{{ sub.notify_email }}</p>
        <p class="text-sm text-gray-500">周期：{{ sub.period_hours }} 小时</p>
        <div class="flex items-center gap-3 mt-2">
          <button @click="remove(sub.id)" class="text-sm text-red-500">取消订阅</button>
          <button @click="checkNow(sub.id)" class="text-sm text-blue-600">立即检测</button>
        </div>
        <p v-if="results[sub.id]" class="text-xs mt-2" :class="results[sub.id].success ? 'text-green-600' : 'text-gray-600'">
          {{ results[sub.id].message }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getSubscriptions, addSubscription, deleteSubscription, checkSubscriptionNow } from '../api/subscriptions';

const subscriptions = ref([]);
const url = ref('');
const email = ref('');
const period = ref(24);
const results = ref({});

const load = async () => {
  const res = await getSubscriptions();
  subscriptions.value = res.data;
};

const add = async () => {
  await addSubscription({ url: url.value, notify_email: email.value, period_hours: period.value });
  url.value = '';
  email.value = '';
  await load();
};

const remove = async (id) => {
  await deleteSubscription(id);
  await load();
};

const checkNow = async (id) => {
  try {
    const res = await checkSubscriptionNow(id);
    results.value[id] = res.data;
    // 刷新列表，以反映 last_checked_at / has_update 等变化（如果前端展示了）
    await load();
  } catch (e) {
    results.value[id] = { success: false, message: '检测失败' };
  }
};

onMounted(load);
</script>
