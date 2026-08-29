package com.recruit.constant;

/**
 * 职位状态常量。
 */
public final class JobStatusConstant {

    private JobStatusConstant() {
    }

    /** 待审核 */
    public static final int PENDING = 0;

    /** 审核通过，招聘中 */
    public static final int APPROVED = 1;

    /** 审核拒绝 */
    public static final int REJECTED = 2;

    /** 已下架 */
    public static final int OFFLINE = 3;
}
