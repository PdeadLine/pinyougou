package com.pinyougou.page.service;


/**
* @Description:商品详情页接口
* @CopyRight (C)
* @author: SintaiZixun
* @date: 18/12/05-10:06
*/
public interface ItemPageService {

    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除生成的静态页面
     * @param ids
     * @return
     */
    public boolean deleteItemHtml(Long[] ids);


}
