<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/home" class="brand">
          <span class="brand-logo"><Trophy /></span>
          <span class="brand-text">健身房会员中心</span>
        </router-link>

        <nav class="nav">
          <router-link
            v-for="item in navList"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            {{ item.title }}
          </router-link>
        </nav>

        <div class="header-right">
          <span class="balance">余额 <b>¥{{ Number(userStore.userInfo.balance || 0).toFixed(2) }}</b></span>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-chip">
              <span class="avatar">{{ userStore.displayName.slice(0, 1) }}</span>
              <span class="user-name">{{ userStore.displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">
      <span>健身房会员中心 · Gym Member Center</span>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navList = [
  { path: '/home', title: '首页' },
  { path: '/goods', title: '商品' },
  { path: '/seckill', title: '限时秒杀' },
  { path: '/course', title: '课程' },
  { path: '/food', title: '健身餐' },
  { path: '/member', title: '会员卡' },
  { path: '/orders', title: '我的订单' }
]

const isActive = (path) => route.path === path || (path === '/goods' && route.path.startsWith('/goods/'))

const userStoreRef = computed(() => userStore)

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
  } else {
    router.push('/' + command)
  }
}

onMounted(() => {
  if (!userStore.userInfo.id) {
    userStore.fetchUserInfo().catch(() => {})
  }
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--line);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 18px;
  white-space: nowrap;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff8a3d, #ff5a00);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo :deep(svg) {
  width: 20px;
  height: 20px;
}

.nav {
  display: flex;
  gap: 4px;
  flex: 1;
  overflow-x: auto;
}

.nav-item {
  padding: 8px 14px;
  border-radius: 8px;
  color: var(--ink-2);
  font-size: 14px;
  white-space: nowrap;
  transition: all 0.2s;
}

.nav-item:hover {
  color: var(--brand);
  background: var(--brand-light);
}

.nav-item.active {
  color: var(--brand);
  background: var(--brand-light);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;
}

.balance {
  font-size: 13px;
  color: var(--ink-3);
}

.balance b {
  color: var(--brand);
  font-size: 15px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-chip:hover {
  background: var(--bg);
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb27a, #ff6a00);
  color: #fff;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-name {
  font-size: 14px;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.main {
  flex: 1;
}

.footer {
  border-top: 1px solid var(--line);
  padding: 20px;
  text-align: center;
  color: var(--ink-3);
  font-size: 12px;
  background: #fff;
}

@media (max-width: 900px) {
  .balance {
    display: none;
  }
  .brand-text {
    display: none;
  }
  .header-inner {
    gap: 12px;
  }
}
</style>
