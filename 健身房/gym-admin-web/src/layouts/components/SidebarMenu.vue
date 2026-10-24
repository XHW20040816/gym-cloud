<template>
  <template v-for="menu in menus" :key="menu.path">
    <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
      <template #title>
        <el-icon v-if="menu.icon">
          <component :is="menu.icon" />
        </el-icon>
        <span>{{ menu.title }}</span>
      </template>
      <SidebarMenu :menus="menu.children" />
    </el-sub-menu>
    <el-menu-item v-else :index="menu.path">
      <el-icon v-if="menu.icon">
        <component :is="menu.icon" />
      </el-icon>
      <template #title>{{ menu.title }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
defineProps({
  menus: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.el-menu {
  border-right: none;
}
</style>
