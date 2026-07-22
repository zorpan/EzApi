package com.newx.ezapi.core.entity.enums;

/**
 * 协议类型枚举
 * 定义 API 支持的协议类型，用于路由到对应的协议执行器
 */
public enum ProtocolType {

    /**
     * SQL 查询协议（MyBatis 动态 SQL）
     */
    SQL("SQL", "数据库查询"),

    /**
     * WebService SOAP 协议
     */
    WS("WS", "WebService");

    private final String code;
    private final String description;

    ProtocolType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static ProtocolType fromCode(String code) {
        if (code == null) {
            return SQL; // 默认 SQL
        }
        for (ProtocolType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return SQL; // 未知类型默认 SQL
    }
}
