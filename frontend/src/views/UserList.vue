<template>
  <div class="user-list-page">
    <div class="page-header">
      <h2 class="page-title">用户列表</h2>
      <div class="header-actions">
        <el-button icon="el-icon-download" :loading="exporting" @click="handleExport">导出</el-button>
        <el-button type="primary" icon="el-icon-plus" @click="openAddDialog">新增用户</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="users" border stripe class="user-table">
      <el-table-column label="头像" width="90" align="center">
        <template slot-scope="{ row }">
          <el-avatar v-if="row.avatarUrl" :size="42" :src="row.avatarUrl" />
          <el-avatar v-else :size="42" icon="el-icon-user-solid" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="账号" min-width="150" />
      <el-table-column prop="displayName" label="姓名" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="160" />
      <el-table-column prop="loginCount" label="登录次数" min-width="110" align="center" />
      <el-table-column label="最后登录时间" min-width="180">
        <template slot-scope="{ row }">
          {{ row.lastLoginTime || '未登录' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-edit" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="text" class="danger-action" icon="el-icon-delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      :total="total"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" @closed="resetForm">
      <el-form ref="userForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/files/avatar"
            name="file"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
          >
            <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar-preview" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <el-button v-if="form.avatarUrl" type="text" class="clear-avatar" @click="form.avatarUrl = ''">清除头像</el-button>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="displayName">
          <el-input v-model.trim="form.displayName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item :label="isEdit ? '新密码' : '密码'" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="isEdit ? '不填写则不修改密码' : '请输入密码'"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addUser, deleteUser, exportUsers, listUsers, updateUser } from '@/api/user'

export default {
  name: 'UserList',
  data() {
    const validatePhone = (rule, value, callback) => {
      if (!/^1\d{10}$/.test(value || '')) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }

    return {
      loading: false,
      saving: false,
      exporting: false,
      users: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      dialogVisible: false,
      isEdit: false,
      currentUserId: '',
      form: this.getEmptyForm(),
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { pattern: /^[A-Za-z0-9_]+$/, message: '账号只能包含字母、数字和下划线', trigger: 'blur' }
        ],
        displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
        password: [
          {
            validator: (rule, value, callback) => {
              if (!this.isEdit && !value) {
                callback(new Error('请输入密码'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '修改用户' : '新增用户'
    },
    uploadHeaders() {
      const token = localStorage.getItem('token')
      return token ? { Authorization: 'Bearer ' + token } : {}
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    getEmptyForm() {
      return {
        username: '',
        displayName: '',
        phone: '',
        avatarUrl: '',
        password: ''
      }
    },
    fetchUsers() {
      this.loading = true
      listUsers({
        pageNum: this.pageNum,
        pageSize: this.pageSize
      })
        .then(res => {
          const page = res.data || {}
          this.users = page.list || []
          this.total = page.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    handlePageChange(page) {
      this.pageNum = page
      this.fetchUsers()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
      this.fetchUsers()
    },
    openAddDialog() {
      this.isEdit = false
      this.currentUserId = ''
      this.form = this.getEmptyForm()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.currentUserId = row.userId
      this.form = {
        username: row.username,
        displayName: row.displayName,
        phone: row.phone,
        avatarUrl: row.avatarUrl || '',
        password: ''
      }
      this.dialogVisible = true
    },
    beforeAvatarUpload(file) {
      const isImage = file.type && file.type.indexOf('image/') === 0
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) {
        this.$message.error('头像文件必须是图片格式')
        return false
      }
      if (!isLt5M) {
        this.$message.error('头像图片不能超过5MB')
        return false
      }
      return true
    },
    handleAvatarSuccess(res) {
      if (res.code === 200 || res.code === 0) {
        this.form.avatarUrl = res.data && res.data.url ? res.data.url : ''
        this.$message.success('头像上传成功')
        return
      }
      this.$message.error(res.message || '头像上传失败')
    },
    handleAvatarError() {
      this.$message.error('头像上传失败')
    },
    handleExport() {
      this.exporting = true
      exportUsers()
        .then(response => {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          })
          const fileName = this.getExportFileName(response.headers['content-disposition'])
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = fileName
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
        })
        .finally(() => {
          this.exporting = false
        })
    },
    getExportFileName(contentDisposition) {
      if (!contentDisposition) {
        return '用户列表.xlsx'
      }
      const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/)
      if (utf8Match && utf8Match[1]) {
        return decodeURIComponent(utf8Match[1])
      }
      const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/)
      return fileNameMatch && fileNameMatch[1] ? decodeURIComponent(fileNameMatch[1]) : '用户列表.xlsx'
    },
    handleSave() {
      this.$refs.userForm.validate(valid => {
        if (!valid) return
        this.saving = true
        const request = this.isEdit
          ? updateUser(this.currentUserId, this.form)
          : addUser(this.form)
        request
          .then(() => {
            this.$message.success(this.isEdit ? '修改成功' : '新增成功')
            this.dialogVisible = false
            this.fetchUsers()
          })
          .finally(() => {
            this.saving = false
          })
      })
    },
    handleDelete(row) {
      this.$confirm(`确定删除用户「${row.username}」吗？`, '删除确认', {
        type: 'warning'
      }).then(() => {
        deleteUser(row.userId).then(() => {
          this.$message.success('删除成功')
          this.fetchUsers()
        })
      }).catch(() => {})
    },
    resetForm() {
      this.form = this.getEmptyForm()
      this.currentUserId = ''
      if (this.$refs.userForm) {
        this.$refs.userForm.clearValidate()
      }
    }
  }
}
</script>

<style scoped>
.user-list-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.user-table {
  width: 100%;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.danger-action {
  color: #f56c6c;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader ::v-deep .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 96px;
  height: 96px;
  line-height: 96px;
}

.avatar-uploader ::v-deep .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 96px;
  height: 96px;
  line-height: 96px;
  text-align: center;
}

.avatar-preview {
  width: 96px;
  height: 96px;
  display: block;
  object-fit: cover;
}

.clear-avatar {
  margin-left: 12px;
}
</style>
