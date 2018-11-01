app.service("specificationService", function ($http) {

    //条件分页查询
    this.search = function (pageNum, pageSize, searchEntity) {
        return $http.post("../specification/search.do?pageNum=" + pageNum + "&pageSize=" + pageSize, searchEntity);
    }

    //新增
    this.add = function (entity) {
        return $http.post("../specification/add.do", entity);
    };
    //修改
    this.update = function (entity) {
        return $http.post("../specification/update.do", entity);
    };

    //修改前查询
    this.findOne = function (id) {
        return $http.get("../specification/findOne.do?id=" + id);
    };

    this.dele = function (ids) {
        return $http.get("../specification/dele.do?ids=" + ids);
    };

});