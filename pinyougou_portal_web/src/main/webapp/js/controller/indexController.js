app.controller('indexController', function ($scope, $controller, contentService) {

    $controller('baseController', {$scope: $scope});

    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList = response;
        })
    }
       // 关键字搜索
    $scope.search =function () {
        //注意,这是angularjs路由地址传参(ngRute)  时, 需要在拼接参数的问号前加#号
        location.href="http://search.pinyougou.com/search.html#?keywords="+$scope.keywords;
    }

});
