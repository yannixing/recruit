package com.recruit.context;

/**
 * 保存当前请求用户信息的线程上下文。
 */
public final class BaseContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void removeCurrentUserId() {
        CURRENT_USER_ID.remove();
    }
}
