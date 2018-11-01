package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSpecification;
import entity.PageResult;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.Result;
import groupEntity.Specification;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;

    @RequestMapping("/search")
    public PageResult search(@RequestBody TbSpecification specificationCon, Integer pageNum, Integer pageSize) {
        return specificationService.search(specificationCon, pageNum, pageSize);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody Specification specification) {
        try {
            specificationService.add(specification);
            return new Result(true, "新增成功");
        } catch (Exception e) {
            return new Result(false, "新增失败");
        }
    }

    @RequestMapping("/findOne")
    public Specification findOne(Long id){
       return specificationService.findOne(id);
    }


    @RequestMapping("/update")
    public Result update(@RequestBody Specification specification) {
        try {
            specificationService.update(specification);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            return new Result(false, "修改失败");
        }
    }
    @RequestMapping("/dele")
    public Result dele(Long [] ids) {
        try {
            specificationService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            return new Result(false, "删除失败");
        }
    }

}
