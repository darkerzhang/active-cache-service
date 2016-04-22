package hello.service.impl;

import hello.domain.Product;
import hello.repository.ProductRepository;
import hello.service.CacheService;
import hello.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CacheService<Product> productCache;
    @Autowired
    private CacheService<String> productListCache;

    public Product create(Product request) {
        Product product = productRepository.save(request);
        String key = "products#" + product.getId();
        productCache.put(key, product);
        productListCache.delete("products#listByCategoryId#categoryId=" + product.getCategoryId());
        productListCache.delete("products#list");
        return product;
    }

    public Page<Product> list(Pageable pageable) {
        String hash = "products#list";
        String key = "page=" + pageable.getPageNumber() + "#size=" + pageable.getPageSize();
        if (productListCache.hhas(hash, key)) {
            logger.debug("List from cache");
            return getListFromIds(pageable, hash, key);
        } else {
            logger.debug("List from db");
            Page<Product> products = productRepository.findAll(pageable);
            putIdsFromList(products, hash, key);
            return products;
        }
    }

    public Page<Product> listByCategoryId(Long categoryId, Pageable pageable) {
        String hash = "products#listByCategoryId#categoryId=" + categoryId;
        String key = "#page=" + pageable.getPageNumber() + "#size=" + pageable.getPageSize();
        if (productListCache.hhas(hash, key)) {
            logger.debug("ListByCategoryId from cache");
            return getListFromIds(pageable, hash, key);
        } else {
            logger.debug("ListByCategoryId from db");
            Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
            putIdsFromList(products, hash, key);
            return products;
        }
    }

    public Product get(Long id) {
        String key = "products#" + id;
        if (productCache.has(key)) {
            logger.debug("Get from cache");
            return productCache.get(key);
        } else {
            logger.debug("Get from db");
            Product product = productRepository.findOne(id);
            productCache.put(key, product);
            return product;
        }
    }

    public Product update(Long id, Product request) {
        String key = "products#" + id;
        Product product = productRepository.findOne(id);
        BeanUtils.copyProperties(request, product);
        product = productRepository.save(product);
        productCache.put(key, product);
        return product;
    }

    public void delete(Long id) {
        String key = "products#" + id;
        Product product = productRepository.findOne(id);
        productCache.delete(key);
        productListCache.delete("products#listByCategoryId#categoryId=" + product.getCategoryId());
        productListCache.delete("products#list");
        productRepository.delete(id);
    }

    // FIXME: 数据总个数存在列表第一项
    private Page<Product> getListFromIds(Pageable pageable, String hash, String key) {
        String string = productListCache.hget(hash, key);
        List<String> ids = Arrays.asList(string.split(","));
        List<Product> products = productCache.mget(ids.subList(1, ids.size()).stream().map(id -> "products#" + id).collect(Collectors.toList()));
        return new PageImpl<>(products, pageable, Long.parseLong(ids.get(0)));
    }

    private void putIdsFromList(Page<Product> products, String hash, String key) {
        List<Long> ids = products.getContent().stream().map(Product::getId).collect(Collectors.toList());
        ids.add(0, products.getTotalElements());
        StringJoiner stringJoiner = new StringJoiner(",");
        ids.forEach(id -> stringJoiner.add(id + ""));
        productListCache.hput(hash, key, stringJoiner.toString());
        products.forEach(product -> {
            if (!productCache.has("products#" + product.getId())) {
                productCache.put("products#" + product.getId(), product);
            }
        });
    }
}
