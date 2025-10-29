<template>
  <div class="min-h-screen bg-gray-50 text-gray-800">
    <!-- 顶部导航栏 -->
    <nav class="bg-white shadow mb-6">
      <div class="container mx-auto flex justify-between items-center px-6 py-3">
        <h1 class="text-xl font-bold text-blue-600">Bloggg 博客管管</h1>
        <ul class="flex space-x-4">
          <li>
            <RouterLink
              to="/"
              class="px-3 py-2 rounded hover:bg-blue-100"
              :class="{ 'bg-blue-500 text-white': $route.path === '/' }"
            >
              博客导航
            </RouterLink>
          </li>
          <li>
            <RouterLink
              to="/subscriptions"
              class="px-3 py-2 rounded hover:bg-blue-100"
              :class="{ 'bg-blue-500 text-white': $route.path === '/subscriptions' }"
            >
              订阅管理
            </RouterLink>
          </li>
        </ul>
      </div>
    </nav>

    <!-- 页面内容 -->
    <main class="container mx-auto px-6">
      <RouterView />
    </main>

    <!-- 页脚 -->
    <footer class="text-center text-gray-400 py-6 mt-10 border-t">
      <p>© 2025 BlogGG - 博客收藏与订阅系统</p>
    </footer>
  </div>
</template>

<script setup>
// 无需逻辑
</script>

<style>
/* 额外的全局样式（可选） */
body {
  font-family: 'Inter', 'Noto Sans SC', system-ui, sans-serif;
}
</style>
