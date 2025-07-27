<template>
  <div v-if="visible" class="global-loading">
    <div class="loading-backdrop" @click.stop></div>
    <div class="loading-content">
      <div class="loading-spinner">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
      </div>
      <div class="loading-text">{{ text }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: '加载中...'
  }
})

// 监听visible变化，控制body滚动
watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
</script>

<style lang="scss" scoped>
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;

  .loading-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(2px);
  }

  .loading-content {
    position: relative;
    background: white;
    border-radius: 8px;
    padding: 32px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    text-align: center;
    min-width: 200px;

    .loading-spinner {
      margin-bottom: 16px;

      .el-icon {
        font-size: 32px;
        color: #409eff;
      }
    }

    .loading-text {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .global-loading {
    .loading-content {
      margin: 0 20px;
      padding: 24px;
      min-width: auto;
      width: calc(100% - 40px);
      max-width: 300px;
    }
  }
}
</style>
