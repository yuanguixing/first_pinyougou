app.controller("indexController", function ($scope, $controller, loginService) {

    $controller("baseController", {$scope:$scope});

    $scope.getName = function () {
        loginService.getName().success(function (response) {
            //获取value {"loginName":"admin"}
            $scope.loginName = response.loginName ;
        })
    }

});