<script setup>

import PreviewCard from "@/component/PreviewCard.vue";
import {reactive, ref} from "vue";
import {get} from "@/net";
import ClientDetails from "@/component/ClientDetails.vue";
import RegisterCard from "@/component/RegisterCard.vue";
import {Plus} from "@element-plus/icons-vue";
import {useRoute} from "vue-router";
import TerminalWindow from "@/component/TerminalWindow.vue";
const list = ref([])
const route = useRoute()

const updateList = () => {
  if(route.name === 'manage') {
    get('/api/frontend-monitor/list', data => {
      list.value = data
    })
  }
}
setInterval(updateList, 10000)
updateList()

const detail = reactive({
  show: false,
  id: -1,
})
const displayClientDetails = (id) => {

  detail.show = true
  detail.id = id
}

const register = reactive({
  show: false,
  token: ''
})

const refreshToken = () => get('api/frontend-monitor/register', code => register.token = code)

function openTerminal(id) {
  terminal.show = true
  terminal.id = id
  detail.show = false  //把详情页面关了，不然太乱了
}

const terminal = reactive({
  show: false,
  token: -1
})
</script>

<template>
  <div class="manage-main">
    <div style="display: flex; justify-content: space-between; align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-server"></i>
          管理主机列表
        </div>
        <div class="desc">
          管理已注册服务器，实时监控服务器状态并管理
        </div>
      </div>
      <div>
        <el-button :icon="Plus" plain @click="register.show=true">添加新主机</el-button>
      </div>
    </div>

    <el-divider style="margin: 10px 0">
    </el-divider>
    <div class="card-list">
      <preview-card v-for="item in list" :data="item" :update="updateList"
                    @click="displayClientDetails(item.id)"/>
    </div>
    <el-drawer size="520" :show-close="false" v-model="detail.show"
               :with-header="false" v-if="list.length" @close="detail.id = -1">
      <client-details :id="detail.id" :update="updateList" @delete="updateList"
                      @terminal="openTerminal"/>
    </el-drawer>
    <el-drawer v-model="register.show" direction="ltr" style="width: 600px; margin-left: 15px"
      @open="refreshToken">
      <register-card :token="register.token"/>
    </el-drawer>
    <el-drawer style="width: 800px" :size="500" direction="btt"
               v-model="terminal.show" :close-on-click-modal="false">
      <template #header>
        <div>
          <div style="font-size: 18px;color: dodgerblue;font-weight: bold;">SSH远程连接</div>
          <div style="font-size: 14px">
            远程连接的建立将由服务端完成
          </div>
        </div>
      </template>
      <terminal-window :id="terminal.id">

      </terminal-window>
    </el-drawer>
  </div>
</template>

<style scoped>
:deep(.el-drawer) {
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

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