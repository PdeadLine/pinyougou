package com.pinyougou.search.service.iml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;
	@Override
	public Map search(Map searchMap) {
		Map<String, Object> map = new HashMap<>();
		//1.按关键字查询（高亮显示）
		map.putAll(searchList(searchMap));

		//2.根据关键字分组查询商品分类
		List categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);

		//3.查询品牌和规格列表
		String category= (String) searchMap.get("category");
		if(!category.equals("")){
			map.putAll(searchBrandAndSpecList(category));
		}else{
			if(categoryList.size()>0){
				map.putAll(searchBrandAndSpecList(categoryList.get(0).toString()));
			}
		}
		return map;
	}

	/**
	 * 根据关键字搜索列表
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap) {
		Map map=new HashMap();
//		Query query=new SimpleQuery("*:*");
		HighlightQuery query = new SimpleHighlightQuery();//高亮查询
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);//高亮查询

		//1.1关键字查询
		Criteria criteria=new Criteria("item_title").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		//1.3 按品牌过滤
		if(!"".equals(searchMap.get("brand"))  )	{//如果用户选择了品牌
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key :specMap.keySet()){

				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		//#######################取高亮#################################
		//取高亮查询列表
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		for (HighlightEntry<TbItem>h : page.getHighlighted()) {
			TbItem item = h.getEntity();
			if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮字段
			}
		}
//		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		map.put("rows", page.getContent());
		return map;
	}

	private List searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		Query query = new SimpleQuery();
		//按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组查询选项
		GroupOptions options = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(options);
		//等到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for(GroupEntry<TbItem> entry:content){
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
		}
		return list;
	}

	private Map searchBrandAndSpecList(String category) {
		Map map=new HashMap();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
		if(typeId!=null){
			//根据模板ID查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);//返回值添加品牌列表
			System.out.println("品牌列表条数："+brandList.size());
			//根据模板ID查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
			System.out.println("规格列表条数："+specList.size());
		}
		return map;
	}

}
