package com.pinyougou.sellergoods.service;


import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌服务层接口
 */
public interface BrandService {

    /**
     * 查询所有品牌列表
     */
    public List<TbBrand> findAll();

    /**
     *
     */
    public PageResult search(TbBrand brand, Integer pageNum, Integer pageSize);

    void add(TbBrand brand);

    TbBrand findOne(Long id);

    void update(TbBrand brand);

    void delete(Long[] ids);

    List<Map> findSelectBrandList();
}
