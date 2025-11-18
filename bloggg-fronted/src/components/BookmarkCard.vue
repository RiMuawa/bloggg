<template>
  <article class="group relative flex flex-col bg-slate-50 rounded-xl border p-5 transition-all duration-200" style="border-color: #e5e7eb;">
    <!-- 标题与链接 -->
    <div class="flex-1 mb-3">
      <h3 class="text-lg font-semibold text-slate-900 mb-2">
        <a
          :href="bookmark.url"
          target="_blank"
          rel="noopener noreferrer"
          class="hover:text-blue-600 transition-colors break-words"
        >
          {{ bookmark.title }}
        </a>
      </h3>
      
      <!-- 描述 -->
      <p v-if="bookmark.description" class="text-sm text-slate-600 leading-relaxed whitespace-pre-line break-words line-clamp-3">
        {{ bookmark.description }}
      </p>
    </div>

    <!-- 底部状态与操作 -->
    <div class="flex items-center justify-between pt-4 border-t" style="border-color: #e5e7eb;">
      <div>
        <span
          v-if="bookmark.hasUpdate"
          class="inline-flex items-center rounded-full bg-red-50 px-2.5 py-0.5 text-xs font-medium text-red-700"
        >
          有更新
        </span>
      </div>
      <button
        @click="$emit('delete', bookmark.id)"
        class="text-sm font-medium bg-transparent border rounded-lg px-3 py-1.5 text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-500"
        style="border-color: #e5e7eb;"
      >
        删除
      </button>
    </div>
  </article>
</template>

<script setup>
defineProps({
  bookmark: Object
});
</script>
