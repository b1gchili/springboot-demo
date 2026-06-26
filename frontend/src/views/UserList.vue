<template>
  <div class="user-list-page">
    <div class="page-header">
      <h2 class="page-title">用户列表</h2>
      <el-button type="primary" icon="el-icon-plus" @click="openAddDialog">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="users" border stripe class="user-table">
      <el-table-column prop="username" label="账号" min-width="150" />
      <el-table-column prop="displayName" label="姓名" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="160" />
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="460px" @closed="resetForm">
      <el-form ref="userForm" :model="form" :rules="rules" label-width="80px">
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
import { addUser, deleteUser, listUsers, updateUser } from '@/api/user'

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
      users: [],
      dialogVisible: false,
      isEdit: false,
      currentUserId: '',
      form: {
        username: '',
        displayName: '',
        phone: '',
        password: ''
      },
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
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    fetchUsers() {
      this.loading = true
      listUsers()
        .then(res => {
          this.users = res.data || []
        })
        .finally(() => {
          this.loading = false
        })
    },
    openAddDialog() {
      this.isEdit = false
      this.currentUserId = ''
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.currentUserId = row.userId
      this.form = {
        username: row.username,
        displayName: row.displayName,
        phone: row.phone,
        password: ''
      }
      this.dialogVisible = true
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
      this.form = {
        username: '',
        displayName: '',
        phone: '',
        password: ''
      }
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

.user-table {
  width: 100%;
}

.danger-action {
  color: #f56c6c;
}
</style>
