package com.cxytiandi.kitty.aggregation.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * API元数据DO
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-22 22:37
 */
@Data
@TableName("api_metadata")
public class ApiMetaDataDO {

    @TableId(type = IdType.ID_WORKER)
    private Long id;

    /**
     * API名称
     */
    private String apiName;

    /**
     * 元数据
     */
    private String metadata;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 添加时间
     */
    private Date addTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}