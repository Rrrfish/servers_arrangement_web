<script setup>
import {reactive, ref} from "vue";
import router from "@/router";
import {logout, post} from "@/net";
import {ElMessage} from "element-plus";

const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid


const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: '',
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.new_password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rules = {
  password: [
    { required: true, message: '请输入原来的密码', trigger: 'blur' },
  ],
  new_password: [
    { required: true, message: '请输入新的密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
  ],
  new_password_repeat: [
    { required: true, message: '请重复输入新的密码', trigger: 'blur' },
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
}



function resetPassword() {
  formRef.value.validate(isValid => {
    if(isValid) {
      post('/api/user/change-password', form, () => {
        ElMessage.success('密码修改成功，请重新登录!')
        logout(() => router.push('/'))
      })
    }
  })
}
</script>

<template>
  <div style="display: flex; gap: 10px">
    <div style="flex">
      <div class="info-card">
<!--        <div class="title"><i class="fa-solid fa-lock"></i> 修改密码</div>-->
<!--        <el-divider style="margin: 10px 0"/>-->
        <el-form @validate="onValidate" :model="form" :rules="rules"
                 ref="formRef" style="margin: 20px" label-width="100">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" v-model="form.password"
                      :prefix-icon="Lock" placeholder="当前密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" v-model="form.new_password"
                      :prefix-icon="Lock" placeholder="新密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" v-model="form.new_password_repeat"
                      :prefix-icon="Lock" placeholder="重复新密码" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button @click="resetPassword"
                       type="success" :disabled="!valid">立即重置密码</el-button>
          </div>
        </el-form>
    </div>

    </div>
  </div>
</template>

<style scoped>
.info_card {
  border-radius: 5px;
  padding: 15px 20px;
  background-color: var(--el-bg-color);
}
</style>