package hello.web;

import hello.domain.Product;
import hello.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ModelMapper modelMapper;

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public Page<Product> list(final @RequestParam(required = false) Long categoryId, final Pageable pageable) {
        if (categoryId == null) {
            return productService.list(pageable);
        } else {
            return productService.listByCategoryId(categoryId, pageable);
        }
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET)
    public Product get(final @PathVariable Long id) {
        return productService.get(id);
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.PUT)
    public Product update(final @PathVariable Long id, @RequestBody Product request) throws Exception {
        Product product = productService.get(id);
        modelMapper.map(request, product);
        productService.update(id, product);
        return product;
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.DELETE)
    public void delete(final @PathVariable Long id) {
        productService.delete(id);
    }
}
