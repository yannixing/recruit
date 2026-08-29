package com.recruit.exception;

/**
 * 当前数据不满足删除条件。
 */
public class DeletionNotAllowedException extends BaseException {

    public DeletionNotAllowedException(String message) {
        super(message);
    }
}
