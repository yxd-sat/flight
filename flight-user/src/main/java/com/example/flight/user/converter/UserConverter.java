package com.example.flight.user.converter;

import com.example.flight.user.dto.UserInfoVO;
import com.example.flight.user.entity.User;

/**
 * Entity ↔ VO 转换器（Entity 对内、VO 对外）
 */
public final class UserConverter {

    private UserConverter() {
    }

    public static UserInfoVO toVO(User user, String token) {
        if (user == null) {
            return null;
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setToken(token);
        return vo;
    }
}
