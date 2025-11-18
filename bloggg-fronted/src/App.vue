<template>
  <div class="min-h-screen" :style="backgroundStyle">
    <!-- 顶部导航栏 -->
    <nav>
      <div class="container mx-auto flex justify-between items-center px-6 py-3">
        <h1 class="text-xl font-bold">Bloggg 博客管管</h1>
        <ul class="flex space-x-4">
          <li>
            <RouterLink to="/">
              博客导航
            </RouterLink>
          </li>
          <li>
            <RouterLink to="/subscriptions">
              订阅管理
            </RouterLink>
          </li>
        </ul>
      </div>
    </nav>

    <!-- 页面内容 -->
    <main>
      <RouterView />
    </main>

    <!-- 页脚 -->
    <footer>
      <p>© 2025 BlogGG - 博客收藏与订阅系统</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import backgroundImage from './assets/clean-gray-paper.png';

const backgroundStyle = computed(() => ({
  backgroundColor: '#f9f4dc',
  backgroundImage: `url(${backgroundImage})`,
  backgroundRepeat: 'repeat',
  backgroundSize: 'auto'
}));
</script>

<style>
/* 额外的全局样式（可选） */
body {
  font-family: 'Inter', 'Noto Sans SC', system-ui, sans-serif;
}
</style>
