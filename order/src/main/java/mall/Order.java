package deliveryorder;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String productId;
    private Integer qty;        
    private String status = "Ordered";

    @PrePersist
    public void onPrePersist(){
        
      //임의의 부하를 위한 강제 설정  
      try {
          Thread.currentThread().sleep((long) (400 + Math.random() * 220));
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
    }
    
    @PostPersist
    public void onPostPersist() throws Exception {
        boolean rslt = OrderApplication.applicationContext.getBean(deliveryorder.external.ProductService.class)
        .checkAndModifyStock(this.getProductId(), this.getQty());

        if (rslt) {
            Ordered ordered = new Ordered();
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
        } else
        System.out.println("########### Out of Stock !! #######");
            //throw new Exception("Out of Stock Exception Raised.");
    }

    @PostUpdate
    public void onPostUpdate() {
        System.out.println("########### Order Update Event raised...!! #######");
    }

    @PreRemove
    public void onPreRemove(){
        OrderCancelled orderCancelled = new OrderCancelled();
        BeanUtils.copyProperties(this, orderCancelled);
        orderCancelled.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        deliveryorder.external.Cancellation cancellation = new deliveryorder.external.Cancellation();
        // mappings goes here
        cancellation.setOrderId(this.getId());
        cancellation.setStatus("Delivery Cancelled");

        OrderApplication.applicationContext.getBean(deliveryorder.external.CancellationService.class)
            .registerCancelledOrder(cancellation);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
