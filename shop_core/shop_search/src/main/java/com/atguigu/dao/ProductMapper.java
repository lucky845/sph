package com.atguigu.dao;

import com.atguigu.search.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author lucky845
 * @date 2022年04月15日
 */
public interface ProductMapper extends ElasticsearchRepository<Product, Long> {
}
