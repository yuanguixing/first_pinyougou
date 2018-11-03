//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            //为分类对象指定父id
            $scope.entity.parentId = $scope.paentId;
            serviceObject = itemCatService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.findByParentId($scope.paentId);//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //批量删除
    $scope.dele = function () {
        itemCatService.findByType($scope.selectIds).success(function (response) {
            if (response.success) {
                if (confirm("你确定要删除吗?")) {
                        //获取选中的复选框
                        itemCatService.dele($scope.selectIds).success(function (response) {
                                if (response.success) {
                                    $scope.findByParentId($scope.paentId);//刷新列表
                                }else {
                                    alert(response.message);
                                }
                            }
                        );
            }else{alert(response.message);
            }
        }else{
                alert(response.message);

            }
    })
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }


    $scope.paentId = 0;
    //基于父id查询下级分类列表
    $scope.findByParentId = function (parentId) {
        $scope.paentId = parentId;
        itemCatService.findByParentId(parentId).success(function (response) {
            $scope.list = response;
        });
    }
    //设置当前级别变量
    $scope.grade = 1;
    //设置级别 ,点击查询下级, 级别加一
    $scope.setGrade = function (grade) {
        $scope.grade = grade;

    }

    //实现面包屑导航栏查询效果
    $scope.selectItemCatList = function (entity_p) {
        //$scope.grade=grade;
        //当级别为一级分类时
        if ($scope.grade == 1) {
            $scope.entity_2 = null;
            $scope.entity_3 = null;
        }
        //当级别为二级分类时
        if ($scope.grade == 2) {
            $scope.entity_2 = entity_p;
            $scope.entity_3 = null;
        }
        //当级别为三级分类时
        if ($scope.grade == 3) {
            $scope.entity_3 = entity_p;
        }
        //查询当前级别子类
        $scope.findByParentId(entity_p.id);
    };
    //查询所有模板数据, 展示分类录入时,可选择的模板列表
    $scope.selectTypeList = function () {
        typeTemplateService.findAll().success(function (response) {
            $scope.typeTemplateList = response;
        })
    }
});
