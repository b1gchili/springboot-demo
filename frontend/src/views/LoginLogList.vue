<template>
  <div class="login-log-page">
    <div class="page-header">
      <h2 class="page-title">登录日志列表</h2>
    </div>

    <el-table v-loading="loading" :data="logs" border stripe class="login-log-table">
      <el-table-column prop="username" label="账号" min-width="150" />
      <el-table-column prop="displayName" label="姓名" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="160" />
      <el-table-column prop="loginIp" label="登录IP" min-width="160" />
      <el-table-column prop="loginTime" label="登录时间" min-width="180" />
    </el-table>
  </div>
</template>

<script>
import { listLoginLogs } from '@/api/loginLog'

export default {
  name: 'LoginLogList',
  data() {
    return {
      loading: false,
      logs: []
    }
  },
  created() {
    this.fetchLogs()
  },
  methods: {
    fetchLogs() {
      this.loading = true
      listLoginLogs()
        .then(res => {
          this.logs = res.data || []
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
.login-log-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.login-log-table {
  width: 100%;
}
</style>
