package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.CouponExchangeItemVO;
import org.example.student.model.CouponTemplate;

import java.util.List;

@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplate> {
    List<CouponExchangeItemVO> listExchangeableCoupons();

    CouponTemplate findCouponTemplateById(@Param("id") Long id);

    int deductStock(@Param("couponTemplateId") Long couponTemplateId);
}
