app.service("contentService",function ($http) {
    //根据分类id查询广告列表
    this.findByCategoryId=function (categoryId) {
        return $http.get("content/findByCategoryId.do?categoryId="+categoryId);
    }

});