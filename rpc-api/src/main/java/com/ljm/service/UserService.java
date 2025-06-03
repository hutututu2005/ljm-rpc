package com.ljm.service;

import com.ljm.annotation.Retryable;
import com.ljm.pojo.User;

public interface UserService {

    // 查询
    @Retryable
    User getUserByUserId(Integer id);

    // 新增
    @Retryable
    Integer insertUserId(User user);
}
