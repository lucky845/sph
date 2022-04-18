package com.atguigu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.dao.ProductMapper;
import com.atguigu.entity.BaseBrand;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.search.*;
import com.atguigu.service.SearchService;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lucky845
 * @date 2022年04月15日
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品的上架
     *
     * @param skuId 商品销售属性id
     */
    @Override
    public void onSale(Long skuId) {
        Product product = new Product();
        // 1. 商品的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo != null) {
            product.setId(skuInfo.getId());
            product.setProductName(skuInfo.getSkuName());
            product.setCreateTime(new Date());
            product.setPrice(skuInfo.getPrice().doubleValue());
            product.setDefaultImage(skuInfo.getSkuDefaultImg());
            // 2. 品牌信息
            Long brandId = skuInfo.getBrandId();
            BaseBrand brand = productFeignClient.getBrandByBrandId(brandId);
            if (brand != null) {
                product.setBrandId(brandId);
                product.setBrandName(brand.getBrandName());
                product.setBrandLogoUrl(brand.getBrandLogoUrl());
            }
            // 3. 商品的分类信息
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
            if (categoryView != null) {
                product.setCategory1Id(categoryView.getCategory1Id());
                product.setCategory1Name(categoryView.getCategory1Name());
                product.setCategory2Id(categoryView.getCategory2Id());
                product.setCategory2Name(categoryView.getCategory2Name());
                product.setCategory3Id(categoryView.getCategory3Id());
                product.setCategory3Name(categoryView.getCategory3Name());
            }
            // 4. 单个商品的平台属性
            List<PlatformPropertyKey> propertyKeyList = productFeignClient.getPlatformPropertyBySkuId(skuId);
            if (!CollectionUtils.isEmpty(propertyKeyList)) {
                List<SearchPlatformProperty> searchPropertyList = propertyKeyList.stream().map(propertyKey -> {
                    SearchPlatformProperty searchPlatformProperty = new SearchPlatformProperty();
                    // 平台属性id
                    searchPlatformProperty.setPropertyKeyId(propertyKey.getId());
                    // 平台属性名称
                    searchPlatformProperty.setPropertyKey(propertyKey.getPropertyKey());
                    // 平台属性值
                    String propertyValue = propertyKey.getPropertyValueList().get(0).getPropertyValue();
                    searchPlatformProperty.setPropertyValue(propertyValue);
                    return searchPlatformProperty;
                }).collect(Collectors.toList());
                product.setPlatformProperty(searchPropertyList);
            }
        }
        // 存储到es中
        productMapper.save(product);
    }

    /**
     * 商品的下架
     *
     * @param skuId 商品销售属性id
     */
    @Override
    public void offSale(Long skuId) {
        // 从es中删除
        productMapper.deleteById(skuId);
    }

    /**
     * 商品的搜索
     *
     * @param searchParam 商品搜索条件对象
     */
    @SneakyThrows
    @Override
    public SearchResponseVo searchProduct(SearchParam searchParam) {
        // 1. 生成DSL语句
        SearchRequest searchRequest = this.buildQueryDSL(searchParam);
        // 2. 通过DSL语句实现查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //System.out.println("searchResponse = " + searchResponse);
        // 3. 通过对查询结果进行解析，并封装到一个对象中
        SearchResponseVo searchResponseVo = this.parseSearchResult(searchResponse);
        // 4. 设置其他参数
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        // 5. 设置总页数
        boolean flag = searchResponseVo.getTotal() % searchParam.getPageSize() == 0;
        long totalPages = 0L;
        if (flag) {
            totalPages = searchResponseVo.getTotal() / searchParam.getPageSize();
        } else {
            totalPages = searchResponseVo.getTotal() / searchParam.getPageSize() + 1;
        }
        searchResponseVo.setTotalPages(totalPages);
        return searchResponseVo;
    }

    /**
     * 解析查询结果并封装到一个对象中
     *
     * @param searchResponse 查询结果对象
     *                       <div>
     *                                {
     *                         "took" : 2,
     *                         "timed_out" : false,
     *                         "_shards" : {
     *                           "total" : 3,
     *                           "successful" : 3,
     *                           "skipped" : 0,
     *                           "failed" : 0
     *                         },
     *                         "hits" : {
     *                           "total" : 14,
     *                           "max_score" : null,
     *                           "hits" : [
     *                             {
     *                               "_index" : "product",
     *                               "_type" : "info",
     *                               "_id" : "26",
     *                               "_score" : null,
     *                               "_source" : {
     *                                 "id" : 26,
     *                                 "defaultImage" : "http://121.89.208.247/group1/M00/00/00/rBSs3WEVAeuEXBm7AAAAABRfYVU308.jpg",
     *                                 "productName" : "Apple iPhone 12苹果智能  (A2404) 256GB 白色 支持移动联通电信5G 双卡双待",
     *                                 "price" : 7599.0,
     *                                 "createTime" : 1628774074649,
     *                                 "brandId" : 1,
     *                                 "brandName" : "苹果",
     *                                 "brandLogoUrl" : "http://121.89.208.247/group1/M00/00/00/rBSs3WEBF6mEDwRYAAAAAIyTSXk800.png",
     *                                 "category1Id" : 2,
     *                                 "category1Name" : "手机",
     *                                 "category2Id" : 13,
     *                                 "category2Name" : "手机通讯",
     *                                 "category3Id" : 61,
     *                                 "category3Name" : "手机",
     *                                 "hotScore" : 0,
     *                                 "platformProperty" : [
     *                                   {
     *                                     "propertyKeyId" : 4,
     *                                     "propertyValue" : "苹果A14",
     *                                     "propertyKey" : "CPU型号"
     *                                   },
     *                                   {
     *                                     "propertyKeyId" : 5,
     *                                     "propertyValue" : "6.0～6.24英寸",
     *                                     "propertyKey" : "屏幕尺寸"
     *                                   }
     *                                 ]
     *                               },
     *                       		"highlight": {
     *                       			"pre_tags": ["<span style=color:red>"],
     *                       			"fields": {"productName": {}},
     *                       			"post_tags": ["</span>"]
     *                                      },
     *                               "sort" : [
     *                                 7599.0
     *                               ]
     *                             },
     *                             {
     *                               "_index" : "product",
     *                               "_type" : "info",
     *                               "_id" : "29",
     *                               "_score" : null,
     *                               "_source" : {
     *                                 "id" : 29,
     *                                 "defaultImage" : "http://121.89.208.247/group1/M00/00/00/rBSs3WEVAeuEYx3wAAAAABxKCKk782.jpg",
     *                                 "productName" : "Apple iPhone 12高端苹果 (A2404) 256GB 白色 支持移动联通电信5G 双卡双待",
     *                                 "price" : 7599.0,
     *                                 "createTime" : 1628774086083,
     *                                 "brandId" : 1,
     *                                 "brandName" : "苹果",
     *                                 "brandLogoUrl" : "http://121.89.208.247/group1/M00/00/00/rBSs3WEBF6mEDwRYAAAAAIyTSXk800.png",
     *                                 "category1Id" : 2,
     *                                 "category1Name" : "手机",
     *                                 "category2Id" : 13,
     *                                 "category2Name" : "手机通讯",
     *                                 "category3Id" : 61,
     *                                 "category3Name" : "手机",
     *                                 "hotScore" : 0,
     *                                 "platformProperty" : [
     *                                   {
     *                                     "propertyKeyId" : 4,
     *                                     "propertyValue" : "苹果A14",
     *                                     "propertyKey" : "CPU型号"
     *                                   },
     *                                   {
     *                                     "propertyKeyId" : 5,
     *                                     "propertyValue" : "6.55-6.64英寸",
     *                                     "propertyKey" : "屏幕尺寸"
     *                                   }
     *                                 ]
     *                               },
     *                               "sort" : [
     *                                 7599.0
     *                               ]
     *                             }
     *                           ]
     *                         },
     *                         "aggregations" : {
     *                           "brandIdAgg" : {
     *                             "doc_count_error_upper_bound" : 0,
     *                             "sum_other_doc_count" : 0,
     *                             "buckets" : [
     *                               {
     *                                 "key" : 1,
     *                                 "doc_count" : 9,
     *                                 "brandLogoUrlAgg" : {
     *                                   "doc_count_error_upper_bound" : 0,
     *                                   "sum_other_doc_count" : 0,
     *                                   "buckets" : [
     *                                     {
     *                                       "key" : "http://121.89.208.247/group1/M00/00/00/rBSs3WEBF6mEDwRYAAAAAIyTSXk800.png",
     *                                       "doc_count" : 9
     *                                     }
     *                                   ]
     *                                 },
     *                                 "brandNameAgg" : {
     *                                   "doc_count_error_upper_bound" : 0,
     *                                   "sum_other_doc_count" : 0,
     *                                   "buckets" : [
     *                                     {
     *                                       "key" : "苹果",
     *                                       "doc_count" : 9
     *                                     }
     *                                   ]
     *                                 }
     *                               },
     *                               {
     *                                 "key" : 3,
     *                                 "doc_count" : 5,
     *                                 "brandLogoUrlAgg" : {
     *                                   "doc_count_error_upper_bound" : 0,
     *                                   "sum_other_doc_count" : 0,
     *                                   "buckets" : [
     *                                     {
     *                                       "key" : "http://121.89.208.247/group1/M00/00/00/rBSs3WEBF6CEAdKWAAAAAOT5oIk435.png",
     *                                       "doc_count" : 5
     *                                     }
     *                                   ]
     *                                 },
     *                                 "brandNameAgg" : {
     *                                   "doc_count_error_upper_bound" : 0,
     *                                   "sum_other_doc_count" : 0,
     *                                   "buckets" : [
     *                                     {
     *                                       "key" : "三星",
     *                                       "doc_count" : 5
     *                                     }
     *                                   ]
     *                                 }
     *                               }
     *                             ]
     *                           },
     *                           "platformPropertyAgg" : {
     *                             "doc_count" : 28,
     *                             "propertyKeyIdAgg" : {
     *                               "doc_count_error_upper_bound" : 0,
     *                               "sum_other_doc_count" : 0,
     *                               "buckets" : [
     *                                 {
     *                                   "key" : 4,
     *                                   "doc_count" : 14,
     *                                   "propertyKeyAgg" : {
     *                                     "doc_count_error_upper_bound" : 0,
     *                                     "sum_other_doc_count" : 0,
     *                                     "buckets" : [
     *                                       {
     *                                         "key" : "CPU型号",
     *                                         "doc_count" : 14
     *                                       }
     *                                     ]
     *                                   },
     *                                   "propertyValueAgg" : {
     *                                     "doc_count_error_upper_bound" : 0,
     *                                     "sum_other_doc_count" : 0,
     *                                     "buckets" : [
     *                                       {
     *                                         "key" : "苹果A14",
     *                                         "doc_count" : 6
     *                                       },
     *                                       {
     *                                         "key" : "骁龙888",
     *                                         "doc_count" : 5
     *                                       },
     *                                       {
     *                                         "key" : "三星Exynos",
     *                                         "doc_count" : 3
     *                                       }
     *                                     ]
     *                                   }
     *                                 },
     *                                 {
     *                                   "key" : 5,
     *                                   "doc_count" : 14,
     *                                   "propertyKeyAgg" : {
     *                                     "doc_count_error_upper_bound" : 0,
     *                                     "sum_other_doc_count" : 0,
     *                                     "buckets" : [
     *                                       {
     *                                         "key" : "屏幕尺寸",
     *                                         "doc_count" : 14
     *                                       }
     *                                     ]
     *                                   },
     *                                   "propertyValueAgg" : {
     *                                     "doc_count_error_upper_bound" : 0,
     *                                     "sum_other_doc_count" : 0,
     *                                     "buckets" : [
     *                                       {
     *                                         "key" : "5.0英寸以下",
     *                                         "doc_count" : 5
     *                                       },
     *                                       {
     *                                         "key" : "5.0～5.49英寸",
     *                                         "doc_count" : 4
     *                                       },
     *                                       {
     *                                         "key" : "6.55-6.64英寸",
     *                                         "doc_count" : 3
     *                                       },
     *                                       {
     *                                         "key" : "6.0～6.24英寸",
     *                                         "doc_count" : 2
     *                                       }
     *                                     ]
     *                                   }
     *                                 }
     *                               ]
     *                             }
     *                           }
     *                         }
     *                       }
     *                       </div>
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        // 1. 拿到商品的基本信息
        SearchHits firstHits = searchResponse.getHits();
        // 总记录数
        searchResponseVo.setTotal(firstHits.getTotalHits());
        // 商品的基本信息
        SearchHit[] secondHits = firstHits.getHits();
        if (secondHits != null && secondHits.length > 0) {
            for (SearchHit secondHit : secondHits) {
                String source = secondHit.getSourceAsString();
                // 获取到的product对象没有高亮的productName
                Product product = JSONObject.parseObject(source, Product.class);
                HighlightField highlightField = secondHit.getHighlightFields().get("productName");
                if (highlightField != null) {
                    String highlightProductName = highlightField.getFragments()[0].toString();
                    // 设置product的高亮信息
                    product.setProductName(highlightProductName);
                }
                // 将每一个product添加到productList中
                searchResponseVo.getProductList().add(product);
            }
        }

        // 2. 拿到商品的品牌信息
        ParsedLongTerms brandIdAgg = searchResponse.getAggregations().get("brandIdAgg");
        List<SearchBrandVo> searchBrandVoList = brandIdAgg.getBuckets().stream().map(bucket -> {
            SearchBrandVo searchBrandVo = new SearchBrandVo();
            // 获取品牌id
            Number brandId = bucket.getKeyAsNumber();
            searchBrandVo.setBrandId(brandId.longValue());
            // 获取品牌名称
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brandNameAgg");
            List<? extends Terms.Bucket> brandNameBucketList = brandNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(brandNameBucketList)) {
                String brandName = brandNameBucketList.get(0).getKeyAsString();
                searchBrandVo.setBrandName(brandName);
            }
            // 获取品牌图片
            ParsedStringTerms brandLogoUrlAgg = bucket.getAggregations().get("brandLogoUrlAgg");
            List<? extends Terms.Bucket> brandLogoUrlList = brandLogoUrlAgg.getBuckets();
            if (!CollectionUtils.isEmpty(brandLogoUrlList)) {
                String brandLogoUrl = brandLogoUrlList.get(0).getKeyAsString();
                searchBrandVo.setBrandLogoUrl(brandLogoUrl);
            }
            return searchBrandVo;
        }).collect(Collectors.toList());
        searchResponseVo.setBrandVoList(searchBrandVoList);

        // 3. 拿到商品的平台属性
        ParsedNested platformPropertyAgg = searchResponse.getAggregations().get("platformPropertyAgg");
        ParsedLongTerms propertyKeyIdAgg = platformPropertyAgg.getAggregations().get("propertyKeyIdAgg");
        List<SearchPlatformPropertyVo> SearchPlatformPropertyVoList = propertyKeyIdAgg.getBuckets().stream().map(bucket -> {
            SearchPlatformPropertyVo searchPlatformPropertyVo = new SearchPlatformPropertyVo();
            // 获取商品的平台属性id
            Number propertyKeyId = bucket.getKeyAsNumber();
            searchPlatformPropertyVo.setPropertyKeyId(propertyKeyId.longValue());
            // 属性名称
            ParsedStringTerms propertyKeyAgg = bucket.getAggregations().get("propertyKeyAgg");
            List<? extends Terms.Bucket> propertyKeyBucketList = propertyKeyAgg.getBuckets();
            if (!CollectionUtils.isEmpty(propertyKeyBucketList)) {
                String propertyKey = propertyKeyBucketList.get(0).getKeyAsString();
                searchPlatformPropertyVo.setPropertyKey(propertyKey);
            }
            // 当前属性值的集合
            ParsedStringTerms propertyValueAgg = bucket.getAggregations().get("propertyValueAgg");
            List<String> propertyValueList = propertyValueAgg.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchPlatformPropertyVo.setPropertyValueList(propertyValueList);
            return searchPlatformPropertyVo;
        }).collect(Collectors.toList());
        searchResponseVo.setPlatformPropertyList(SearchPlatformPropertyVoList);
        return searchResponseVo;
    }

    /**
     * 生成DSL语句
     *
     * @param searchParam 商品搜索条件对象<br />
     *                    <div>
     *                    GET /product/_search
     *                    {
     *                    	"query": {
     *                    		"bool": {
     *                    			"filter": [
     *                                                   {
     *                    					"term": {
     *                    						"category3Id": "61"
     *                                       }
     *                                   },
     *                                   {
     *                    					"term": {
     *                    						"brandId": "1"
     *                                       }
     *                                   },
     *                                   {
     *                    					"bool": {
     *                    						"must": [
     *                                               {
     *                    								"nested": {
     *                    									"path": "platformProperty",
     *                    									"query": {
     *                    										"bool": {
     *                    											"must": [
     *                                                                   {
     *                    													"term": {
     *                    														"platformProperty.propertyKeyId": {
     *                    															"value": "4"
     *                                                                           }
     *                                                                       }
     *                                                                   },
     *                                                                   {
     *                    													"term": {
     *                    														"platformProperty.propertyValue": {
     *                    															"value": "苹果A14"
     *                                                                           }
     *                                                                       }
     *                                                                   }
     *                    											]
     *                                                           }
     *                                                       }
     *                                                   }
     *                                               }
     *                    						]
     *                                       }
     *                                   }
     *                    			],
     *                    			"must": [
     *                                   {
     *                    					"match": {
     *                    						"productName": {
     *                    							"query": "智能苹果",
     *                    							"operator": "and"
     *                                           }
     *                                       }
     *                                   }
     *                    			]
     *                            }
     *                        },
     *                    	"from": 0,
     *                    	"size": 20,
     *                    	"sort": [
     *                            {
     *                    			"hotScore": {
     *                    				"order": "desc"            * 			}
     *                           }
     *                    	],
     *                    	"highlight": {
     *                    		"fields": {
     *                    			"productName": {}
     *                           },
     *                    		"post_tags": [
     *                    			"</span>"
     *                    		],
     *                    		"pre_tags": [
     *                    			"<span style=color:red>"
     *                    		]    * 	},
     *                    	"aggregations": {
     *                    		"brandIdAgg": {
     *                    			"terms": {
     *                    				"field": "brandId"
     *                                }            ,
     *                    			"aggregations": {
     *                    				"brandNameAgg": {
     *                    					"terms": {
     *                    						"field": "brandName",
     *                    						"size": 10
     *                                       }
     *                                   },
     *                    				"brandLogoUrlAgg": {
     *                    					"terms": {
     *                    						"field": "brandLogoUrl",
     *                    						"size": 10
     *                                       }
     *                                   }
     *                               }
     *                           },
     *                    		"platformPropertyAgg": {
     *                    			"nested": {
     *                    				"path": "platformProperty"
     *                               },
     *                    			"aggregations": {
     *                    				"propertyKeyIdAgg": {
     *                    					"terms": {
     *                    						"field": "platformProperty.propertyKeyId",
     *                    						"size": 10
     *                                       },
     *                    					"aggregations": {
     *                    						"propertyKeyAgg": {
     *                    							"terms": {
     *                    								"field": "platformProperty.propertyKey",
     *                    								"size": 10
     *                                               }
     *                                           },
     *                    						"propertyValueAgg": {
     *                    							"terms": {
     *                    								"field": "platformProperty.propertyValue",
     *                    								"size": 10
     *                                               }
     *                                           }
     *                                       }
     *                                   }
     *                               }
     *                           }    * 	}
     *                    }
     *                    </div>
     * @return
     */
    private SearchRequest buildQueryDSL(SearchParam searchParam) {
        // 1. 构建一个query
        SearchSourceBuilder esSqlBuilder = new SearchSourceBuilder();
        // 2. 构建第一个bool
        BoolQueryBuilder firstBool = QueryBuilders.boolQuery();
        // 3. 构建一个分类过滤器
        Long category1Id = searchParam.getCategory1Id();
        if (!StringUtils.isEmpty(category1Id)) {
            // 构建一级分类过滤器
            TermQueryBuilder category1IdTerm = QueryBuilders.termQuery("category1Id", category1Id);
            firstBool.filter(category1IdTerm);
        }
        Long category2Id = searchParam.getCategory2Id();
        if (!StringUtils.isEmpty(category2Id)) {
            // 构建二级分类过滤器
            TermQueryBuilder category2IdTerm = QueryBuilders.termQuery("category2Id", category2Id);
            firstBool.filter(category2IdTerm);
        }
        Long category3Id = searchParam.getCategory3Id();
        if (!StringUtils.isEmpty(category3Id)) {
            // 构建三级分类过滤器
            TermQueryBuilder category3IdTerm = QueryBuilders.termQuery("category3Id", category3Id);
            firstBool.filter(category3IdTerm);
        }
        // 4. 构建一个品牌过滤器
        String brandName = searchParam.getBrandName();
        if (!StringUtils.isEmpty(brandName)) {
            // 构建品牌过滤器
            String[] brandSplit = brandName.split(":");
            if (brandSplit.length == 2) {
                String brandId = brandSplit[0];
                TermQueryBuilder brandIdTerm = QueryBuilders.termQuery("brandId", brandId);
                firstBool.filter(brandIdTerm);
            }
        }

        // 5. 构造关键字查询
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("productName", keyword).operator(Operator.OR);
            firstBool.must(matchQuery);
        }

        // 6. 构造平台属性过滤器
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                // 切分获取到每一个平台属性键值对(如： 4:骁龙888:CPU型号)
                String[] platformPrams = prop.split(":");
                if (platformPrams.length == 3) {
                    BoolQueryBuilder secondBool = QueryBuilders.boolQuery();
                    BoolQueryBuilder childBool = QueryBuilders.boolQuery();
                    childBool.must(QueryBuilders.termQuery("platformProperty.propertyKeyId", platformPrams[0]));
                    childBool.must(QueryBuilders.termQuery("platformProperty.propertyValue", platformPrams[1]));
                    secondBool.must(QueryBuilders.nestedQuery("platformProperty", childBool, ScoreMode.None));
                    firstBool.filter(secondBool);
                }
            }
        }
        // 把firstBool放到query中
        esSqlBuilder.query(firstBool);

        // 7. 构造分页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        esSqlBuilder.from(from);
        esSqlBuilder.size(searchParam.getPageSize());

        // 8. 构建排序(综合排序(hotScore)、价格排序(price))
        String pageOrder = searchParam.getOrder();
        if (!StringUtils.isEmpty(pageOrder)) {
            String[] orderParams = pageOrder.split(":");
            if (orderParams.length == 2) {
                String fieldName = "";
                switch (orderParams[0]) {
                    case "1":
                        fieldName = "hotScore";
                        break;
                    case "2":
                        fieldName = "price";
                        break;
                }
                esSqlBuilder.sort(fieldName, "asc".equals(orderParams[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        } else {
            // 如果没有选择排序方式，默认按照综合排序降序排列
            esSqlBuilder.sort("hotScore", SortOrder.DESC);
        }

        // 9. 构建高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.field("productName");
        highlightBuilder.postTags("</span>");
        esSqlBuilder.highlighter(highlightBuilder);

        // 10. 聚合
        // 10.1 构造品牌聚合
        esSqlBuilder.aggregation(
                AggregationBuilders
                        .terms("brandIdAgg")
                        .field("brandId")
                        .subAggregation(
                                AggregationBuilders
                                        .terms("brandNameAgg")
                                        .field("brandName")
                        )
                        .subAggregation(
                                AggregationBuilders
                                        .terms("brandLogoUrlAgg")
                                        .field("brandLogoUrl")
                        )
        );

        // 10.2 构建平台属性聚合
        esSqlBuilder.aggregation(
                AggregationBuilders
                        .nested("platformPropertyAgg", "platformProperty")
                        .subAggregation(
                                AggregationBuilders
                                        .terms("propertyKeyIdAgg")
                                        .field("platformProperty.propertyKeyId")
                                        .subAggregation(
                                                AggregationBuilders
                                                        .terms("propertyKeyAgg")
                                                        .field("platformProperty.propertyKey")
                                        )
                                        .subAggregation(
                                                AggregationBuilders
                                                        .terms("propertyValueAgg")
                                                        .field("platformProperty.propertyValue")
                                        )
                        )
        );

        // 11. 查询哪个index和type
        SearchRequest searchRequest = new SearchRequest("product");
        searchRequest.types("info");
        searchRequest.source(esSqlBuilder);
        //System.out.println("拼接好的DSL语句： " + esSqlBuilder.toString());
        return searchRequest;
    }
}
