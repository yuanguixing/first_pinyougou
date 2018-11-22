app.controller("payController", function ($scope, $controller, $location,payService) {

    $controller("baseController", {$scope: $scope});

    //生成二维码
    $scope.createNative = function () {
        payService.createNative().success(function (response) {
            //展示订单号和支付金额
            $scope.out_trade_no = response.out_trade_no;
            $scope.total_fee = (response.total_fee / 100).toFixed(2);
            //将支付链接生成二维码
            var qr = window.qr = new QRious({
                element: document.getElementById('qrious'),
                size: 250,
                value: response.code_url, //生成二维码的内容
                level: 'H'
            })
            $scope.queryPayStatus();//一进页面就查询支付状态
        })
    };

    //查询支付状态
    $scope.queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(function (response) {
            if (response.success) {
                //支付成功
                location.href="paysuccess.html#?money="+$scope.total_fee;
            } else {
                if(response.message=="timeout"){
                    $scope.createNative();
                }
                //支付失败
                location.href="payfail.html";
            }
        })
    }
//支付成功页面获取支付金额方法
    $scope.getMoney = function () {
        $scope.money = $location.search()["money"];
    }

});