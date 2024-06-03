<script setup>

import {onBeforeUnmount, onMounted, ref} from "vue";
import {ElMessage} from "element-plus";
import {AttachAddon} from "xterm-addon-attach/src/AttachAddon";

import "xterm/css/xterm.css";
import {Terminal} from "xterm";
const props = defineProps({
  id: Number
})

const terminalRef = ref()
const emits = defineEmits(['dispose'])

const socket = new WebSocket(`ws://localhost:8080/terminal/${props.id}`)
socket.onclose = evt => {
  if(evt.code !== 1000) {
    ElMessage.warning(`连接失败: ${evt.reason}`)
  } else {
    ElMessage.success('远程SSH连接已断开')
  }
  emits('dispose')
}

const attachAddon = new AttachAddon(socket);
const term = new Terminal({
  lineHeight: 1.2,
  rows: 20,
  fontSize: 13,
  fontFamily: "Monaco, Menlo, Consolas, 'Courier New', monospace",
  fontWeight: "bold",
  theme: {
    background: '#000000'
  },
  // 光标闪烁
  cursorBlink: true,
  cursorStyle: 'underline',
  scrollback: 100,
  tabStopWidth: 4,
});
term.loadAddon(attachAddon);

onMounted(() => {
  term.open(terminalRef.value)
  term.focus()  //让当前输入集中到这个组件上
})

onBeforeUnmount(() => {   //点×之后关闭
  socket.close()
  term.dispose()
})
</script>

<template>
  <div ref="terminalRef" class="xterm"/>

</template>

<style scoped>

</style>