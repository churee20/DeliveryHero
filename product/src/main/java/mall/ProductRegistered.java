package mall;

public class ProductRegistered extends AbstractEvent {

    private Long id;

    public ProductRegistered(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
