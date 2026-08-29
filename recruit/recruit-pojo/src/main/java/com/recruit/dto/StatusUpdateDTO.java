package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 状态更新请求。
 */
@Data
public class StatusUpdateDTO implements Serializable {

    private Integer status;//审核状态，通过或者驳回
    private String remark;//备注
}
