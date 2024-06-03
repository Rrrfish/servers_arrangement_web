<script setup>
import {computed, reactive, watch} from "vue";
import {get, post} from "@/net";
import {useClipboard} from "@vueuse/core";
import {ElMessage, ElMessageBox} from "element-plus";
import {fitByUnit, percentageToStatus} from "@/tools";
import {Delete} from "@element-plus/icons-vue";

const locations = [
  {name: 'cn', desc: '中国大陆'},
  {name: 'hk', desc: '香港'},
  {name: 'jp', desc: '日本'},
  {name: 'us', desc: '美国'},
  {name: 'sg', desc: '新加坡'},
  {name: 'kr', desc: '韩国'},
  {name: 'de', desc: '德国'}
]

const props = defineProps({
  id: Number,
  update: Function,
})

const details = reactive({
  base: {},
  runtime: {list: []},
  editNode: false,
})

const nodeEdit = reactive({
  name: '',
  locations: '',
})

const enableEditNode = () => {
  details.editNode = true
  nodeEdit.name = details.base.node
  nodeEdit.locations = details.base.location
}

const emits = defineEmits(['delete']) //通知外面

function deleteClient() {
  ElMessageBox.confirm("删除后，所有统计数据都将消失，您确定要这样做吗？",
    "删除主机", {
    confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
  }).then(() => {
    get(`api/frontend-monitor/delete?clientId=${props.id}`, () => {
      emits("delete")
      props.update()
      ElMessage.success("服务器删除成功")
    })
  }).catch()
}

function updateDetails() {
  props.update()
  init(props.id)
}

const init = id => {
  if(id !== -1) {
    details.base = {}
    details.runtime = {list: []}
    get(`api/frontend-monitor/runtime-history?clientId=${id}`, data => Object.assign(details.runtime, data))
    get(`api/frontend-monitor/details?clientId=${id}`, data => Object.assign(details.base, data))
    console.info(details.runtime)
  }
}

const submitNodeEdit = () => {
  post('api/frontend-monitor/renameNode', {
    id: props.id,
    node: nodeEdit.name,
    location: nodeEdit.locations,
  }, () => {
    console.info(nodeEdit.name, nodeEdit.locations)
    details.editNode = false
    updateDetails()
    ElMessage("节点已更新")
  })

}

const {copy} = useClipboard()
const copyIp =  () => copy(details.base.ip).then(() => ElMessage.success("成功复制到剪切板"))


watch(() => props.id, init, { immediate: true })

setInterval( () => {
  if(props.id !== -1 && details.runtime) {
    get(`/api/frontend-monitor/runtime-now?clientId=${props.id}`, data => {
      if(details.runtime.list.length >= 360)
        details.runtime.list.splice(0, 1)   //删除开头的数据
      details.runtime.list.push(data)   //加到后面
    })
  }
}, 10000)

const now = computed(() => details.runtime.list[details.runtime.list.length -1 ])

</script>

<template>
  <div class="client-details" v-loading="Object.keys(details.base).length === 0">
    <div v-if="Object.keys(details.base).length" >
      <div style="display: flex; justify-content: space-between" >
        <div class="title">
          <i class="fa-solid fa-server"></i>
          服务器信息
        </div>
        <el-button :icon="Delete" type="danger" text @click="deleteClient">删除此主机</el-button>
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
          </span>
        </div>
        <div v-if="!details.editNode">
          <span>服务器节点</span>
          <span :class="`flag-icon flag-icon-${details.base.location}`"></span>
          <span>{{details.base.node}}</span>
          <i class="fa-solid fa-pen-to-square interact-item " @click.stop="enableEditNode"></i>
        </div>
        <div v-else>
          <span>服务器节点</span>
          <div style="display: inline-block; height: 15px">
            <div style="display: flex">
              <el-select v-model="nodeEdit.locations" style="width: 80px" size="small">
                <el-option v-for="item in locations" :value="item.name">
                  <span :class="`flag-icon flag-icon-${item.name}`"></span>&nbsp;
                  {{item.desc}}
                </el-option>
              </el-select>
              <el-input style="margin-left:10px " v-model="nodeEdit.name" size="small"
                        placeholder="请输入节点名称...">
              </el-input>
              <div style="margin-left: 10px">
                <i @click.stop="submitNodeEdit" class="fa-solid fa-check interact-item"></i>
              </div>
            </div>
          </div>
        </div>
        <div>
          <span>公网IP地址</span>
          <span>
            {{details.base.ip}}
            <i class="fa-solid fa-copy interact-item " style="color: dodgerblue" @click.stop="enableEditNode"></i>
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
      <div v-if="details.base.online" v-loading="!details.runtime.list.length"
        style="min-height: 200px">
        <div style="display: flex" v-if="details.runtime.list.length">
          <el-progress type="dashboard" :width="100" :percentage="now.cpuUsage*100" :status="percentageToStatus(now.cpuUsage*100)">
            <div style="font-size: 17px; font-weight: bold;color: initial">CPU</div>
            <div style="font-size: 13px; color: grey;margin-top: 5px">{{(now.cpuUsage*100).toFixed(1)}}%</div>
          </el-progress>
          <el-progress style="margin-left: 20px"
                       type="dashboard" :width="100" :percentage="now.memoryUsage*100" :status="percentageToStatus((now.memoryUsage/details.base.memory)*100)">
            <div style="font-size: 16px;font-weight: bold; color: initial">内存</div>
            <div style="font-size: 13px;color: grey;margin-top: 5px">{{now.memoryUsage.toFixed(1)}}GB</div>
          </el-progress>
          <div style="flex: 1; margin-left: 30px;display: flex; flex-direction: column; height: 80px">
            <div style="flex: 1; font-size: 13px;">
              <div>实时网络速度</div>
              <div>
                <i style="color: orange" class="fa-solid fa-arrow-up"></i>
                <span>{{`${fitByUnit(now.networkUpload, 'KB')}`}}/s</span>
                <el-divider direction="vertical"/>
                <i style="color: orange" class="fa-solid fa-arrow-down"></i>
                <span>{{`${fitByUnit(now.networkDownload, 'KB')}`}}/s</span>
              </div>
            </div>
            <div style="font-size: 13px;display: flex;justify-content: space-between">
              <div>
                <i class="fa-solid fa-hard-drive"></i>
                <span>磁盘总容量</span>
              </div>
              <div>{{(now.diskUsage).toFixed(1)}}GB / {{(details.base.disk).toFixed(1)}}GB</div>
            </div>
            <el-progress type="line" :status="percentageToStatus(now.diskUsage/details.base.disk)" :percentage="now.diskUsage/details.base.disk*100" :show-text="false"/>
          </div>
        </div>
      </div>
      <div v-else>
        <div style="font-size: 15px; color: #910215">
          <i class="fa-solid fa-globe"></i>&nbsp;
          服务器处于离线状态，请检查服务器是否正常运行
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
