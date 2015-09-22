package models;

/**
 * Created by skorti on 9/10/15.
 */
public class Sales {
    double rcAvgSales;
    double rcAvgSalesActual;
    double rcSales;
    double rcSalesActual;
    double epSales;
    double epSalesActual;
    double lostSales;
    double dailySalesScallingFactor;

    public double getRcAvgSales() {
        return rcAvgSales;
    }

    public void setRcAvgSales(double rcAvgSales) {
        this.rcAvgSales = rcAvgSales;
    }

    public double getRcAvgSalesActual() {
        return rcAvgSalesActual;
    }

    public void setRcAvgSalesActual(double rcAvgSalesActual) {
        this.rcAvgSalesActual = rcAvgSalesActual;
    }

    public double getEpSales() {
        return epSales;
    }

    public void setEpSales(double epSales) {
        this.epSales = epSales;
    }

    public double getRcSales() {
        return rcSales;
    }

    public void setRcSales(double rcSales) {
        this.rcSales = rcSales;
    }

    public double getRcSalesActual() {
        return rcSalesActual;
    }

    public void setRcSalesActual(double rcSalesActual) {
        this.rcSalesActual = rcSalesActual;
    }

    public double getLostSales() {
        return lostSales;
    }

    public void setLostSales(double lostSales) {
        this.lostSales = lostSales;
    }

    public double getEpSalesActual() {
        return epSalesActual;
    }

    public void setEpSalesActual(double epSalesActual) {
        this.epSalesActual = epSalesActual;
    }
}
