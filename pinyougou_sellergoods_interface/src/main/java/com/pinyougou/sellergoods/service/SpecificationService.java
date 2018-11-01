package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbSpecification;
import entity.PageResult;
import groupEntity.Specification;

public interface SpecificationService {

    /**
     * 条件分页查询
     */
    public PageResult search(TbSpecification specificationCon, Integer pageNum, Integer pageSize);

    /**
     * 新增
     */
    public void add(Specification specification);

    void update(Specification specification);

    Specification findOne(Long id);

    void delete(Long[] ids);
}
