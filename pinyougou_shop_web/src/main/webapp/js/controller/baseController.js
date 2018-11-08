app.controller("baseController", function ($scope) {
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
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
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
    };
    //是否选中为了翻页后回来还能勾选上
    $scope.isChecked = function (id) {
        if ($scope.selectIds.indexOf(id) != -1) {
            return true;
        }
        return false;
    }
    //去数组中对象的属性值,拼接为字符串
    $scope.selectValueByKey = function (jsonString, key) {
        //[{"id":1,"text":"联想"},{"id":3,"text":"三星"}]
        var value = "";
        var objList = JSON.parse(jsonString);
        for (var i = 0; i < objList.length; i++) {
            //从json对象中, 根据属性名取属性值得方式, 有两种方法
            //1. 如果属性名是确定值, 对象.属性名
            //2.如果属性名是变量,    取值方式为 对象[属性名]
            if (i == 0) {
                value += objList[i][key];
            } else {
                value += "," + objList[i][key];
            }
        }
        return value;
    };
    //根据规格的属性名称判断该规格对象是否存在规格列表中 (存在于json格式的对象中)
    /* [{"attributeName":"网络制式","attributeValue":["移动3G","移动4G","联通3G"]},
        {"attributeName":"屏幕尺寸","attributeValue":["5.5寸","6寸"]}] */
    //参数一: json格式的数组对象  参数二: 数组中对象的属性名 参数三:数组中对象的属性值
    $scope.getObjectByName = function (list, key, value) {
        for(var i = 0; i< list.length; i++) {
            //基于对象属性值, 判断该对象是否存在
            if (list[i][key] == value) {
                return list[i];
                break;
            }
        }
        return null;

    }

});