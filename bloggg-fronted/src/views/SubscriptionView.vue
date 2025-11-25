<template>
  <div class="min-h-screen py-8 px-4 sm:px-6 lg:px-8">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
          订阅管理
        </h1>
        <p class="mt-2 text-lg text-slate-600">
          订阅博客并接收更新通知
        </p>
      </div>

      <!-- 订阅列表 -->
      <div v-if="subscriptions.length > 0">
        <section class="space-y-10">
          <article
            v-for="sub in subscriptions"
            :key="sub.id"
            class="bookmark-entry"
          >
            <header
              class="entry-header flex flex-col gap-2 sm:flex-row sm:items-baseline sm:justify-between"
            >
              <div class="flex items-start gap-3 flex-1">
                <!-- 图标显示 -->
                <div class="flex-shrink-0 mt-1">
                  <img
                    v-if="getIconSrc(sub)"
                    :src="getIconSrc(sub)"
                    :alt="sub.url + ' 图标'"
                    @error="handleIconError($event)"
                    class="w-6 h-6 object-contain"
                  />
                  <div
                    v-else
                    class="w-6 h-6 bg-slate-200 rounded flex items-center justify-center"
                  >
                    <svg
                      class="w-4 h-4 text-slate-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                      />
                    </svg>
                  </div>
                </div>

                <div class="flex-1 min-w-0">
                  <h3 class="text-2xl font-semibold text-slate-900 break-words">
                    {{ sub.url }}
                  </h3>
                  <p class="text-sm text-slate-500">
                    每 {{ sub.period_hours }} 小时检测一次 ·<br />通知邮箱:
                    {{ sub.notify_email }}
                  </p>
                </div>
              </div>

              <div class="flex flex-wrap gap-3">
                <button
                  type="button"
                  class="text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors"
                  @click="checkNow(sub.id)"
                >
                  立即检测
                </button>
                <button
                  type="button"
                  class="text-sm font-medium text-rose-500 hover:text-rose-600 transition-colors"
                  @click="remove(sub.id)"
                >
                  取消订阅
                </button>
              </div>
            </header>

            <dl class="entry-body text-sm text-slate-600 space-y-2">
              <div v-if="results[sub.id]">
                <p :class="results[sub.id].success ? 'text-green-600' : 'text-red-600'">
                  {{ results[sub.id].message }}
                </p>
              </div>
              <div v-else class="text-slate-400">
                最近暂无检测结果
              </div>
            </dl>
          </article>
        </section>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-12 text-slate-500">
        <p class="text-lg">还没有订阅任何博客，添加一个开始吧！</p>
      </div>
    </div>

    <!-- 添加订阅表单 -->
    <section class="space-y-6 mb-12">
      <article class="bookmark-entry">
        <header
          class="entry-header flex flex-col gap-2 sm:flex-row sm:items-baseline sm:justify-between"
        >
          <div>
            <h2 class="text-2xl font-semibold text-slate-900">添加订阅</h2>
            <p class="text-sm text-slate-500">
              填写订阅信息，系统将按周期检测更新
            </p>
          </div>
        </header>

        <form @submit.prevent="add" class="entry-body space-y-5">
          <div>
            <label for="url" class="block text-sm font-medium text-slate-700 mb-2">
              博客地址
            </label>
            <input
              id="url"
              v-model="url"
              type="url"
              placeholder="https://example.com"
              class="block w-full border-0 border-b border-slate-200 bg-transparent px-0 py-2 text-base focus:border-blue-500 focus:ring-0"
            />
          </div>

          <div class="grid gap-5 sm:grid-cols-2">
            <div>
              <label for="email" class="block text-sm font-medium text-slate-700 mb-2">
                通知邮箱
              </label>
              <input
                id="email"
                v-model="email"
                type="email"
                placeholder="your@email.com"
                class="block w-full border-0 border-b border-slate-200 bg-transparent px-0 py-2 text-base focus:border-blue-500 focus:ring-0"
              />
            </div>

            <div>
              <label for="period" class="block text-sm font-medium text-slate-700 mb-2">
                检测周期（小时）
              </label>
              <input
                id="period"
                v-model.number="period"
                type="number"
                min="1"
                placeholder="24"
                class="block w-full border-0 border-b border-slate-200 bg-transparent px-0 py-2 text-base focus:border-blue-500 focus:ring-0"
              />
            </div>
          </div>

          <div class="pt-2 flex justify-end">
            <button
              type="submit"
              class="inline-flex items-center gap-2 text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors"
            >
              添加订阅
            </button>
          </div>
        </form>
      </article>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import {
  getSubscriptions,
  addSubscription,
  deleteSubscription,
  checkSubscriptionNow,
} from '../api/subscriptions';

const subscriptions = ref([]);
const url = ref('');
const email = ref('');
const period = ref(24);
const results = ref({});

const load = async () => {
  const subsRes = await getSubscriptions();
  subscriptions.value = subsRes.data;
};

// ✅ 使用 Base64 图标
const getIconSrc = (sub) => {
  return sub.icon ? 'data:image/png;base64,' + sub.icon : null;
};

// ✅ 图标加载失败处理
const handleIconError = (event) => {
  console.error('图标加载失败:', event.target.src);
  event.target.style.display = 'none';
};

const add = async () => {
  await addSubscription({
    url: url.value,
    notify_email: email.value,
    period_hours: period.value,
  });
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
    await load();
  } catch (e) {
    results.value[id] = { success: false, message: '检测失败' };
  }
};

onMounted(load);
</script>
