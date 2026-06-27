package org.example.student;

import org.example.student.exception.BusinessException;
import org.example.student.model.ExchangeCouponRequest;
import org.example.student.model.SignInVO;
import org.example.student.model.TaskRewardVO;
import org.example.student.model.CompleteTaskRequest;
import org.example.student.service.PointsExchangeService;
import org.example.student.service.PointsSignService;
import org.example.student.service.PointsTaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 积分系统并发安全和幂等性测试。
 *
 * 这些测试直接调用 Service，目的是覆盖真实事务、唯一索引、乐观锁和条件扣库存逻辑。
 * 测试数据统一使用 TEST_USER_PREFIX 前缀，避免误删正常业务数据。
 */
@SpringBootTest
public class PointsConcurrencyTests {
    /** 每个并发场景固定启动 20 个线程，模拟同一时刻的重复请求。 */
    private static final int THREAD_COUNT = 20;
    /** 测试用户 ID 前缀，清理数据时只清理该前缀下的数据。 */
    private static final String TEST_USER_PREFIX = "990000";

    @Autowired
    private PointsSignService pointsSignService;

    @Autowired
    private PointsTaskService pointsTaskService;

    @Autowired
    private PointsExchangeService pointsExchangeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 每个用例开始前先清理历史测试数据，再准备任务配置，保证用例之间互不影响。
        cleanTestData();
        upsertTaskConfig("DAILY_SIGN_IN", "每日签到", 10, 1);
        upsertTaskConfig("TEST_ONCE_TASK", "测试不可重复任务", 30, 0);
        upsertTaskConfig("TEST_REPEAT_TASK", "测试可重复任务", 40, 1);
    }

//    @AfterEach
//    void tearDown() {
//        // 用例执行结束后再次清理，避免测试数据残留到开发库。
//        cleanTestData();
//    }

    @Test
    void sameUserConcurrentSignInOnlyRewardsOnce() throws Exception {
        Long userId = 9900001L;

        // 同一个用户并发签到，唯一索引 user_id + sign_date 应保证只有一个请求成功。
        ConcurrentResult<SignInVO> result = runConcurrent(() -> pointsSignService.signIn(userId));

        assertEquals(1, result.successCount());
        assertEquals(10L, availablePoints(userId));
        assertEquals(1, countSignRecords(userId));
        assertEquals(1, countFlows(userId, "SIGN_IN"));
    }

    @Test
    void sameUserConcurrentCompleteSameNonRepeatableTaskOnlyRewardsOnce() throws Exception {
        Long userId = 9900002L;
        CompleteTaskRequest request = new CompleteTaskRequest();
        request.setTaskCode("TEST_ONCE_TASK");
        request.setBizId("ignored-biz");

        // 不可重复任务会把 bizId 统一替换为 taskCode，所以并发请求只能奖励一次。
        ConcurrentResult<TaskRewardVO> result = runConcurrent(() -> pointsTaskService.completeTask(userId, request));

        assertEquals(1, result.successCount());
        assertEquals(30L, availablePoints(userId));
        assertEquals(1, countTaskRecords(userId, "TEST_ONCE_TASK", "TEST_ONCE_TASK"));
    }

    @Test
    void sameUserConcurrentCompleteRepeatableTaskWithSameBizIdOnlyRewardsOnce() throws Exception {
        Long userId = 9900003L;
        CompleteTaskRequest request = new CompleteTaskRequest();
        request.setTaskCode("TEST_REPEAT_TASK");
        request.setBizId("same-biz");

        // 可重复任务允许不同 bizId 多次奖励，但同一个 bizId 仍然只能奖励一次。
        ConcurrentResult<TaskRewardVO> result = runConcurrent(() -> pointsTaskService.completeTask(userId, request));

        assertEquals(1, result.successCount());
        assertEquals(40L, availablePoints(userId));
        assertEquals(1, countTaskRecords(userId, "TEST_REPEAT_TASK", "same-biz"));
    }

    @Test
    void sameUserConcurrentExchangeSameRequestNoIsIdempotent() throws Exception {
        Long userId = 9900004L;
        Long couponTemplateId = insertCouponTemplate("TEST_COUPON_SAME_REQUEST", "并发幂等券", 15, 20);
        insertUserPoints(userId, 100);

        ExchangeCouponRequest request = new ExchangeCouponRequest();
        request.setCouponTemplateId(couponTemplateId);
        request.setRequestNo("REQ-SAME-001");

        // 相同 requestNo 表示同一次兑换请求的重试，应返回同一结果，不重复扣积分或发券。
        runConcurrent(() -> pointsExchangeService.exchangeCoupon(userId, request));

        assertEquals(85L, availablePoints(userId));
        assertEquals(1, countExchangeRecords(userId, "REQ-SAME-001"));
        assertEquals(1, countUserCoupons(userId, "TEST_COUPON_SAME_REQUEST"));
    }

    @Test
    void multipleUsersConcurrentExchangeSameCouponDoesNotOversell() throws Exception {
        int initialStock = 5;
        Long couponTemplateId = insertCouponTemplate("TEST_COUPON_STOCK", "库存并发券", 10, initialStock);
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Long userId = 9900100L + i;
            userIds.add(userId);
            insertUserPoints(userId, 100);
        }

        // 多个用户同时抢同一张库存有限的券，库存条件更新应防止超卖。
        ConcurrentResult<Object> result = runConcurrentWithIndex(index -> {
            ExchangeCouponRequest request = new ExchangeCouponRequest();
            request.setCouponTemplateId(couponTemplateId);
            request.setRequestNo("REQ-STOCK-" + index);
            return pointsExchangeService.exchangeCoupon(userIds.get(index), request);
        });

        int finalStock = couponStock(couponTemplateId);
        int successCount = result.successCount();
        assertTrue(successCount <= initialStock);
        assertTrue(finalStock >= 0);
        assertEquals(initialStock - successCount, finalStock);
        assertEquals(successCount, countUserCouponsByTemplate("TEST_COUPON_STOCK"));
    }

    @Test
    void exchangeCouponFailsWhenPointsNotEnoughAndRollsBackStockAndCoupon() {
        Long userId = 9900005L;
        Long couponTemplateId = insertCouponTemplate("TEST_COUPON_NOT_ENOUGH", "积分不足券", 100, 3);
        insertUserPoints(userId, 5);

        ExchangeCouponRequest request = new ExchangeCouponRequest();
        request.setCouponTemplateId(couponTemplateId);
        request.setRequestNo("REQ-NOT-ENOUGH");

        // 积分不足发生在扣库存之后，事务回滚后库存、用户券、兑换记录都不能保留。
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pointsExchangeService.exchangeCoupon(userId, request)
        );
        assertEquals("积分不足", exception.getMessage());

        assertEquals(5L, availablePoints(userId));
        assertEquals(3, couponStock(couponTemplateId));
        assertEquals(0, countUserCoupons(userId, "TEST_COUPON_NOT_ENOUGH"));
        assertEquals(0, countExchangeRecords(userId, "REQ-NOT-ENOUGH"));
    }

    private <T> ConcurrentResult<T> runConcurrent(Callable<T> task) throws Exception {
        return runConcurrentWithIndex(index -> task.call());
    }

    /**
     * 并发执行模板。
     *
     * readyLatch 确保所有线程都创建并阻塞在起跑线；
     * startLatch 同时放行所有线程，让唯一索引、乐观锁、条件扣库存真正处于竞争状态。
     */
    private <T> ConcurrentResult<T> runConcurrentWithIndex(IndexedCallable<T> task) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                return task.call(index);
            }));
        }
        readyLatch.await();
        startLatch.countDown();

        List<T> successes = new ArrayList<>();
        List<Throwable> failures = new ArrayList<>();
        for (Future<T> future : futures) {
            try {
                successes.add(future.get());
            } catch (Exception e) {
                failures.add(e.getCause() == null ? e : e.getCause());
            }
        }
        executorService.shutdownNow();
        assertFalse(successes.isEmpty(), "并发请求至少应有一次成功");
        return new ConcurrentResult<>(successes, failures);
    }

    /**
     * 插入或重置任务配置。
     *
     * repeatable=0 表示不可重复任务；repeatable=1 表示可重复任务。
     */
    private void upsertTaskConfig(String taskCode, String taskName, int rewardPoints, int repeatable) {
        jdbcTemplate.update(
                "DELETE FROM points_task_config WHERE task_code = ?",
                taskCode
        );
        jdbcTemplate.update(
                "INSERT INTO points_task_config (task_code, task_name, reward_points, repeatable, reward_limit_type, daily_limit, enabled, description) "
                        + "VALUES (?, ?, ?, ?, ?, 1, 1, ?)",
                taskCode,
                taskName,
                rewardPoints,
                repeatable,
                repeatable == 1 ? "UNLIMITED" : "ONCE",
                taskName
        );
    }

    /** 创建测试优惠券模板，返回模板主键 ID。 */
    private Long insertCouponTemplate(String templateCode, String templateName, int pointsCost, int stock) {
        jdbcTemplate.update(
                "INSERT INTO coupon_template (template_code, template_name, coupon_type, face_value, threshold_amount, exchange_points, stock, total_stock, valid_days, enabled) "
                        + "VALUES (?, ?, 'AMOUNT', 5.00, 0.00, ?, ?, ?, 30, 1)",
                templateCode,
                templateName,
                pointsCost,
                stock,
                stock
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM coupon_template WHERE template_code = ?",
                Long.class,
                templateCode
        );
    }

    /** 初始化用户积分账户，避免测试依赖登录或注册流程。 */
    private void insertUserPoints(Long userId, long availablePoints) {
        jdbcTemplate.update(
                "INSERT INTO user_points (user_id, available_points, total_earned_points, total_used_points, version) VALUES (?, ?, ?, 0, 0)",
                String.valueOf(userId),
                availablePoints,
                availablePoints
        );
    }

    /** 查询用户当前可用积分。 */
    private long availablePoints(Long userId) {
        Long value = jdbcTemplate.queryForObject(
                "SELECT available_points FROM user_points WHERE user_id = ?",
                Long.class,
                String.valueOf(userId)
        );
        return value == null ? 0L : value;
    }

    /** 统计用户签到记录数，用于验证重复签到是否被唯一索引挡住。 */
    private int countSignRecords(Long userId) {
        return count("SELECT COUNT(*) FROM points_sign_record WHERE user_id = ?", String.valueOf(userId));
    }

    /** 统计任务奖励记录数，用于验证任务奖励幂等性。 */
    private int countTaskRecords(Long userId, String taskCode, String bizId) {
        return count(
                "SELECT COUNT(*) FROM points_task_record WHERE user_id = ? AND task_code = ? AND biz_id = ?",
                String.valueOf(userId),
                taskCode,
                bizId
        );
    }

    /** 统计指定来源类型的积分流水数量。 */
    private int countFlows(Long userId, String sourceType) {
        return count(
                "SELECT COUNT(*) FROM points_flow WHERE user_id = ? AND source_type = ?",
                String.valueOf(userId),
                sourceType
        );
    }

    /** 统计兑换记录数量，用于验证 requestNo 幂等性。 */
    private int countExchangeRecords(Long userId, String requestNo) {
        return count(
                "SELECT COUNT(*) FROM points_exchange_record WHERE user_id = ? AND request_no = ?",
                String.valueOf(userId),
                requestNo
        );
    }

    /** 统计某个用户获得的指定模板优惠券数量。 */
    private int countUserCoupons(Long userId, String templateCode) {
        return count(
                "SELECT COUNT(*) FROM user_coupon WHERE user_id = ? AND template_code = ?",
                String.valueOf(userId),
                templateCode
        );
    }

    /** 统计指定模板总共发出的用户优惠券数量。 */
    private int countUserCouponsByTemplate(String templateCode) {
        return count("SELECT COUNT(*) FROM user_coupon WHERE template_code = ?", templateCode);
    }

    /** 查询优惠券模板剩余库存。 */
    private int couponStock(Long couponTemplateId) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT stock FROM coupon_template WHERE id = ?",
                Integer.class,
                couponTemplateId
        );
        return value == null ? 0 : value;
    }

    /** 执行 COUNT 查询并把空结果兜底为 0。 */
    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    /**
     * 清理当前测试类创建的数据。
     *
     * 清理顺序按业务依赖从明细到主表，避免外键约束或唯一索引残留影响后续用例。
     */
    private void cleanTestData() {
        jdbcTemplate.update("DELETE FROM points_exchange_record WHERE user_id LIKE ?", TEST_USER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM user_coupon WHERE user_id LIKE ?", TEST_USER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM points_task_record WHERE user_id LIKE ?", TEST_USER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM points_sign_record WHERE user_id LIKE ?", TEST_USER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM points_flow WHERE user_id LIKE ?", TEST_USER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM user_points WHERE user_id LIKE ?", TEST_USER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM coupon_template WHERE template_code LIKE 'TEST_COUPON_%'");
        jdbcTemplate.update("DELETE FROM points_task_config WHERE task_code IN ('TEST_ONCE_TASK', 'TEST_REPEAT_TASK')");
    }

    /** 支持每个并发线程拿到自己的下标，用于构造不同用户或 requestNo。 */
    private interface IndexedCallable<T> {
        T call(int index) throws Exception;
    }

    /** 保存并发执行结果，测试主要关心成功次数，失败异常保留下来便于调试。 */
    private static class ConcurrentResult<T> {
        private final List<T> successes;
        private final List<Throwable> failures;

        ConcurrentResult(List<T> successes, List<Throwable> failures) {
            this.successes = successes;
            this.failures = failures;
        }

        int successCount() {
            return successes.size();
        }

        List<Throwable> failures() {
            return failures;
        }
    }
}
