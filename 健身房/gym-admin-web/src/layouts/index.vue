<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo-area">
        <el-icon class="logo-icon"><Trophy /></el-icon>
        <span class="logo-title">健身房管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#c0c4cc"
        active-text-color="#409eff"
        class="side-menu"
      >
        <SidebarMenu :menus="menuStore.menuList" />
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-input
            v-model="searchText"
            placeholder="搜索..."
            prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatarUrl">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              <el-tag v-if="userStore.userInfo.role" size="small" type="primary" effect="light" class="role-tag">
                {{ roleText }}
              </el-tag>
              <el-icon class="caret-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { useMenuStore } from '@/store/modules/menu'
import { ElMessageBox, ElMessage } from 'element-plus'
import SidebarMenu from './components/SidebarMenu.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const searchText = ref('')

const activeMenu = computed(() => route.path)

const roleText = computed(() => {
  const map = { ADMIN: '管理员', COACH: '教练', USER: '用户' }
  return map[userStore.userInfo.role] || userStore.userInfo.role
})

function handleSearch() {
  ElMessage.info('搜索功能暂未开放：' + searchText.value)
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
      })
      .catch(() => {})
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  } else if (command === 'settings') {
    ElMessage.info('系统设置功能开发中')
  }
}

onMounted(() => {
  menuStore.generateMenus()
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100%;
}

.layout-aside {
  background-color: #001529;
  overflow-x: hidden;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 21, 41, 0.15);
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 22px;
}

.logo-title {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.side-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.layout-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 200px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background-color: #f5f7fa;
  }
}

.username {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  margin-left: 4px;
}

.caret-icon {
  color: #909399;
  font-size: 12px;
}

.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
