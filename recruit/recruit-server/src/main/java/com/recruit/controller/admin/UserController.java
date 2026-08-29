package com.recruit.controller.admin;

import com.recruit.entity.User;
import com.recruit.exception.BaseException;
import com.recruit.mapper.UserMapper;
import com.recruit.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理接口。
 */
@RestController
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public Result<List<User>> list(@RequestParam(required = false) Integer role,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(userMapper.list(role, keyword));
    }

    /**
     * 启用或禁用用户。
     *
     * status=1 表示正常，status=0 表示禁用，注销状态不能通过此接口恢复。
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BaseException("用户状态只能设置为正常或禁用");
        }
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 2) {
            throw new BaseException("已注销用户不能修改状态");
        }
        userMapper.updateStatus(id, status);
        return Result.success();
    }
}
