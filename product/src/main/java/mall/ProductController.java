package deliveryorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

 @RestController
 public class ProductController {

    @Autowired 
    ProductRepository productRepository;

    @RequestMapping(value = "/products/checkAndModifyStock",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public boolean checkAndModifyStock(@RequestParam("productId") Long productId, 
                                        @RequestParam("qty")  int qty)                        

            throws Exception {

            System.out.println("##### /products checked product Status #####");

            boolean status = false;
            Optional<Product> productOptional = productRepository.findByProductId(productId);
            Product product = productOptional.get();

            if (product.getStock() >= qty) {
                    status = true;
                    product.setStock(product.getStock() - qty);
                    productRepository.save(product);
                    
            System.out.println("##### Order Accepted #####");

            }
            return status;
    }   

 }
