package com.recruit.constant;

/**
 * 面试邀约状态常量。
 */
public final class InterviewStatusConstant {

    private InterviewStatusConstant() {
    }

    /** 待确认 */
    public static final int PENDING = 0;

    /** 已确认 */
    public static final int CONFIRMED = 1;

    /** 求职者拒绝或面试不通过 */
    public static final int REJECTED = 2;

    /** 已完成 */
    public static final int FINISHED = 3;

    /** HR 已取消 */
    public static final int CANCELED = 4;
}
