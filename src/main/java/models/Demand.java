package models;

/**
 * Created by skorti on 9/10/15.
 */
public class Demand {
    double rcAvgDemand;
    double rcAvgDemandActual;
    double rcOldAvgDemand;
    double rcDemand;
    double rcDemandActual;
    double epDemand;

    public double getRcAvgDemand() {
        return rcAvgDemand;
    }

    public void setRcAvgDemand(double rcAvgDemand) {
        this.rcAvgDemand = rcAvgDemand;
    }

    public double getRcAvgDemandActual() {
        return rcAvgDemandActual;
    }

    public void setRcAvgDemandActual(double rcAvgDemandActual) {
        this.rcAvgDemandActual = rcAvgDemandActual;
    }

    public double getRcOldAvgDemand() {
        return rcOldAvgDemand;
    }

    public void setRcOldAvgDemand(double rcOldAvgDemand) {
        this.rcOldAvgDemand = rcOldAvgDemand;
    }

    public double getRcDemand() {
        return rcDemand;
    }

    public void setRcDemand(double rcDemand) {
        this.rcDemand = rcDemand;
    }

    public double getRcDemandActual() {
        return rcDemandActual;
    }

    public void setRcDemandActual(double rcDemandActual) {
        this.rcDemandActual = rcDemandActual;
    }

    public double getEpDemand() {
        return epDemand;
    }

    public void setEpDemand(double epDemand) {
        this.epDemand = epDemand;
    }


}
