package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import entity.PageResult;
import com.pinyougou.sellergoods.service.SpecificationService;
import groupEntity.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper tbSpecificationMapper;
    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    /**
     * 条件分页查询
     */
    @Override
    public PageResult search(TbSpecification specificationCon, Integer pageNum, Integer pageSize) {
        //分页
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        ;

        if (null != specificationCon) {
            String specName = specificationCon.getSpecName();
            if (null != specName && !"".equals(specName)) {
                //设置查询条件
                TbSpecificationExample.Criteria criteria = example.createCriteria();
                criteria.andSpecNameLike("%" + specName + "%");
            }
        }
        Page<TbSpecification> page = (Page<TbSpecification>) tbSpecificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Specification specification) {
        //保存规格
        TbSpecification tbSpecification = specification.getSpecification();
        tbSpecificationMapper.insert(tbSpecification);

        //保存规格选项
        List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptions();

        for (TbSpecificationOption specificationOption : specificationOptions) {
            //设置规格选项关联规格id
            specificationOption.setSpecId(tbSpecification.getId());
            tbSpecificationOptionMapper.insert(specificationOption);
        }
    }


    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();

        TbSpecification tbSpecification = tbSpecificationMapper.selectByPrimaryKey(id);
        specification.setSpecification(tbSpecification);

        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);

        List<TbSpecificationOption> tbSpecificationOptions = tbSpecificationOptionMapper.selectByExample(example);
        specification.setSpecificationOptions(tbSpecificationOptions);
        return specification;
    }


    @Override
    public void update(Specification specification) {

        TbSpecification tbSpecification = specification.getSpecification();
        tbSpecificationMapper.updateByPrimaryKey(tbSpecification);

        List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptions();
        //删除原来规格选项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(tbSpecification.getId());
        tbSpecificationOptionMapper.deleteByExample(example);
        for (TbSpecificationOption specificationOption : specificationOptions) {
            //新增页面传递的规格选项
            specificationOption.setSpecId(tbSpecification.getId());
            tbSpecificationOptionMapper.insert(specificationOption);
        }
    }

    /**
     * 删除
     */
    @Override
    public void delete(Long[] ids) {

        for (Long id : ids) {
            //删除规格
            tbSpecificationMapper.deleteByPrimaryKey(id);
            //删除规格选项
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            tbSpecificationOptionMapper.deleteByExample(example);
        }
    }

}
