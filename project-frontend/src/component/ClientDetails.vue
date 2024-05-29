<script setup>
import {reactive, watch} from "vue";
import {get} from "@/net";
import {useClipboard} from "@vueuse/core";
import {ElMessage} from "element-plus";
import {fitByUnit, percentageToStatus} from "../tools";

const props = defineProps({
  id: Number
})

const details = reactive({
  base: {},
  runtime: {},
})

const {copy} = useClipboard()
const copyIp =  () => copy(details.base.ip).then(() => ElMessage.success("成功复制到剪切板"))

watch(() => props.id, value=> {
  if(value !== -1) {
    details.base = {}
    get(`api/frontend-monitor/details?clientId=${value}`, data => {
      Object.assign(details.base, data)
      console.info(data)
    })

  }
}, {immediate: true})
</script>

<template>
  <div class="client-details" v-loading="Object.keys(details.base).length === 0">
    <div v-if="Object.keys(details.base).length">
      <div class="title">
        <i class="fa-solid fa-server"></i>
          服务器信息
      </div>
      <el-divider style="margin: 10px 0"/>
      <div class="details-list">
        <div>
          <span>服务器ID</span>
          <span>{{details.base.id}}</span>
        </div>
        <div>
          <span>服务器名称</span>
          <span>{{details.base.name}}</span>
        </div>
        <div>
          <span>运行状态</span>
          <span>
            {{details.base.online? "运行中" : "离线"}}
            <i style="color: #156b6b" class="fa-solid fa-check" v-if="details.base.online"></i>
            <i class="fa-solid fa-earth-europe" v-else></i>
          </span>
        </div>
        <div>
          <span>服务器节点</span>
          <span :class="`flag-icon flag-icon-${details.base.location}`"></span>
          <span>{{details.base.node}}</span>
        </div>
        <div>
          <span>公网IP地址</span>
          <span>
            {{details.base.ip}}
            <i class="fa-solid fa-copy interact-item " style="color: dodgerblue" @click.stop="copyIp(details.base.ip)"></i>
          </span>
        </div>
        <div>
          <span>处理器</span>
          <span>{{details.base.cpuName}}</span>
        </div>
        <div>
          <span>硬件配置信息</span>
          <span>
            <i class="fa-solid fa-microchip" style="margin-right: 5px"></i>
            <span style="margin-right: 10px">{{`${details.base.cpuCore} CPU`}}</span>
            <i class="fa-solid fa-memory" style="margin-right: 5px"></i>
            <span>{{`${details.base.memory.toFixed(1)} GB`}}</span>
          </span>
        </div>
        <div>
          <span>操作系统</span>
          <span>{{`${details.base.osName} ${details.base.osVersion}`}}</span>
        </div>
      </div> &nbsp;
      <div class="title">
        <i class="fa-solid fa-chart-line"></i>
        实时监控
      </div>
      <el-divider style="margin: 10px 0"/>
      <div style="display: flex">
        <el-progress type="dashboard" :width="100" :percentage="20" status="success">
          <div style="font-size: 17px; font-weight: bold;color: initial">CPU</div>
          <div style="font-size: 13px; color: grey;margin-top: 5px">20%</div>
        </el-progress>
        <el-progress style="margin-left: 20px"
                type="dashboard" :width="100" :percentage="60" status="success">
          <div style="font-size: 16px;font-weight: bold; color: initial">内存</div>
          <div style="font-size: 13px;color: grey;margin-top: 5px">28.6GB</div>
        </el-progress>
        <div style="flex: 1; margin-left: 30px;display: flex; flex-direction: column; height: 80px">
          <div style="flex: 1; font-size: 13px;">
            <div>实时网络速度</div>
            <div>
              <i style="color: orange" class="fa-solid fa-arrow-up"></i>
              <span>{{`${fitByUnit(details.base.networkUpload, 'KB')}`}}/s</span>
              <el-divider direction="vertical"/>
              <i style="color: orange" class="fa-solid fa-arrow-down"></i>
              <span>{{`${fitByUnit(details.base.networkDownload, 'KB')}`}}/s</span>
            </div>
          </div>
          <div style="font-size: 13px;display: flex;justify-content: space-between">
            <div>
              <i class="fa-solid fa-hard-drive"></i>
              <span>磁盘总容量</span>
            </div>
            <div>6.6GB / 40.0GB</div>
          </div>
          <el-progress type="line" :status="percentageToStatus(88)" :percentage="88" :show-text="false"/>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>


.client-details {
  height: 100%;
  padding: 20px;

  .title {
    color: #07668f;
    font-size: 18px;
    font-weight: bold;

  }

  .details-list {
    font-size: 14px;

    & div {
      margin-bottom: 10px;

      & span:first-child {
        color: gray;
        font-size: 13px;
        font-weight: normal;
        display: inline-block;
        width:  120px;
      }


    }
  }

}
</style>
