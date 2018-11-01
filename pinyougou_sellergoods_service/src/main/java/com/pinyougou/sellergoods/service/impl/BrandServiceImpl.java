package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import entity.PageResult;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {


        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult search(TbBrand brand, Integer pageNum, Integer pageSize) {
        //设置分页查询条件
        PageHelper.startPage(pageNum, pageSize);
        TbBrandExample example = null;
        if (null != brand) {
            String brandName = brand.getName();
            String firstChar = brand.getFirstChar();
            example = new TbBrandExample();
            TbBrandExample.Criteria criteria = example.createCriteria();
            if (null != brandName && !"".equals(brandName)) {
                criteria.andNameLike("%"+ brandName + "%");
            }
            if (null != firstChar && !"".equals(firstChar)) {
                criteria.andFirstCharEqualTo(firstChar);
            }
        }

        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }
}
