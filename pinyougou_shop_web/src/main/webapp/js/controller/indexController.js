app.controller("indexController", function ($scope, $controller, shopLoginService) {

    $controller("baseController", {$scope:$scope});

    $scope.getName = function () {
        shopLoginService.getName().success(function (response) {
            //获取value {"loginName":"admin"}
            $scope.loginName = response.loginName ;
        })
    }

});