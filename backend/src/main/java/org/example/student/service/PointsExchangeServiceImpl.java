package org.example.student.service;

import org.example.student.exception.BusinessException;
import org.example.student.mapper.CouponTemplateMapper;
import org.example.student.mapper.PointsExchangeRecordMapper;
import org.example.student.mapper.UserCouponMapper;
import org.example.student.model.CouponExchangeItemVO;
import org.example.student.model.CouponTemplate;
import org.example.student.model.ExchangeCouponRequest;
import org.example.student.model.ExchangeCouponVO;
import org.example.student.model.PointsExchangeRecord;
import org.example.student.model.PointsSourceTypeEnum;
import org.example.student.model.UserCoupon;
import org.example.student.model.UserPoints;
import org.example.student.util.CouponNoGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分兑换优惠券服务实现。
 */
@Service
public class PointsExchangeServiceImpl implements PointsExchangeService {
    private static final String EXCHANGE_SUCCESS = "SUCCESS";
    private static final String COUPON_UNUSED = "UNUSED";

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;
    private final PointsExchangeRecordMapper pointsExchangeRecordMapper;
    private final PointsAccountService pointsAccountService;

    public PointsExchangeServiceImpl(CouponTemplateMapper couponTemplateMapper,
                                     UserCouponMapper userCouponMapper,
                                     PointsExchangeRecordMapper pointsExchangeRecordMapper,
                                     PointsAccountService pointsAccountService) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.userCouponMapper = userCouponMapper;
        this.pointsExchangeRecordMapper = pointsExchangeRecordMapper;
        this.pointsAccountService = pointsAccountService;
    }

    @Override
    public List<CouponExchangeItemVO> listExchangeableCoupons() {
        return couponTemplateMapper.listExchangeableCoupons();
    }

    /**
     * 积分兑换优惠券。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExchangeCouponVO exchangeCoupon(String userId, ExchangeCouponRequest request) {
        validateExchangeRequest(userId, request);
        String userIdValue = userId.trim();

        PointsExchangeRecord existing = pointsExchangeRecordMapper.findByUserIdAndRequestNo(
                userIdValue,
                request.getRequestNo()
        );
        if (existing != null) {
            if (EXCHANGE_SUCCESS.equals(existing.getExchangeStatus())) {
                return buildExchangeVO(existing, userIdValue);
            }
            throw new BusinessException(400, "请勿重复兑换");
        }

        CouponTemplate template = couponTemplateMapper.findCouponTemplateById(request.getCouponTemplateId());
        if (template == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        if (template.getEnabled() == null || template.getEnabled() != 1) {
            throw new BusinessException(400, "优惠券已下架");
        }

        int stockRows = couponTemplateMapper.deductStock(request.getCouponTemplateId());
        if (stockRows == 0) {
            throw new BusinessException(400, "优惠券库存不足");
        }

        long pointsCost = template.getExchangePoints() == null ? 0L : template.getExchangePoints();
        UserPoints userPoints = pointsAccountService.deductPoints(
                userIdValue,
                Math.toIntExact(pointsCost),
                PointsSourceTypeEnum.EXCHANGE.name(),
                request.getCouponTemplateId() + ":" + request.getRequestNo(),
                "兑换优惠券：" + template.getTemplateName()
        );

        UserCoupon userCoupon = buildUserCoupon(userIdValue, template);
        userCouponMapper.insertUserCoupon(userCoupon);

        PointsExchangeRecord exchangeRecord = buildExchangeRecord(userIdValue, request, template, userCoupon);
        try {
            pointsExchangeRecordMapper.insertExchangeRecord(exchangeRecord);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(400, "请勿重复兑换");
        }

        ExchangeCouponVO vo = buildExchangeVO(exchangeRecord, userCoupon, userPoints.getAvailablePoints());
        vo.setPointsCost(pointsCost);
        return vo;
    }

    private UserCoupon buildUserCoupon(String userId, CouponTemplate template) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCouponNo(CouponNoGenerator.generateCouponNo());
        userCoupon.setUserId(userId);
        userCoupon.setTemplateCode(template.getTemplateCode());
        userCoupon.setCouponName(template.getTemplateName());
        userCoupon.setCouponType(template.getCouponType());
        userCoupon.setFaceValue(template.getFaceValue());
        userCoupon.setThresholdAmount(template.getThresholdAmount());
        userCoupon.setStatus(COUPON_UNUSED);
        userCoupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays() == null ? 30 : template.getValidDays()));
        return userCoupon;
    }

    private PointsExchangeRecord buildExchangeRecord(String userId,
                                                     ExchangeCouponRequest request,
                                                     CouponTemplate template,
                                                     UserCoupon userCoupon) {
        PointsExchangeRecord record = new PointsExchangeRecord();
        record.setExchangeNo(CouponNoGenerator.generateExchangeNo());
        record.setRequestNo(request.getRequestNo());
        record.setUserId(userId);
        record.setTemplateCode(template.getTemplateCode());
        record.setCouponNo(userCoupon.getCouponNo());
        record.setUsedPoints(template.getExchangePoints() == null ? 0L : template.getExchangePoints());
        record.setExchangeStatus(EXCHANGE_SUCCESS);
        return record;
    }

    private ExchangeCouponVO buildExchangeVO(PointsExchangeRecord record, String userId) {
        UserCoupon userCoupon = userCouponMapper.findUserCouponByCouponNo(record.getCouponNo());
        UserPoints userPoints = pointsAccountService.getOrCreateUserPoints(userId);
        return buildExchangeVO(record, userCoupon, userPoints.getAvailablePoints());
    }

    private ExchangeCouponVO buildExchangeVO(PointsExchangeRecord record, UserCoupon userCoupon, Long availablePoints) {
        ExchangeCouponVO vo = new ExchangeCouponVO();
        vo.setExchangeNo(record.getExchangeNo());
        vo.setRequestNo(record.getRequestNo());
        vo.setUserId(record.getUserId());
        vo.setUserCouponId(userCoupon == null ? null : userCoupon.getId());
        vo.setTemplateCode(record.getTemplateCode());
        vo.setCouponNo(record.getCouponNo());
        vo.setPointsCost(record.getUsedPoints());
        vo.setUsedPoints(record.getUsedPoints());
        vo.setAvailablePoints(availablePoints);
        vo.setExchangeStatus(record.getExchangeStatus());
        return vo;
    }

    private void validateExchangeRequest(String userId, ExchangeCouponRequest request) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (request == null || request.getCouponTemplateId() == null) {
            throw new BusinessException(400, "优惠券模板ID不能为空");
        }
        if (!StringUtils.hasText(request.getRequestNo())) {
            throw new BusinessException(400, "请求号不能为空");
        }
    }
}
