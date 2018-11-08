//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, itemCatService, typeTemplateService, uploadService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //新增 goods_edit
    $scope.add = function () {
        //获取商品介绍编辑器中的HTML内容, 赋予goodsDesc.introduction
        $scope.entity.goodsDesc.introduction = editor.html();

        goodsService.add($scope.entity).success(function (response) {
            if (response.success) {
                //清除录入商品信息时输入的内容
                $scope.entity = {};
                //清除商品介绍编辑框中的内容
                editor.html("");
            } else {
                alert(response.message);
            }
        });
    }
    //查询商品一级分类方法
    $scope.selectItemCatList = function () {

        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    }
    //基于一级分类查询对应的二级分类
    //参数一:监控的值, 参数二:监控内容发生后,做的事情
    //ordValue: 之前的值  newValue 之后的值
    $scope.$watch("entity.goods.category1Id", function (newValue, ordValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = [];
            $scope.entity.goods.typeTemplateId = "";
            $scope.itemCat2List = response;
        })
    });

    //基于二级分类查询对应的三级分类
    $scope.$watch("entity.goods.category2Id", function (newValue, ordValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = "";
            $scope.itemCat3List = response;
        })
    })
    //基于三级分类查询对应的规格ID
    $scope.$watch("entity.goods.category3Id", function (newValue, ordValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
        })
    })
    //基于模板变化查询对应的品牌和规格选项等数据
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, ordValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            //模板对象
            //处理模板关联的品牌数据
            $scope.brandList = JSON.parse(response.brandIds);
            //处理模板关联的扩展属性数据 原先数据是字符串 "[{"text":"内存大小"},{"text":"颜色"}]"
            //解析后是对象数组 [{"text":"内存大小"},{"text":"颜色"}]
            $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
        })
        //查询模板关联的规格选项列表
        typeTemplateService.findSpecList(newValue).success(function (response) {
            $scope.specList = response;
        })

    })
    $scope.image_entity = {};
    //文件上传
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {
                //接受图片地址
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message);
            }
        })
    }

    //初始化保存商品的组合实体类对象
    $scope.entity = {goods: {isEnableSpec:"1"}, goodsDesc: {itemImages: [], specificationItems: []}, itemList: []};
    //图片保存时,将图片添加到图片列表中
    $scope.addImageEntity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);

    }

    //删除图片列表中的对象
    $scope.deleImageEntity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    };

    //规格选项勾选和取消勾选组装规格结果集功能
    $scope.updateSpecAttribute = function ($event, specName, specOption) {
        //根据规格名称判断该对象是否存在于json格式的数组对象
        var specObject = $scope.getObjectByName($scope.entity.goodsDesc.specificationItems, "attributeName", specName);
        if (specObject != null) {
            //存在对象, 判断勾选还是取消勾选
            if ($event.target.checked) {
                specObject.attributeValue.push(specOption);
            } else {
                //取消勾选规格选项,移除规格选项
                var index = specObject.attributeValue.indexOf(specOption);
                specObject.attributeValue.splice(index, 1);
                //如果全部取消规格对应的规格选项数据
                if (specObject.attributeValue.length == 0) {
                    //将全部取消规格选项的规格对象从规格结果集中移除
                    var index1 = $scope.entity.goodsDesc.specificationItems.indexOf(specObject);
                    $scope.entity.goodsDesc.specificationItems.splice(index1, 1);
                }
            }

        } else {
            //不存在 新增规格对象
            $scope.entity.goodsDesc.specificationItems.push({
                "attributeName": specName,
                "attributeValue": [specOption]
            });
        }

    };

    //组装sku列表数据
    $scope.createItemList = function () {
        //初始化sku列表对象
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "1", isDefault: "0"}];
        //考虑组装spec {"机身内存","16g","网络":"联通4g"}
        //组装spec对象的值 和 勾选的规格结果集有关
        var specList = $scope.entity.goodsDesc.specificationItems;
        if (specList.length == 0) {
        $scope.entity.itemList=[];
        }
        for (var i = 0; i < specList.length; i++) {
            //基于深克隆 ,构建itemList中对象的spec属性
            $scope.entity.itemList = addColumn($scope.entity.itemList, specList[i].attributeName, specList[i].attributeValue);
        }
    }
    //构建sku列表行与列数据
    addColumn = function (list, specName, specOptions) {
        //声明新的sku列表
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var ordItem = list[i];
            //遍历勾选的规格选项集合
            for (var j = 0; j < specOptions.length; j++) {
                //基于深克隆创建新的sku对象
                var newItem = JSON.parse(JSON.stringify(ordItem));
                newItem.spec[specName] = specOptions[j];
                newList.push(newItem);
            }
        }
        return newList;
    }
    /* //复选框的实现方法一 ng-click=updateAttribute($event,$index)
                       方法二: ng-true-value="1" ng-false-value="0"
   $scope.updateAttribute = function ($event,$index) {
        if($event.target.checked) {
            $scope.entity.itemList[index].status="1";
        }else{
            $scope.entity.itemList[index].status="0";
        }
    }*/
    //定义商品状态的数组
    $scope.status = ["未审核","已审核","审核未通过","关闭"];

    //商品上下架
    $scope.updateIsMarketable = function (isMarketable) {
        goodsService.updateIsMarketable($scope.selectIds,isMarketable).success(function (response) {
            if (response.success) {
                $scope.reloadList();
            }else {
                alert(response.message);
            }
        })
    }
    //定义商品上下架状态的数组
    $scope.isMarketable = ["上架","下架"];

    $scope.itemCatList= [];

    //查询所有分类列表数据
    $scope.findAllItemCatList = function () {
        //response = [{id:1, name:手机},{id:2, name:电视}];
        itemCatService.findAll().success(function (response) {
            for (var i = 0;i<response.length; i++) {
                //将分类ID作为列表索引值 ,将分类名称作为该索引对象的列表对象值
                $scope.itemCatList[response[i].id] = response[i].name;
            }
        });
    }

});
