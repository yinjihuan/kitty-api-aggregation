package com.cxytiandi.kitty.aggregation.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cxytiandi.kitty.aggregation.dataobject.ApiMetaDataDO;

import java.util.List;

/**
 * Api元数据DAO
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-22 22:36
 */
public interface ApiMetaDataDao {

    /**
     * 保存元数据
     * @param apiMetaData
     */
    void saveApiMetaData(ApiMetaDataDO apiMetaData);

    /**
     * 查询元数据
     * @param page  页数
     * @param pageSize  页大小
     * @return
     */
    IPage<ApiMetaDataDO> listApiMetaDatas(int page, int pageSize);

    /**
     * 查询元数据
     * @return
     */
    List<ApiMetaDataDO> list();

}