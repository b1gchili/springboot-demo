<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="brand">
        <div class="brand-mark">S</div>
        <div>
          <h1>学生管理系统</h1>
          <p>登录后继续管理学生信息</p>
        </div>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="账号密码登录" name="password">
          <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-position="top">
            <el-form-item label="账号" prop="username">
              <el-input v-model.trim="passwordForm.username" prefix-icon="el-icon-user" placeholder="admin" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="passwordForm.password"
                prefix-icon="el-icon-lock"
                type="password"
                placeholder="123456"
                show-password
                @keyup.enter.native="handlePasswordLogin"
              />
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="loading" @click="handlePasswordLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机验证码登录" name="sms">
          <el-form ref="smsForm" :model="smsForm" :rules="smsRules" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="smsForm.phone" prefix-icon="el-icon-mobile-phone" placeholder="13800138000" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-row">
                <el-input
                  v-model.trim="smsForm.code"
                  prefix-icon="el-icon-key"
                  placeholder="6位验证码"
                  maxlength="6"
                  @keyup.enter.native="handleSmsLogin"
                />
                <el-button :disabled="countdown > 0" :loading="sendingCode" @click="handleSendCode">
                  {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSmsLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { loginByPassword, loginBySms, sendSmsCode } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    const validatePhone = (rule, value, callback) => {
      if (!/^1\d{10}$/.test(value || '')) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }

    return {
      activeTab: 'password',
      loading: false,
      sendingCode: false,
      countdown: 0,
      timer: null,
      passwordForm: {
        username: 'admin',
        password: '123456'
      },
      smsForm: {
        phone: '13800138000',
        code: ''
      },
      passwordRules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      smsRules: {
        phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
        ]
      }
    }
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    handlePasswordLogin() {
      this.$refs.passwordForm.validate(valid => {
        if (!valid) return
        this.loading = true
        loginByPassword(this.passwordForm)
          .then(res => this.afterLogin(res.data))
          .catch(() => {
            // 业务异常已由 request 拦截器用 Message.error 展示，这里消费异常，避免开发环境红屏。
          })
          .finally(() => {
            this.loading = false
          })
      })
    },
    handleSendCode() {
      this.$refs.smsForm.validateField('phone', error => {
        if (error) return
        this.sendingCode = true
        sendSmsCode({ phone: this.smsForm.phone })
          .then(res => {
            this.smsForm.code = ''
            this.$message.success('验证码已发送，有效期' + res.data.expiresIn + '秒')
            this.startCountdown(res.data.expiresIn || 60)
          })
          .catch(() => {
            // 业务异常已由 request 拦截器用 Message.error 展示，这里消费异常，避免开发环境红屏。
          })
          .finally(() => {
            this.sendingCode = false
          })
      })
    },
    handleSmsLogin() {
      this.$refs.smsForm.validate(valid => {
        if (!valid) return
        this.loading = true
        loginBySms(this.smsForm)
          .then(res => this.afterLogin(res.data))
          .catch(() => {
            // 业务异常已由 request 拦截器用 Message.error 展示，这里消费异常，避免开发环境红屏。
          })
          .finally(() => {
            this.loading = false
          })
      })
    },
    afterLogin(data) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(data.user || {}))
      this.$message.success('登录成功')
      const redirect = this.$route.query.redirect || '/query'
      this.$router.replace(redirect)
    },
    startCountdown(seconds) {
      this.countdown = seconds
      if (this.timer) {
        clearInterval(this.timer)
      }
      this.timer = setInterval(() => {
        this.countdown -= 1
        if (this.countdown <= 0) {
          clearInterval(this.timer)
          this.timer = null
        }
      }, 1000)
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2f7;
  padding: 24px;
  box-sizing: border-box;
}

.login-panel {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 18px 45px rgba(31, 45, 61, 0.14);
  padding: 32px;
  box-sizing: border-box;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #2f7df6;
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
}

.brand p {
  margin: 6px 0 0;
  color: #6b778c;
  font-size: 14px;
}

.code-row {
  display: flex;
  gap: 10px;
}

.code-row .el-input {
  flex: 1;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
