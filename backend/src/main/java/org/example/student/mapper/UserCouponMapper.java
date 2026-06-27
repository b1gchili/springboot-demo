package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.UserCoupon;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    int insertUserCoupon(UserCoupon userCoupon);

    UserCoupon findUserCouponById(@Param("id") Long id);

    UserCoupon findUserCouponByCouponNo(@Param("couponNo") String couponNo);
}
