package models;

import Processors.ProcessingHook;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Created by skorti on 8/17/15.
 */
public class Location {
    String id;

    Map<String, Product> productMap = new HashMap<String,  Product>();

    public Location(String id){
        this.id = id;
    }

    public void addProduct(Product product){
        product.addLocation(this);
        productMap.put(product.getId(), product);
    }

    public Product getProduct(String id){
        return productMap.get(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<Product> getProducts(){
        return productMap.values();
    }

    public void processSale(Product prod, Integer qty){
        if(productMap.get(prod.getId()) == null){
            productMap.put(prod.getId(), prod);
        }
        Product p = productMap.get(prod.getId());

        p.processSale(qty);
    }

    public void processWeeklyMetrics(){
        Collection<Product> products = productMap.values();
        for(Product p : products){
            p.processWeeklyMetrics();
        }
    }

    public void processDailyMetrics(){
        /*Collection<Product> products = productMap.values();
        for(Product p : products){
            p.processDailyMetrics();
        }*/
    }

}
