app.service("uploadService", function ($http) {

    //文件上传的方法
    this.uploadFile = function () {

        //angularjs结合html完成文件上传
        //html5中FastData将上传文件作为提交参数, 提交给后端
        var formData = new FormData();
        //获取页面选择的文件, 并追加FromData对象中 参数一: 提交值, 与后端接收文件对象参数名称相同
        //参数二: file.file[0] file与 页面的表单id 值对应
        formData.append("file",file.files[0]);

        //发起请求, 完成文件上传
        return $http({
            method:"post",
            url:"../upload/uploadFile.do",
            data:formData,
            headers:{'Content-Type':undefined},//会转换  相当于 multipart/form-data
            transformRequest:angular.identity
        });

    }
});