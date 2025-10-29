<template>
  <div class="p-6 bg-gray-50 min-h-screen">
    <h1 class="text-2xl font-bold mb-4">博客导航</h1>

    <!-- 收藏展示 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <BookmarkCard
        v-for="item in bookmarks"
        :key="item.id"
        :bookmark="item"
        @delete="removeBookmark"
      />
    </div>

    <!-- 添加收藏 -->
    <div class="mt-6 bg-white p-4 rounded-xl shadow">
      <h2 class="text-lg font-semibold mb-2">添加收藏</h2>
      <form @submit.prevent="add" class="space-y-3">
        <div>
          <input
            v-model="title"
            placeholder="标题"
            class="border p-2 rounded w-full"
          />
        </div>
        <div>
          <input
            v-model="url"
            placeholder="网址"
            class="border p-2 rounded w-full"
          />
        </div>
        <div>
          <textarea
            v-model="description"
            placeholder="描述"
            class="border p-2 rounded w-full"
            rows="3"
          ></textarea>
        </div>
        <button
          type="submit"
          class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          添加
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import BookmarkCard from '../components/BookmarkCard.vue';
import { getBookmarks, addBookmark, deleteBookmark } from '../api/bookmarks';

const bookmarks = ref([]);
const title = ref('');
const url = ref('');
const description = ref('');

const load = async () => {
  const res = await getBookmarks();
  bookmarks.value = res.data;
};

const add = async () => {
  if (!title.value || !url.value) return;
  await addBookmark({
    title: title.value,
    url: url.value,
    description: description.value,
  });
  title.value = '';
  url.value = '';
  description.value = '';
  await load();
};

const removeBookmark = async (id) => {
  await deleteBookmark(id);
  await load();
};

onMounted(load);
</script>
