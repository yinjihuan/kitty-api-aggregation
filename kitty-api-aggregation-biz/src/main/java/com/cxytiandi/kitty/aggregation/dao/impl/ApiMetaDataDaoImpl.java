package com.cxytiandi.kitty.aggregation.dao.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxytiandi.kitty.aggregation.dao.ApiMetaDataDao;
import com.cxytiandi.kitty.aggregation.dataobject.ApiMetaDataDO;
import com.cxytiandi.kitty.aggregation.mapper.ApiMetaDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Api元数据DAO实现
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-22 22:36
 */
@Repository
public class ApiMetaDataDaoImpl implements ApiMetaDataDao {

    @Autowired
    private ApiMetaDataMapper apiMetaDataMapper;

    @Override
    public void saveApiMetaData(ApiMetaDataDO apiMetaData) {
        apiMetaDataMapper.insert(apiMetaData);
    }

    @Override
    public IPage<ApiMetaDataDO> listApiMetaDatas(int page, int pageSize) {
        Page queryPage = new Page<>(page, pageSize);
        return apiMetaDataMapper.selectPage(queryPage, null);
    }

    @Override
    public List<ApiMetaDataDO> list() {
        return apiMetaDataMapper.selectList(null);
    }
}