app.controller("specificationController", function ($scope, $controller, specificationService) {

    $controller("baseController", {$scope: $scope});

    //条件分页查询
    $scope.searchEntity = {};
    $scope.search = function (pageNum, pageSize) {
        specificationService.search(pageNum, pageSize, $scope.searchEntity).success(function (response) {
            $scope.paginationConf.totalItems = response.total;
            $scope.list = response.rows;
        })
    };

    $scope.entity = {};
    //新增
    $scope.save = function () {

        var method = null;
        if ($scope.entity.specification.id != null) {
            method = specificationService.update($scope.entity);
        } else {
            method = specificationService.add($scope.entity);
        }
        method.success(function (response) {
            if (response.success) {
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        })
    };

    $scope.findOne = function (id) {
        specificationService.findOne(id).success(function (response) {
            $scope.entity = response;
        });
    }

    $scope.add = function () {
        specificationService.add($scope.entity).success(function (response) {
            if (response.success) {
                if ($scope.searchEntity != null) {
                    $scope.searchEntity = null;
                }
                $scope.reloadList();

            } else {
                alert(response.message);
            }
        })
    }

    //$scope.entity = {specificationOptions: []};
    $scope.addRow = function () {
        $scope.entity.specificationOptions.push({});
    }

    $scope.deleRow = function (index) {
        $scope.entity.specificationOptions.splice(index, 1);
    }


    //删除
    $scope.dele = function () {
        if (confirm("你确定要删除吗?")){
            specificationService.dele($scope.selectIds).success(function (response) {
                if (response.success) {
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            });
        }
    }

});