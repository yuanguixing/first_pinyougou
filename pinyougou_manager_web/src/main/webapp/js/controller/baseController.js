app.controller("baseController",function ($scope) {
    //分页组件
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };
    // 分页+ 条件查询
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    };

    //记录选中的id的数组
    $scope.selectIds = [];
    $scope.updateSelection = function ($event, id) {
        //判断复选框勾选状态
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);
        }
    }
    //是否选中为了翻页后回来还能勾选上
    $scope.isChecked = function(id){
        if($scope.selectIds.indexOf(id)!= -1){
            return true;
        }
        return false;
    }
});