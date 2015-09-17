package Processors;

import models.Location;
import models.Product;

import java.util.Collection;

/**
 * Created by skorti on 9/16/15.
 */
public class UpdateDailyMetricsProcessingHook implements ProcessingHook {
    public void execute(Location loc) {
        Collection<Product> products = loc.getProducts();
        for(Product p : products){
            p.performDailyMetricsProcessing(loc.getId());
        }
    }
}
