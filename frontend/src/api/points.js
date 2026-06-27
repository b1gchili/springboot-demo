import request from '@/utils/request'

/**
 * 查询当前登录用户的积分账户。
 */
export function getMyPoints() {
  return request({
    url: '/points/me',
    method: 'get'
  })
}

/**
 * 每日签到获取积分。
 */
export function signIn() {
  return request({
    url: '/points/sign-in',
    method: 'post'
  })
}

/**
 * 完成任务奖励积分。
 *
 * @param {Object} data - 任务完成参数
 * @param {string} data.taskCode - 任务编码
 * @param {string} data.bizId - 业务 ID；不可重复任务后端会统一使用 taskCode
 */
export function completeTask(data) {
  return request({
    url: '/points/tasks/complete',
    method: 'post',
    data
  })
}

/**
 * 分页查询当前登录用户的积分流水。
 *
 * @param {Object} params - 分页参数
 * @param {number} [params.pageNum=1] - 当前页码
 * @param {number} [params.pageSize=10] - 每页条数
 */
export function getPointsFlows(params) {
  return request({
    url: '/points/flows',
    method: 'get',
    params
  })
}

/**
 * 分页查询积分排行榜。
 *
 * @param {Object} params - 分页参数
 * @param {number} [params.pageNum=1] - 当前页码
 * @param {number} [params.pageSize=20] - 每页条数
 */
export function getPointsRanking(params) {
  return request({
    url: '/points/ranking',
    method: 'get',
    params
  })
}

/**
 * 查询可兑换优惠券列表。
 */
export function getExchangeCouponList() {
  return request({
    url: '/coupons/exchange/list',
    method: 'get'
  })
}

/**
 * 积分兑换优惠券。
 *
 * @param {Object} data - 兑换请求参数
 * @param {number} data.couponTemplateId - 优惠券模板 ID
 * @param {string} data.requestNo - 幂等请求号，同一次兑换重试必须保持一致
 */
export function exchangeCoupon(data) {
  return request({
    url: '/coupons/exchange',
    method: 'post',
    data
  })
}
