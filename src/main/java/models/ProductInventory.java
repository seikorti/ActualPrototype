package models;

/**
 * Created by skorti on 9/11/15.
 */
public class ProductInventory {
    double inStockPct;
    double epAvgInv;
    double epEopInv;
    int epInvOut;
    int epInvIn;

    int rcInvIn;
    int rcInvOut;
    double rcBopInv;

    int inventory;


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

    public double getRcBopInv() {
        return rcBopInv;
    }

    public void setRcBopInv(double rcBopInv) {
        this.rcBopInv = rcBopInv;
    }

    public int getEpInvOut() {
        return epInvOut;
    }

    public void setEpInvOut(int epInvOut) {
        this.epInvOut = epInvOut;
    }

    public int getEpInvIn() {
        return epInvIn;
    }

    public void setEpInvIn(int epInvIn) {
        this.epInvIn = epInvIn;
    }

    public int getRcInvIn() {
        return rcInvIn;
    }

    public void setRcInvIn(int rcInvIn) {
        this.rcInvIn = rcInvIn;
    }

    public int getRcInvOut() {
        return rcInvOut;
    }

    public void setRcInvOut(int rcInvOut) {
        this.rcInvOut = rcInvOut;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }
}
