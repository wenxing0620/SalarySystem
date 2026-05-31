package com.salarysystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class sysUser {
    private Integer userId;
    private Integer empId;
    private String username;
    private String password;
    private Integer roleId;
    private LocalDateTime pwdUpdateTime;
    private Integer failCount;
    private LocalDateTime lockTime;
}

