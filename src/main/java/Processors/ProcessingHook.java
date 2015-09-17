package Processors;

import models.Location;
import models.Product;

/**
 * Created by skorti on 9/16/15.
 */
public interface ProcessingHook {
    public void execute(Location loc);
}
