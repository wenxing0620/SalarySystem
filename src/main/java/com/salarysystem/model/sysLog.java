package com.salarysystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class sysLog {
    private Long logId;
    private Integer userId;
    private String actionType;
    private String ipAddress;
    private LocalDateTime createTime;
    // HMAC-SM3 校验值（hex）
    private String hmac;
    // 读取后校验是否通过
    private boolean hmacValid;
}

