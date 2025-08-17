package com.ljm.service;

import com.ljm.annotation.Retryable;
import com.ljm.pojo.User;

public interface UserService {

    // 查询
    @Retryable
    User getUserByUserId(Integer id);

    // 新增 不是幂等性操作，不支持重试
    Integer insertUserId(User user);
}
