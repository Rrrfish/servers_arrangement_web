<script setup>

</script>

<template>
  <div style="width: 100vw; height: 100vh; overflow: hidden; display: flex">
    <div style="flex: 1" >
      <el-image style="width: 100%;height: 100%" fit="cover"
                src="https://w.wallhaven.cc/full/r7/wallhaven-r7vxk1.jpg"/>

    </div>
    <div class = "right-card">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.right-card {
  width: 450px;
  z-index: 1;
  background-color: var(--el-bg-color);
}
</style>