package models;

/**
 * Created by skorti on 9/11/15.
 */
public class ProductInventory {
    double inStockPct;
    double epAvgInv;
    double epEopInv;

    public double getInStockPct() {
        return inStockPct;
    }

    public void setInStockPct(double inStockPct) {
        this.inStockPct = inStockPct;
    }

    public double getEpAvgInv() {
        return epAvgInv;
    }

    public void setEpAvgInv(double epAvgInv) {
        this.epAvgInv = epAvgInv;
    }

    public double getEpEopInv() {
        return epEopInv;
    }

    public void setEpEopInv(double epEopInv) {
        this.epEopInv = epEopInv;
    }
}
