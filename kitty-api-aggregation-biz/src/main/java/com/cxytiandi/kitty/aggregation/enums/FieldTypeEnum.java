package com.cxytiandi.kitty.aggregation.enums;

/**
 * 字段类型枚举
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-22 21:01:04
 */
public enum FieldTypeEnum {

    INTEGER("Integer", "数字"),

    STRING("String", "字符串"),

    ENTITY("Entity", "实体");

    FieldTypeEnum(String type, String descp) {
        this.type = type;
        this.descp = descp;
    }

    /**
     * 类型
     */
    private String type;

    /**
     * 描述
     */
    private String descp;

    public String getType() {
        return type;
    }

    public String getDescp() {
        return descp;
    }
}
