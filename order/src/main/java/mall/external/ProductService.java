package mall.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name="product", url="http://localhost:8084")
public interface ProductService {

    @RequestMapping(method= RequestMethod.GET, path="/products/checkAndModifyStock")
    public boolean checkAndModifyStock(
        @RequestParam("productId") String productId, 
        @RequestParam("qty") int qty);

}