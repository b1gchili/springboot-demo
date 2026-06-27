package org.example.student.model;

import lombok.Data;

/**
 * 积分排行榜分页展示对象。
 */
@Data
public class PointsRankingPageVO {
    private PageResult<PointsRankingVO> page;
    private Integer myRank;
}
