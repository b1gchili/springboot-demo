<template>
  <div class="coupon-exchange-page">
    <div class="page-header">
      <h2 class="page-title">积分商城</h2>
      <el-button
        icon="el-icon-refresh"
        :loading="loadingCoupons || loadingPoints"
        :disabled="loadingCoupons || loadingPoints"
        @click="refreshPage"
      >
        刷新
      </el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card v-loading="loadingPoints" shadow="never" class="summary-card">
          <div class="summary-label">当前可用积分</div>
          <div class="summary-value">{{ points.availablePoints || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="section-header">
      <span>可兑换优惠券</span>
    </div>

    <div v-loading="loadingCoupons" class="coupon-list-wrap">
      <el-row v-if="coupons.length > 0" :gutter="16">
        <el-col
          v-for="coupon in coupons"
          :key="getCouponTemplateId(coupon)"
          :xs="24"
          :sm="12"
          :lg="8"
        >
          <el-card shadow="hover" class="coupon-card">
            <div class="coupon-main">
              <div class="coupon-name">{{ getCouponName(coupon) }}</div>
              <div class="coupon-cost">{{ getPointsCost(coupon) }} 积分</div>
            </div>
            <div class="coupon-meta">
              <span>库存：{{ getStock(coupon) }}</span>
              <span>有效期：{{ getValidDays(coupon) }} 天</span>
            </div>
            <el-button
              class="exchange-button"
              type="primary"
              :disabled="isSoldOut(coupon) || !!exchangingCouponId"
              :loading="exchangingCouponId === getCouponTemplateId(coupon)"
              @click="handleExchange(coupon)"
            >
              {{ isSoldOut(coupon) ? '库存不足' : '立即兑换' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
      <div v-else class="empty-text">暂无可兑换优惠券</div>
    </div>
  </div>
</template>

<script>
import { exchangeCoupon, getExchangeCouponList, getMyPoints } from '@/api/points'

export default {
  name: 'CouponExchange',
  data() {
    return {
      loadingPoints: false,
      loadingCoupons: false,
      exchangingCouponId: null,
      points: {
        availablePoints: 0,
        totalEarnedPoints: 0,
        totalUsedPoints: 0
      },
      coupons: []
    }
  },
  created() {
    this.refreshPage()
  },
  methods: {
    refreshPage() {
      this.fetchPoints()
      this.fetchCoupons()
    },
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
    fetchCoupons() {
      this.loadingCoupons = true
      getExchangeCouponList()
        .then(res => {
          const data = res.data || []
          // 兼容后端直接返回数组、分页 list 或 records 的不同包装形式。
          this.coupons = Array.isArray(data) ? data : (data.list || data.records || [])
        })
        .catch(() => {
          // 业务异常已由 request 拦截器提示，这里消费异常，避免开发环境红屏。
        })
        .finally(() => {
          this.loadingCoupons = false
        })
    },
    handleExchange(coupon) {
      if (this.exchangingCouponId) {
        return
      }
      const couponTemplateId = this.getCouponTemplateId(coupon)
      const pointsCost = this.getPointsCost(coupon)
      this.$confirm(`确定消耗 ${pointsCost} 积分兑换该优惠券吗？`, '兑换确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.exchangingCouponId = couponTemplateId
          return exchangeCoupon({
            couponTemplateId,
            requestNo: this.generateRequestNo()
          })
        })
        .then(() => {
          this.$message.success('兑换成功')
          this.refreshPage()
        })
        .catch(error => {
          // 取消确认不提示；接口异常已由 request 拦截器展示后端 message。
          if (error !== 'cancel' && error !== 'close') {
            return
          }
        })
        .finally(() => {
          this.exchangingCouponId = null
        })
    },
    generateRequestNo() {
      // 前端生成一次性请求号，用于后端根据 userId + requestNo 做幂等控制。
      return `EX_${Date.now()}_${Math.random().toString(16).slice(2, 10)}`
    },
    getCouponTemplateId(coupon) {
      return coupon.couponTemplateId || coupon.id
    },
    getCouponName(coupon) {
      return coupon.couponName || coupon.templateName || '-'
    },
    getPointsCost(coupon) {
      return coupon.pointsCost || coupon.exchangePoints || 0
    },
    getStock(coupon) {
      return coupon.stock || 0
    },
    getValidDays(coupon) {
      return coupon.validDays || 0
    },
    isSoldOut(coupon) {
      return this.getStock(coupon) <= 0
    }
  }
}
</script>

<style scoped>
.coupon-exchange-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  min-height: 112px;
}

.summary-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.summary-value {
  color: #409eff;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.2;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 14px;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.coupon-list-wrap {
  min-height: 240px;
}

.coupon-card {
  margin-bottom: 16px;
  border-radius: 6px;
}

.coupon-main {
  min-height: 92px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 14px;
}

.coupon-name {
  color: #303133;
  font-size: 17px;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-word;
}

.coupon-cost {
  margin-top: 14px;
  color: #e6a23c;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.coupon-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #606266;
  font-size: 14px;
  margin-bottom: 16px;
}

.exchange-button {
  width: 100%;
}

.empty-text {
  padding: 80px 0;
  color: #909399;
  text-align: center;
}
</style>
