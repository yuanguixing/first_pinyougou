//$controller实现angularjs继承机制
app.controller("brandController", function ($scope, $controller, brandService) {

    //angularjs继承代码实现, 参数一:父对象名称 参数二: 共享$scope配置{$scope:$scope} 固定写法 伪继承
    $controller("baseController",{$scope:$scope});

    //查询所有品牌数据
    $scope.findAll = function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        })
    };

    $scope.searchEntity={};
    //分页查询 + 条件
    $scope.search = function (pageNum ,pageSize) {
        brandService.search(pageNum ,pageSize,$scope.searchEntity).success(function (response) {
            $scope.paginationConf.totalItems = response.total; //总记录数
            $scope.list = response.rows;//当前页查询结果集
        })
    };
    //保存品牌
    $scope.save = function () {

        var method = null;
        if ($scope.entity.id != null) {
            //当id存在时,执行修改
            method = brandService.update($scope.entity);
        } else {
            //当id不存在时, 执行新增
            method = brandService.add($scope.entity);
        }

        method.success(function (response) {
            if (response.success) {
                //保存成功, 重新加载分页列表数据
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        })
    }
    //基于ID查询品牌列表
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    }


    //批量删除
    $scope.dele = function () {
        if (confirm('确定要删除吗?')){
            brandService.dele($scope.selectIds).success(function (response) {
                if (response.success) {

                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            })
        }
    }



})