<template>
  <div class="points-page">
    <div class="page-header">
      <h2 class="page-title">积分中心</h2>
      <el-button
        type="primary"
        icon="el-icon-check"
        :loading="signing"
        :disabled="signing"
        @click="handleSignIn"
      >
        每日签到
      </el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card v-loading="loadingPoints" shadow="never" class="summary-card">
          <div class="summary-label">当前可用积分</div>
          <div class="summary-value primary">{{ points.availablePoints || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card v-loading="loadingPoints" shadow="never" class="summary-card">
          <div class="summary-label">累计获得积分</div>
          <div class="summary-value income">{{ points.totalEarnedPoints || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card v-loading="loadingPoints" shadow="never" class="summary-card">
          <div class="summary-label">累计使用积分</div>
          <div class="summary-value expense">{{ points.totalUsedPoints || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="task-section">
      <div slot="header" class="section-header">
        <span>积分任务</span>
      </div>

      <el-row :gutter="16">
        <el-col v-for="task in pointTasks" :key="task.taskCode" :xs="24" :sm="12" :lg="6">
          <div class="task-item">
            <div class="task-info">
              <div class="task-name">{{ task.taskName }}</div>
              <div class="task-desc">{{ task.description }}</div>
              <div class="task-reward">+{{ task.rewardPoints }} 积分</div>
            </div>
            <el-button
              size="small"
              type="primary"
              :plain="isTaskCompleted(task)"
              :disabled="isTaskCompleted(task) || !!completingTaskCode"
              :loading="completingTaskCode === task.taskCode"
              @click="handleCompleteTask(task)"
            >
              {{ isTaskCompleted(task) ? '已完成' : task.buttonText }}
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="ranking-section">
      <div slot="header" class="section-header ranking-header">
        <span>积分排行榜</span>
        <span class="my-rank">我的排名：{{ formatMyRank(myRank) }}</span>
      </div>

      <el-table v-loading="loadingRanking" :data="rankings" border stripe class="ranking-table">
        <el-table-column label="排名" width="100" align="center">
          <template slot-scope="{ row }">
            <span :class="getRankClass(getRankValue(row))">
              {{ getRankValue(row) || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" min-width="150" />
        <el-table-column prop="displayName" label="姓名" min-width="140">
          <template slot-scope="{ row }">
            {{ row.displayName || row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="累计获得积分" min-width="150" align="center">
          <template slot-scope="{ row }">
            {{ row.totalEarnedPoints || 0 }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="rankingPageNum"
        :page-size="rankingPageSize"
        :page-sizes="[10, 20, 50]"
        :total="rankingTotal"
        @current-change="handleRankingPageChange"
        @size-change="handleRankingSizeChange"
      />
    </el-card>

    <el-card shadow="never" class="flow-section">
      <div slot="header" class="section-header">
        <span>积分流水</span>
      </div>

      <el-table v-loading="loadingFlows" :data="flows" border stripe class="flow-table">
        <el-table-column label="积分变动" min-width="120" align="center">
          <template slot-scope="{ row }">
            <span :class="getChangeClass(getFlowChangePoints(row))">
              {{ formatPointsChange(getFlowChangePoints(row)) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动前积分" min-width="120" align="center">
          <template slot-scope="{ row }">
            {{ getFlowBeforePoints(row) }}
          </template>
        </el-table-column>
        <el-table-column label="变动后积分" min-width="120" align="center">
          <template slot-scope="{ row }">
            {{ getFlowAfterPoints(row) }}
          </template>
        </el-table-column>
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
import { completeTask, getMyPoints, getPointsFlows, getPointsRanking, signIn } from '@/api/points'

export default {
  name: 'PointsIndex',
  data() {
    return {
      loadingPoints: false,
      loadingFlows: false,
      loadingRanking: false,
      completingTaskCode: '',
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
      rankings: [],
      rankingTotal: 0,
      rankingPageNum: 1,
      rankingPageSize: 20,
      myRank: null,
      // 当前后端暂无任务配置查询接口，先在积分中心维护一份轻量任务入口。
      pointTasks: [
        {
          taskCode: 'COMPLETE_PROFILE',
          taskName: '完善个人资料',
          description: '补全账号基础信息后领取奖励',
          rewardPoints: 20,
          repeatable: false,
          bizId: 'COMPLETE_PROFILE',
          buttonText: '领取积分'
        },
        {
          taskCode: 'SHARE_APP',
          taskName: '分享应用',
          description: '每次分享都可以领取奖励',
          rewardPoints: 10,
          repeatable: true,
          bizIdPrefix: 'SHARE_APP',
          buttonText: '领取积分'
        }
      ],
      completedTaskMap: {},
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
    this.fetchRanking()
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
        .catch(() => {
          // 业务异常已由 request 拦截器提示，这里消费异常，避免开发环境红屏。
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
          const page = this.normalizePage(res.data)
          this.flows = page.list
          this.total = page.total
        })
        .catch(() => {
          // 业务异常已由 request 拦截器提示，这里消费异常，避免开发环境红屏。
        })
        .finally(() => {
          this.loadingFlows = false
        })
    },
    fetchRanking() {
      this.loadingRanking = true
      getPointsRanking({
        pageNum: this.rankingPageNum,
        pageSize: this.rankingPageSize
      })
        .then(res => {
          const data = res.data || {}
          const page = this.normalizePage(data.page || data)
          this.rankings = page.list
          this.rankingTotal = page.total
          this.myRank = this.extractRank(data.myRank)
        })
        .catch(() => {
          // 业务异常已由 request 拦截器提示，这里消费异常，避免开发环境红屏。
        })
        .finally(() => {
          this.loadingRanking = false
        })
    },
    handleSignIn() {
      if (this.signing) {
        return
      }
      this.signing = true
      signIn()
        .then(() => {
          this.$message.success('签到成功')
          this.refreshPointsData()
        })
        .catch(() => {
          // 重复签到等业务异常已由 request 拦截器展示后端 message。
        })
        .finally(() => {
          this.signing = false
        })
    },
    handleCompleteTask(task) {
      if (this.completingTaskCode) {
        return
      }
      this.completingTaskCode = task.taskCode
      completeTask({
        taskCode: task.taskCode,
        // 可重复任务每次点击生成新的业务 ID；后端仍用 userId + taskCode + bizId 防止同一次业务重复发奖。
        bizId: this.buildTaskBizId(task)
      })
        .then(() => {
          this.$message.success('任务积分领取成功')
          if (!task.repeatable) {
            this.markTaskCompleted(task.taskCode)
          }
          this.refreshPointsData()
        })
        .catch(error => {
          const message = error && error.message ? error.message : ''
          if (!task.repeatable && message.indexOf('该任务已奖励过积分') >= 0) {
            this.markTaskCompleted(task.taskCode)
          }
        })
        .finally(() => {
          this.completingTaskCode = ''
        })
    },
    refreshPointsData() {
      this.pageNum = 1
      this.fetchPoints()
      this.fetchRanking()
      this.fetchFlows()
    },
    markTaskCompleted(taskCode) {
      this.$set(this.completedTaskMap, taskCode, true)
    },
    isTaskCompleted(task) {
      return !task.repeatable && this.completedTaskMap[task.taskCode]
    },
    buildTaskBizId(task) {
      if (!task.repeatable) {
        return task.bizId
      }
      // 分享应用每次点击都生成新的 bizId，满足“每次分享领取一次积分”的业务效果。
      return `${task.bizIdPrefix}_${Date.now()}_${Math.random().toString(16).slice(2, 10)}`
    },
    normalizePage(pageData) {
      // 兼容项目 PageResult(list/total) 和常见 records/total 两种分页结构。
      const page = pageData || {}
      return {
        list: page.list || page.records || [],
        total: Number(page.total || 0)
      }
    },
    getFlowChangePoints(row) {
      // 后端当前返回 changePoints；这里兼容早期前端约定的 pointsChange。
      return row.changePoints !== undefined ? row.changePoints : row.pointsChange
    },
    getFlowBeforePoints(row) {
      // 后端当前返回 beforePoints；这里兼容早期前端约定的 pointsBefore。
      const value = row.beforePoints !== undefined ? row.beforePoints : row.pointsBefore
      return value !== undefined && value !== null ? value : '-'
    },
    getFlowAfterPoints(row) {
      // 后端当前返回 afterPoints；这里兼容早期前端约定的 pointsAfter。
      const value = row.afterPoints !== undefined ? row.afterPoints : row.pointsAfter
      return value !== undefined && value !== null ? value : '-'
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
    handleRankingPageChange(page) {
      this.rankingPageNum = page
      this.fetchRanking()
    },
    handleRankingSizeChange(size) {
      this.rankingPageSize = size
      this.rankingPageNum = 1
      this.fetchRanking()
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
    },
    getRankValue(row) {
      return row.ranking || row.rank
    },
    extractRank(value) {
      if (value && typeof value === 'object') {
        return value.ranking || value.rank
      }
      return value
    },
    formatMyRank(rank) {
      return rank ? `第 ${rank} 名` : '暂无排名'
    },
    getRankClass(rank) {
      if (rank === 1) return 'rank-first'
      if (rank === 2) return 'rank-second'
      if (rank === 3) return 'rank-third'
      return ''
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

.task-section,
.ranking-section,
.flow-section {
  border-radius: 6px;
}

.task-section,
.ranking-section {
  margin-bottom: 16px;
}

.task-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  min-height: 132px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
}

.task-info {
  min-width: 0;
}

.task-name {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
}

.task-desc {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.task-reward {
  margin-top: 12px;
  color: #67c23a;
  font-size: 18px;
  font-weight: 700;
}

.section-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.ranking-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.my-rank {
  font-size: 14px;
  font-weight: 400;
  color: #606266;
}

.ranking-table,
.flow-table {
  width: 100%;
}

.rank-first,
.rank-second,
.rank-third {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  border-radius: 50%;
  font-weight: 700;
}

.rank-first {
  color: #b7791f;
  background: #fff7e6;
}

.rank-second {
  color: #606266;
  background: #f2f3f5;
}

.rank-third {
  color: #b45f06;
  background: #fff1e8;
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
