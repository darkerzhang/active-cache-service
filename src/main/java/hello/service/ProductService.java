package hello.service;

import hello.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Product create(Product request);

    Page<Product> list(Pageable pageable);

    Page<Product> listByCategoryId(Long categoryId, Pageable pageable);

    Product get(Long id);

    Product update(Long id, Product request);

    void delete(Long id);
}
