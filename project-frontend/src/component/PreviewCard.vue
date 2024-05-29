<script setup>
import {useClipboard} from "@vueuse/core";

const props = defineProps({
  data: Object,
  update: Function,
})

import {fitByUnit} from '@/tools'
import {ElMessage, ElMessageBox} from "element-plus";
import {post} from "@/net";

const {copy} = useClipboard()
const copyIp =  () => copy(props.data.ip).then(() => ElMessage.success("成功复制到剪切板"))

function rename() {
  ElMessageBox.prompt("请输入新的服务器名称", "修改名称", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    inputValue: props.data.name,
    inputPattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]{1,10}$/,
    inputErrorMessage: '名称只能包含中英文字符、数字和下划线',

  }).then(({ value }) => post('/api/frontend-monitor/rename', {
        id: props.data.id,
        name: value
      }, () => {
        ElMessage.success('主机名称已更新')
        props.update()
      })
  )
}

</script>

<template>
  <div class="instance-card">
    <div style="display: flex;justify-content: space-between">
      <div>
        <div class="name">
          <span :class="`flag-icon flag-icon-${data.location}`"></span>
          <span style="margin: 0 5px " >{{data.name}}</span>
          <i class="fa-solid fa-pen-to-square interact-item " @click.stop="rename"></i>
        </div>
        <div class="os">
          os: {{`${data.osName} ${data.osVersion}`}}
        </div>
      </div>
      <div class="status" v-if="data.online">
        <span style="margin: 0 5px">运行中</span>
        <i style="color: #156b6b" class="fa-solid fa-play"></i>
      </div>
      <div class="status" v-else>
        <span style="margin: 0 5px; color: #a10501">离线</span>
        <i class="fa-solid fa-earth-europe"></i>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div class="network" >
      <span style="margin-right: 10px">公网IP：{{data.ip}}</span>
      <i class="fa-solid fa-copy interact-item" @click.stop="copyIp" style="color: cornflowerblue"></i>
    </div>
    <div class="cpu">
      <span style="margin-right: 10px">处理器：{{data.cpuName}}</span>
    </div>
    <div class="hardware">
      <i class="fa-solid fa-microchip" style="margin-right: 5px"></i>
      <span style="margin-right: 10px">{{`${data.cpuCore} CPU`}}</span>
      <i class="fa-solid fa-memory" style="margin-right: 5px"></i>
      <span>{{`${data.memory.toFixed(1)} GB`}}</span>
    </div>
    <div class="progress">
      <span>{{`CPU: ${(data.cpuUsage*100).toFixed(1)}%`}}</span>
      <el-progress status="success" :percentage="data.cpuUsage*100" :stroke-width="5" :show-text="false"/>
    </div>
    <div class="progress">
      <span>Memory: <b>{{data.memoryUsage.toFixed(2)}}</b> GB</span>
      <el-progress  status="success" :percentage="1.2/4*100" :stroke-width="5" :show-text="false"/>
    </div>
    <div class="network-flow">
      <div>网络流量</div>
      <div>
        <i class="fa-solid fa-upload" style="margin-right: 5px"></i>
        <span>{{`${fitByUnit(data.networkUpload, 'KB')}`}}/s</span>
        <el-divider direction="vertical"/>
        <i class="fa-solid fa-download" style="margin-right: 5px"></i>
        <span>{{`${fitByUnit(data.networkDownload, 'KB')}`}}/s</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-progress-bar__outer) {
  background-color: #156b6b22;
}

:deep(.el-progress-bar__inner) {
  background-color: #156b6b;
}

.interact-item {
  transition: .3s;

  &:hover {
    cursor: pointer;
    scale: 1.1;
    opacity: 0.8;
  }
}

.dark .instance-card {
  color: #d9d9d9;
}

.instance-card {
  width: 320px;
  padding: 15px;
  background-color: var(--el-bg-color);
  border-radius: 5px;
  box-sizing: border-box;
  color: #2d2c2c;

  .name {
    font-size: 15px;
    font-weight: bold;
  }

  .status {
    font-size: 14px;
  }

  .os {
    font-size: 13px;
    color: gray;
  }

  .network {
    font-size: 13px;
  }

  .cpu {
    font-size: 13px;
  }

  .hardware {
    font-size: 13px;
  }

  .progress {
    margin: 10px 0 ;
    font-size: 12px;
  }

  .network-flow {
    margin-top: 10px;
    font-size: 11px;
    display: flex;
    justify-content: space-between;

  }
}
</style>