<script setup>

import PreviewCard from "@/component/PreviewCard.vue";
</script>

<template>
  <div class="manage-main">
    <div class="title"><i class="fa-solid fa-server"></i>
      管理主机列表
    </div>
    <div class="desc">
      管理已注册服务器，实时监控服务器状态并管理
    </div>
    <el-divider style="margin: 10px 0">
    </el-divider>
    <div class="card-list">
      <preview-card/>
      <preview-card/>
      <preview-card/>
    </div>
  </div>
</template>

<style scoped>
.manage-main {
  margin: 0 50px;
  .title {
    font-size: 22px;
    font-weight: bold;
  }

  .desc {
    font-size: 15px;
    color: gray;
  }


}

.card-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
</style>