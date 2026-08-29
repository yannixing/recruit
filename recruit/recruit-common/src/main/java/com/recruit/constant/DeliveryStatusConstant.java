package com.recruit.constant;

/**
 * 投递状态常量。
 */
public final class DeliveryStatusConstant {

    private DeliveryStatusConstant() {
    }

    /** 待沟通 */
    public static final int TO_COMMUNICATE = 0;

    /** 已查看 */
    public static final int VIEWED = 1;

    /** 邀约面试 */
    public static final int INTERVIEW = 2;

    /** 面试通过 */
    public static final int INTERVIEW_PASS = 3;

    /** 已发送 Offer */
    public static final int OFFER = 4;

    /** 已入职 */
    public static final int EMPLOYED = 5;

    /** 不合适 */
    public static final int UNFIT = 6;

    /** 已拒绝 */
    public static final int REJECTED = 7;
}
