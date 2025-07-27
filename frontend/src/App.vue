<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 应用启动时检查登录状态
  userStore.checkLoginStatus()
})
</script>

<style lang="scss">
#app {
  height: 100vh;
  width: 100vw;
}

// 全局滚动条样式
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
  
  &:hover {
    background: #a8a8a8;
  }
}

// 暗色主题滚动条
.dark {
  ::-webkit-scrollbar-track {
    background: #2c2c2c;
  }
  
  ::-webkit-scrollbar-thumb {
    background: #6c6c6c;
    
    &:hover {
      background: #8c8c8c;
    }
  }
}
</style>
