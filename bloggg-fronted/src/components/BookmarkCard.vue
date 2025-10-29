<template>
  <div class="bg-white p-4 rounded-xl shadow hover:shadow-lg transition relative">
    <!-- 标题与链接 -->
    <a
      :href="bookmark.url"
      target="_blank"
      class="text-lg font-semibold text-blue-600 hover:underline break-words"
    >
      {{ bookmark.title }}
    </a>

    <!-- 描述 -->
    <p v-if="bookmark.description" class="text-sm text-gray-600 mt-2 whitespace-pre-line break-words">
      {{ bookmark.description }}
    </p>

    <!-- 底部状态与操作 -->
    <div class="mt-3 flex justify-between items-center">
      <span
        v-if="bookmark.hasUpdate"
        class="text-xs text-red-500 font-semibold"
      >
        有更新！
      </span>
      <button
        @click="$emit('delete', bookmark.id)"
        class="text-sm text-gray-400 hover:text-red-600 transition"
      >
        删除
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  bookmark: Object
});
</script>
