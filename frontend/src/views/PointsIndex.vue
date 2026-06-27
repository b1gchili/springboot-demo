<template>
  <div class="points-page">
    <div class="page-header">
      <h2 class="page-title">我的积分</h2>
      <el-button type="primary" icon="el-icon-check" :loading="signing" @click="handleSignIn">每日签到</el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">当前可用积分</div>
          <div class="summary-value primary">{{ points.availablePoints || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">累计获得积分</div>
          <div class="summary-value income">{{ points.totalEarnedPoints || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">累计使用积分</div>
          <div class="summary-value expense">{{ points.totalUsedPoints || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="flow-section">
      <div slot="header" class="section-header">
        <span>积分流水</span>
      </div>

      <el-table v-loading="loadingFlows" :data="flows" border stripe class="flow-table">
        <el-table-column label="积分变动" min-width="120" align="center">
          <template slot-scope="{ row }">
            <span :class="getChangeClass(row.pointsChange)">
              {{ formatPointsChange(row.pointsChange) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="pointsBefore" label="变动前积分" min-width="120" align="center" />
        <el-table-column prop="pointsAfter" label="变动后积分" min-width="120" align="center" />
        <el-table-column label="流水类型" min-width="120">
          <template slot-scope="{ row }">
            {{ formatFlowType(row.flowType) }}
          </template>
        </el-table-column>
        <el-table-column label="来源类型" min-width="140">
          <template slot-scope="{ row }">
            {{ formatSourceType(row.sourceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="180" />
        <el-table-column prop="createTime" label="时间" min-width="180" />
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
    </el-card>
  </div>
</template>

<script>
import { getMyPoints, getPointsFlows, signIn } from '@/api/points'

export default {
  name: 'PointsIndex',
  data() {
    return {
      loadingPoints: false,
      loadingFlows: false,
      signing: false,
      points: {
        availablePoints: 0,
        totalEarnedPoints: 0,
        totalUsedPoints: 0
      },
      flows: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      sourceTypeMap: {
        SIGN_IN: '每日签到',
        TASK: '任务奖励',
        EXCHANGE: '兑换优惠券',
        ADMIN: '后台调整'
      },
      flowTypeMap: {
        INCOME: '收入',
        EXPENSE: '支出'
      }
    }
  },
  created() {
    this.fetchPoints()
    this.fetchFlows()
  },
  methods: {
    fetchPoints() {
      this.loadingPoints = true
      getMyPoints()
        .then(res => {
          this.points = res.data || {
            availablePoints: 0,
            totalEarnedPoints: 0,
            totalUsedPoints: 0
          }
        })
        .finally(() => {
          this.loadingPoints = false
        })
    },
    fetchFlows() {
      this.loadingFlows = true
      getPointsFlows({
        pageNum: this.pageNum,
        pageSize: this.pageSize
      })
        .then(res => {
          const page = res.data || {}
          this.flows = page.list || []
          this.total = page.total || 0
        })
        .finally(() => {
          this.loadingFlows = false
        })
    },
    handleSignIn() {
      this.signing = true
      signIn()
        .then(() => {
          this.$message.success('签到成功')
          this.pageNum = 1
          this.fetchPoints()
          this.fetchFlows()
        })
        .catch(error => {
          const message = error && error.message ? error.message : ''
          if (message.indexOf('今日已签到') >= 0) {
            this.$message.warning('今日已签到')
          }
        })
        .finally(() => {
          this.signing = false
        })
    },
    handlePageChange(page) {
      this.pageNum = page
      this.fetchFlows()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
      this.fetchFlows()
    },
    formatPointsChange(value) {
      const points = Number(value || 0)
      return points > 0 ? `+${points}` : String(points)
    },
    getChangeClass(value) {
      return Number(value || 0) >= 0 ? 'points-income' : 'points-expense'
    },
    formatFlowType(type) {
      return this.flowTypeMap[type] || type || '-'
    },
    formatSourceType(type) {
      return this.sourceTypeMap[type] || type || '-'
    }
  }
}
</script>

<style scoped>
.points-page {
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

.summary-row {
  margin-bottom: 16px;
}

.summary-card {
  border-radius: 6px;
}

.summary-label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.summary-value.primary {
  color: #409eff;
}

.summary-value.income {
  color: #67c23a;
}

.summary-value.expense {
  color: #f56c6c;
}

.flow-section {
  border-radius: 6px;
}

.section-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.flow-table {
  width: 100%;
}

.points-income {
  color: #67c23a;
  font-weight: 600;
}

.points-expense {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
