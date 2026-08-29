package com.recruit.mapper;

import com.recruit.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper {

    /**
     * 按账号和角色查找用户。
     */
    User getByAccountAndRole(@Param("account") String account, @Param("role") Integer role);

    User getById(Long id);

    List<User> list(@Param("role") Integer role, @Param("keyword") String keyword);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
