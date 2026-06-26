<template>
  <div id="app">
    <router-view v-if="isLoginPage" />
    <div v-else class="app-container">
      <div class="sidebar-wrapper">
        <Sidebar />
      </div>
      <div class="main-wrapper">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'App',
  components: {
    Sidebar
  },
  computed: {
    isLoginPage() {
      return this.$route.path === '/login'
    }
  }
}
</script>

<style>
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
}

#app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  display: flex;
  height: 100%;
}

.sidebar-wrapper {
  width: 200px;
  flex-shrink: 0;
  height: 100%;
}

.main-wrapper {
  flex: 1;
  overflow-y: auto;
  background-color: #f0f2f5;
}
</style>
