<template>
  <div class="min-h-screen py-8 px-4 sm:px-6 lg:px-8">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
          博客导航
        </h1>
        <p class="mt-2 text-lg text-slate-600">
          收藏和管理您喜爱的博客
        </p>
      </div>

      <!-- 收藏展示 -->
      <div v-if="bookmarks.length > 0" class="mb-12">
        <section class="space-y-10">
          <article
            v-for="item in bookmarks"
            :key="item.id"
            class="bookmark-entry"
          >
            <header class="entry-header flex flex-col gap-2 sm:flex-row sm:items-baseline sm:justify-between">
              <div class="flex items-start gap-3 flex-1">
                <!-- 图标显示 -->
                <div class="flex-shrink-0 mt-1">
                  <img
                    v-if="item.icon"
                    :src="'data:image/png;base64,' + item.icon"
                    :alt="item.title + ' 图标'"
                    @error="handleIconError($event, item.id)"
                    class="w-6 h-6 object-contain"
                  />

                  <div
                    v-else
                    class="w-6 h-6 bg-slate-200 rounded flex items-center justify-center"
                  >
                    <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                    </svg>
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <h3 class="text-2xl font-semibold text-slate-900">
                    <a
                      :href="item.url"
                      target="_blank"
                      rel="noopener"
                      class="hover:text-blue-600 transition-colors entry-link"
                    >
                      {{ item.title }}
                    </a>
                  </h3>
                  <p class="text-base leading-relaxed text-slate-700 break-all entry-url">
                    {{ item.description }}
                  </p>
                </div>
              </div>
              <button
                type="button"
                class="text-sm text-rose-500 hover:text-rose-600 transition-colors"
                @click="removeBookmark(item.id)"
              >
                删除
              </button>
            </header>
            <p v-if="item.description" class="text-sm text-slate-500 entry-body">
              {{ item.url }}
            </p>
            <p v-else class="text-sm text-slate-400 italic entry-body">
              暂无描述，点击标题访问详情。
            </p>
          </article>
        </section>
      </div>

      <!-- 空状态 -->
      <div v-else class="mb-12 text-center py-12">
        <p class="text-slate-500 text-lg">还没有收藏任何博客，添加一个开始吧！</p>
      </div>

      <!-- 添加收藏 -->
      <section class="space-y-6 mt-14">
        <article class="bookmark-entry">
          <header class="entry-header flex flex-col gap-2 sm:flex-row sm:items-baseline sm:justify-between">
            <div>
              <h2 class="text-2xl font-semibold text-slate-900">
                添加收藏
              </h2>
              <p class="text-sm text-slate-500">
                输入信息并保存到上方列表
              </p>
            </div>
          </header>
          <form @submit.prevent="add" class="entry-body space-y-5">
            <div>
              <label for="title" class="block text-sm font-medium text-slate-700 mb-2">
                标题
              </label>
              <input
                id="title"
                v-model="title"
                type="text"
                placeholder="输入博客标题"
                class="block w-full border-0 border-b border-slate-200 bg-transparent px-0 py-2 text-base focus:border-blue-500 focus:ring-0"
              />
            </div>
            <div>
              <label for="url" class="block text-sm font-medium text-slate-700 mb-2">
                网址
              </label>
              <input
                id="url"
                v-model="url"
                type="url"
                placeholder="https://example.com"
                class="block w-full border-0 border-b border-slate-200 bg-transparent px-0 py-2 text-base focus:border-blue-500 focus:ring-0"
              />
            </div>
            <div>
              <label for="description" class="block text-sm font-medium text-slate-700 mb-2">
                描述
              </label>
              <textarea
                id="description"
                v-model="description"
                placeholder="输入博客描述（可选）"
                rows="4"
                class="block w-full border border-slate-200 bg-transparent px-3 py-2 text-base rounded-md focus:border-blue-500 focus:ring-0"
              ></textarea>
            </div>
            <div class="pt-2 flex justify-end">
              <button
                type="submit"
                class="inline-flex items-center gap-2 text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors"
              >
                添加收藏
              </button>
            </div>
          </form>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getBookmarks, addBookmark, deleteBookmark, fetchIcon } from '../api/bookmarks';

const bookmarks = ref([]);
const title = ref('');
const url = ref('');
const description = ref('');
const loadingIcons = ref(new Set());

const load = async () => {
  const res = await getBookmarks();
  bookmarks.value = res.data;
  // 为没有图标的书签自动获取图标
  for (const bookmark of bookmarks.value) {
    if (!bookmark.iconUrl && bookmark.url) {
      loadIcon(bookmark.id);
    }
  }
};

const loadIcon = async (id) => {
  if (loadingIcons.value.has(id)) return;
  loadingIcons.value.add(id);
  try {
    await fetchIcon(id);
    // 重新加载列表以获取更新后的图标
    const res = await getBookmarks();
    bookmarks.value = res.data;
  } catch (e) {
    console.error('获取图标失败:', e);
  } finally {
    loadingIcons.value.delete(id);
  }
};

const handleIconError = (event, id) => {
  // 图标加载失败时，隐藏图片元素
  event.target.style.display = 'none';
  // 可选：尝试重新获取图标
  // loadIcon(id);
};

const add = async () => {
  if (!title.value || !url.value) return;
  const res = await addBookmark({
    title: title.value,
    url: url.value,
    description: description.value,
  });
  const newId = res.data;
  title.value = '';
  url.value = '';
  description.value = '';
  await load();
  // 为新添加的书签获取图标
  if (newId) {
    await loadIcon(newId);
  }
};

const removeBookmark = async (id) => {
  await deleteBookmark(id);
  await load();
};

onMounted(load);
</script>
