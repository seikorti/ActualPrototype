package models;

import org.joda.time.*;
import util.SystemDao;

import java.util.*;

/**
 * Created by skorti on 8/17/15.
 */
public class Product {
    String id;
    List<Location> locations = new ArrayList<Location>();

    Map<Years,Map<LocalDate, List<Event>>> eventsMap = new TreeMap<Years, Map<LocalDate, List<Event>>>();
    Map<Years,Map<LocalDate, Boolean>> tempOnHoldMap = new TreeMap<Years, Map<LocalDate, Boolean>>();
    Map<Years,Map<LocalDate, Sales>> salesMap = new TreeMap<Years, Map<LocalDate, Sales>>();
    Map<Years,Map<LocalDate, Demand>> demandMap = new TreeMap<Years, Map<LocalDate, Demand>>();
    Map<Years,Map<LocalDate, ProductInventory>> inventoryMap = new TreeMap<Years, Map<LocalDate, ProductInventory>>();


    int inventory;
    int invIn;
    LocalDate firstSalesDate;
    LocalDate storeOpenDate;
    LocalDate firstReceiptDate;
    STATUS_CD statusCd;
    boolean isBasicItem;
    int innerPackQty;
    int numberOfDaysToBecomeActive;
    boolean syncInventory;
    int invOut;

    //RC Accumulators
    double rcAvgSales;
    double epSales;
    double rcSalesActual;

    double rcDemand;
    double rcDemandActual;
    double rcAvgDemand;
    double rcAvgDemandActual;
    double rcOldAvgDemand;


    //EP Accumulators
    double epAvgInv;
    int epEopInv;
    int bopInv;

    double rcWass2;


    Boolean eventSeasonalIndicator;
    private int daysSinceWalk;
    private int demoStock;
    private double rcMaxSales;
    private int weekSinceMaxSales;
    private int daysSinceSale;
    private int learningWeekCounter;
    private boolean disableLearning;
    private boolean onRange;
    private boolean hasSale;
    //double rcLostUnits;
    private boolean hasBeenOffRange;
    private boolean learningMetricsInitialized;
    private boolean highSeasonality;
    private double rcAvgSalesActual;
    private double epSalesActual;
    private double epDemand;
    private double rcSales;


    public enum STATUS_CD {
        NEW, LEARNING, ACTIVE, INACTIVE
    }

    public Product(String id){
        this.id = id;
        statusCd = STATUS_CD.NEW;
        isBasicItem = true;
        learningMetricsInitialized = false;
        highSeasonality = false;
        eventSeasonalIndicator = false;
        epSales = 75; //assumes this is the average for similar product across departments in STG
        innerPackQty = 10;
        syncInventory = false;
        onRange = true;
        bopInv = 1000;
        demoStock = 20;
        rcSales = 0;
    }


    public void performDailyMetricsProcessing(String locationId) {
        System.out.println("Starting Daily Metrics Processing for product/location: " + locationId);
        processDailyMetrics();
    }

    //Hook: WeeklyLearningSummationHook.java
    public void performWeeklyMetricsProcessing(String locationId) {
        System.out.println("Starting the Weekly Metrics Processing for product/location: " + locationId);
        if(SystemDao.getCrc().getDayOfWeek() == DateTimeConstants.SUNDAY) {
            processWeeklyMetrics();

        }
    }

    public void peformDemandLearning(String locationId) {
        //System.out.println("Starting the Demand Learning for product/location: " + locationId);
        initLearningMetrics();
        performDailyLearning();
    }

    //Hook: MSSalesOutlierCheckHook.java
    public void performOutlierProcessing(String locId) {
        // only apply constraint if we are not in a seasonal period
        System.out.println("Starting the Outlier/Unreported Sales Calculation for product/location: " + locId);

        int specialPuchaseOrderWassMult = SystemDao.getSpecialPurchaseOrderWassMultiplier();
        int specialPuchaseOrderSizeMult = SystemDao.getSpecialPurchaseOrderSizeMultipler();
        double rcWass2 = Math.pow(rcAvgSales, 2);
        Double maxValue = Math.max(rcAvgSales + specialPuchaseOrderSizeMult * Math.sqrt(Math.max(0, rcWass2)), specialPuchaseOrderWassMult * innerPackQty);
        if(epSalesActual > maxValue && eventSeasonalIndicator == false){
            epSales = maxValue;
        }
        else{
            epSales = epSalesActual;
        }

    }

    public void performPreprocessing(String locationId) {
        System.out.println("Starting Pre-Processing for product/location: " + locationId);

    }

    public void performPostProcessing(String locationId) {
        System.out.println("Starting Post-Processing for product/location: " + locationId);

    }

    public void performResetAndCounter(String locationId) {
       System.out.println("Starting Reset And Counter Processing for product/location: " + locationId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public boolean getOverAbundance() {
        //set to true only if high seasonality/lift false otherwise
        if(highSeasonality) {
            return true;
        }
        else{
            return false;
        }
    }

    public int getDemoStock() {
        return demoStock;
    }

    public void setDemoStock(int demoStock) {
        this.demoStock = demoStock;
    }

    public int getNumberOfDaysToBecomeActive() {
        return numberOfDaysToBecomeActive;
    }

    public void setNumberOfDaysToBecomeActive(int numberOfDaysToBecomeActive) {
        this.numberOfDaysToBecomeActive = numberOfDaysToBecomeActive;
    }

    public LocalDate getStoreOpenDate() {
        return storeOpenDate;
    }

    public void setStoreOpenDate(LocalDate storeOpenDate) {
        this.storeOpenDate = storeOpenDate;
    }

    protected void addLocation(Location location){
        locations.add(location);
    }

    public void setBopInv(int bopInv) {
        if(SystemDao.getCrc().getDayOfWeek() == DateTimeConstants.SUNDAY) {
            this.bopInv = bopInv;
        }
    }

    public void setInvIn(int invIn) {
        this.invIn = invIn;
    }


    protected void processSale(Integer sale){
        hasSale = true;
        epSalesActual += sale;
        inventory -= sale;
        invOut += sale;
        invIn -= sale;
    }

    public Boolean getOnRange() {
        return onRange;
    }

    public void setOnRange(Boolean onRange) {
        if(!onRange){
            hasBeenOffRange = false;
        }
        this.onRange = onRange;
    }

    public LocalDate getFirstSalesDate() {
        return firstSalesDate;
    }

    public void setFirstSalesDate(LocalDate firstSalesDate) {
        if(firstSalesDate == null && epSalesActual > 0){
            this.firstSalesDate = SystemDao.getCrc();
        }
        else {
            this.firstSalesDate = firstSalesDate;
        }
    }

    public LocalDate getFirstReceiptDate() {
        return firstReceiptDate;
    }

    public void setFirstReceiptDate(LocalDate firstReceiptDate) {
        if(this.firstReceiptDate == null && epEopInv > 0 || invIn > 0){
            this.firstReceiptDate = SystemDao.getCrc();
        }
        else if(this.firstReceiptDate == null && firstSalesDate != null){
            this.firstReceiptDate = firstSalesDate;
        }
        this.firstReceiptDate = firstReceiptDate;
    }

    public STATUS_CD getStatusCd() {
        return statusCd;
    }

    public void updateStatusCode() {
        switch (this.statusCd) {
            case NEW:
                if (this.statusCd == STATUS_CD.NEW && !firstReceiptDate.isAfter(SystemDao.getCrc()) && !storeOpenDate.isAfter(SystemDao.getCrc())) {
                    this.statusCd = STATUS_CD.LEARNING;
                }
                break;
            case LEARNING:
                //provided there have been no non-demand events or events that lasted most of the week and the PL has been on
                // range the entire time, the PL is moved to status_cd = ACTIVE after the 3rd full week.
                if (!hasBeenOffRange && learningWeekCounter > 3) {
                    statusCd = STATUS_CD.ACTIVE;
                }
                break;
            case INACTIVE:
                //item only goes inactive during integration, when PL drops from the file
        }
    }

    public boolean isBasicItem() {
        return isBasicItem;
    }

    public void setIsBasicItem(boolean isBasicItem) {
        this.isBasicItem = isBasicItem;
    }

    public Integer getInnerPackQty() {
        return innerPackQty;
    }

    public void setInnerPackQty(Integer innerPackQty) {
        this.innerPackQty = innerPackQty;
    }

    private Sales getSales(LocalDate dt){
        return salesMap.get(Years.years(dt.getYear())) == null ? null : salesMap.get(Years.years(dt.getYear())).get(dt);
    }

    private Demand getDemand(LocalDate dt){
        return demandMap.get(Years.years(dt.getYear())) == null ? null : demandMap.get(Years.years(dt.getYear())).get(dt);
    }

    public void setDisableLearning(Boolean disableLearning) {
        //Learning can only be turned on on a SUNDAY
        if(disableLearning) {
            if (SystemDao.getCrc().plusDays(1).getDayOfWeek() == DateTimeConstants.SUNDAY)
                this.disableLearning = disableLearning;
        }
        else{
            this.disableLearning = disableLearning;
        }
    }

    private double getDemandUplift(LocalDate dt){
        Map<LocalDate, List<Event>> currEventMap = eventsMap.get(Years.years(dt.getYear()));
        if(currEventMap == null){
            return 1.0;
        }
        List<Event> events = currEventMap.get(dt);
        return Event.calculateDemandUplift(events);
    }

    private void initLearningMetrics(){
        if(learningMetricsInitialized){
            return;
        }
        double defaultWeight = SystemDao.getDefaultWeight();
        double lastWeekLift = getDemandUplift(SystemDao.getReviewCycleStartDate());
        Sales beginOfRunCycleSales = getSales(SystemDao.getReviewCycleStartDate());
        double beginOfRunCycleRcAvgSales = 0;
        if(beginOfRunCycleSales != null){
            beginOfRunCycleRcAvgSales = beginOfRunCycleSales.getRcAvgSales();
        }

        rcAvgSales = getWeightedWeight1(learningWeekCounter, defaultWeight) * epSales/lastWeekLift +
                (1 - getWeightedWeight1(learningWeekCounter, defaultWeight))*beginOfRunCycleRcAvgSales;

        //Tim's documentation states:
        //Its important sales metrics are initialized to AVG_WEEKLY_SALES based on the initialization logic.
        //This estimates initial values based on different combinations of the PLs product and location
        //hierarchies dependent on the specific situation for that PL
        rcAvgSalesActual = rcAvgSales;
        rcAvgDemand = rcAvgSales;
        rcOldAvgDemand = rcAvgSales;
        rcAvgDemandActual = rcAvgSales;
        epAvgInv = rcAvgSales;

        rcWass2 = Math.pow(rcAvgSales, 2);
        learningMetricsInitialized = true;
    }

    private void performDailyLearning() {
        updateStatusCode();
        if (statusCd != STATUS_CD.LEARNING || disableLearning == true) {
            return;
        }
        if (onRange && (bopInv > 0 || hasSale == true || invIn > 0)) {
            initLearningMetrics();
            //Daily learning only applies to RC_AVG_DEMAND and begins immediately when the PL comes on range
            LocalDate crc = SystemDao.getCrc();
            Years currYr = Years.years(crc.getYear());
            double lastWeekLift = getDemandUplift(SystemDao.getReviewCycleStartDate());
            double defaultWeight = SystemDao.getDefaultWeight();
            Demand beginOfRunCycleDemand = getDemand(SystemDao.getReviewCycleStartDate());
            double beginOfRunCycleRcAvgDemand = 0;
            if(beginOfRunCycleDemand != null){
                beginOfRunCycleRcAvgDemand = beginOfRunCycleDemand.getRcAvgDemand();
            }

            if (onRange && !disableLearning) {

                rcAvgDemand = getWeightedWeight1(learningWeekCounter, defaultWeight) * rcDemand / lastWeekLift +
                        (1 - getWeightedWeight1(learningWeekCounter, defaultWeight)) * beginOfRunCycleRcAvgDemand;

                Map<LocalDate, Demand> currDemandMap = demandMap.get(currYr);
                if (currDemandMap == null) {
                    currDemandMap = new TreeMap<LocalDate, Demand>();
                }
                Demand d = currDemandMap.get(crc);
                if (d == null) {
                    d = new Demand();
                }
                d.setRcAvgDemand(rcAvgDemand);
                currDemandMap.put(crc, d);
                demandMap.put(currYr, currDemandMap);
            }
        }
    }

    private double getEpDemand() {
        //assumes this product sells in eaches
        double demand;
        double randNum = new Random().nextDouble(); // a random number between 0 and 1;
        double lostSales = getLostSales();
        double minDemand = epSalesActual + Math.floor(lostSales);
        if (randNum < (lostSales - Math.floor(lostSales))) {
            demand = minDemand + 1;
        } else {
            demand = minDemand;
        }
        return demand;
    }

    private void resetEpAccumulators() {
        epSales = 0.0;
        epSalesActual = 0;
        epDemand = 0.0;
    }


    private double getLostSales(){
        return Math.max(0, rcAvgDemand/7 - epSalesActual);
    }

    /** Outlier filtering has been done prior to processing daily metrics **/
    private void processDailyMetrics(){

        LocalDate crc = SystemDao.getCrc();

        //lostSales = max(0, average sales - actual sales)
        double lostSales = getLostSales();

        rcSalesActual = rcSalesActual + epSalesActual;
        //rcSales is outlier filtered sales and has been calculated in outlier processing
        rcSales = rcSales + epSales;

        Years yr = Years.years(crc.getYear());
        Map<LocalDate, Sales> currSalesMap = salesMap.get(yr);
        if(currSalesMap == null){
            currSalesMap = new TreeMap<LocalDate, Sales>();
        }
        Sales s = currSalesMap.get(crc);
        if(s == null) {
            s = new Sales();
        }
        s.setEpSalesActual(epSalesActual);
        s.setEpSales(epSales);
        s.setRcSales(rcSales);
        s.setRcSalesActual(rcSalesActual);
        s.setLostSales(lostSales);

        currSalesMap.put(crc, s);
        salesMap.put(yr, currSalesMap);

        /***  Daily Demand Calculations *****/
        //daily demand
        epDemand = getEpDemand();

        //demand = sales + lostsales
        //outlier filtered sales used for rcDemand
        rcDemand = rcDemand + (rcSales + lostSales);

        rcDemandActual = rcDemandActual + (epSalesActual + lostSales);


        Map<LocalDate, Demand> currDemandMap = demandMap.get(yr);
        if(currDemandMap == null){
            currDemandMap = new TreeMap<LocalDate, Demand>();
        }
        Demand d = currDemandMap.get(crc);
        if(d == null) {
            d = new Demand();
        }
        d.setEpDemand(epDemand);
        d.setRcDemand(rcDemand);
        d.setRcDemandActual(rcDemandActual);
        d.setRcAvgDemand(rcAvgDemand);
        d.setRcAvgDemandActual(rcAvgDemandActual);

        currDemandMap.put(crc, d);
        demandMap.put(yr, currDemandMap);

        /****** Daily Inventory Calculations *******/

        epAvgInv =  Math.max(0, (1 - getWeight1(5) * epAvgInv) + (getWeight1(5)*epEopInv));
        if(epSales > rcMaxSales){
            rcMaxSales = epSales;
            weekSinceMaxSales = 0;
        }

        if(epEopInv > demoStock){
            daysSinceWalk = daysSinceWalk + 1;
        }
        else{
            daysSinceWalk = 0;
        }

        if(epSalesActual == 0){
            daysSinceSale = daysSinceSale + 1;
        }
        else{
            daysSinceSale = 0;
        }

        Map<LocalDate, ProductInventory> currInventoryMap = inventoryMap.get(yr);
        if(currInventoryMap == null){
            currInventoryMap = new TreeMap<LocalDate, ProductInventory>();
        }
        ProductInventory inv = currInventoryMap.get(crc);
        if(inv == null) {
            inv = new ProductInventory();
        }
        inv.setEpAvgInv(epAvgInv);
        inv.setEpEopInv(epEopInv);
        inv.setBopInv(bopInv);

        currInventoryMap.put(crc, inv);
        inventoryMap.put(yr, currInventoryMap);

        if(statusCd == STATUS_CD.LEARNING) {
            performDailyLearning();
        }

        resetEpAccumulators();
        updateInventory();
    }

    private void updateInventory(){
        int minInv = 10;
        if((invIn - invOut) <= minInv){
            setBopInv(Vendor.getInventory(invOut));
            invOut = 0;
        }
        else{
            setBopInv(invIn);
        }
        setEpEopInv();
    }

    private void processWeeklyMetrics() {
        //On the end of the review cycle (Sat night) the rcAvgDemand does not undergo weekly learning
        //For this exercise we do not have time granulity  so weekly processing is done Sunday for the prior week

        LocalDate crc = SystemDao.getCrc();
        Sales salesData = getSales(crc);
        Sales beginOfPeriodSalesData = getSales(SystemDao.getReviewCycleStartDate());
        double beginOfPeriodRcAvgSales = 0;
        double beginOfPeriodRcActualSales = 0;
        if(beginOfPeriodSalesData != null){
            beginOfPeriodRcAvgSales = beginOfPeriodSalesData.getRcAvgSales();
            beginOfPeriodRcActualSales = beginOfPeriodSalesData.getRcSalesActual();
        }

        Demand demandData = getDemand(crc);
        Demand beginOfPeriodDemandData = getDemand(SystemDao.getReviewCycleStartDate());
        double beginOfPeriodDemandRcAvgDemand = 0;
        double beginOfPeriodDemandRcActual = 0;
        if(beginOfPeriodDemandData != null){
            beginOfPeriodDemandRcAvgDemand = beginOfPeriodDemandData.getRcAvgDemand();
            beginOfPeriodDemandRcActual = beginOfPeriodDemandData.getRcDemandActual();
        }
        double defaultWeight = SystemDao.getDefaultWeight();

        double lastWeekLift = getDemandUplift(SystemDao.getReviewCycleStartDate());

        rcAvgSalesActual = getWeight1(learningWeekCounter) * salesData.getRcSalesActual() + (1 - getWeight1(learningWeekCounter)) * beginOfPeriodRcActualSales;
        rcAvgSales = getWeight1(learningWeekCounter) * salesData.getRcSales() / lastWeekLift + (1 - getWeight1(learningWeekCounter)) * beginOfPeriodRcAvgSales;
        rcAvgDemand = getWeight1(learningWeekCounter) * demandData.getRcDemand() / lastWeekLift + (1 - getWeight1(learningWeekCounter)) * beginOfPeriodDemandRcAvgDemand;
        rcAvgDemandActual = getWeightedWeight1(learningWeekCounter, defaultWeight) * demandData.getRcDemandActual() / lastWeekLift +
                (1 - getWeightedWeight1(learningWeekCounter, defaultWeight)) * beginOfPeriodDemandRcActual;
        //error checking
        if (rcAvgDemand == 0 && statusCd != STATUS_CD.INACTIVE) {
            System.out.println("Error: 0 demand when product status is not inactive");
        }
        if (rcAvgDemand >= 4 * rcAvgSales) {
            rcAvgSales = 4 * rcAvgSalesActual;
            System.out.println("Error: RC Actual Sales greater then 4 times RC Average Sales");
        }
        if (rcAvgDemand >= 3 * rcAvgSalesActual) {
            rcAvgSales = 3 * rcAvgSalesActual;
            System.out.println("Error: RC Actual Sales greater then 3 times RC Actual Average Sales");
        }

        storeWeeklyMetrics(crc);
        this.learningWeekCounter++;
        //this is the end of the review cycle reset hasBeenOffRange
        if (hasBeenOffRange) {
            hasBeenOffRange = false;
        }
        resetWeeklyMatrics();
    }

    private void storeWeeklyMetrics(LocalDate crc){
        //Store weekly demand metrics
        Years yr = Years.years(crc.getYear());
        Map<LocalDate, Demand> currDemandMap = demandMap.get(yr);
        if (currDemandMap == null) {
            currDemandMap = new TreeMap<LocalDate, Demand>();
        }
        Demand d = currDemandMap.get(crc);
        if (d == null) {
            d = new Demand();
        }
        d.setRcAvgDemand(rcAvgDemand);
        d.setRcAvgDemandActual(rcAvgDemandActual);
        currDemandMap.put(crc, d);
        demandMap.put(yr, currDemandMap);

        //Store weekly sales metrics
        Map<LocalDate, Sales> currSalesMap = salesMap.get(yr);
        if (currSalesMap == null) {
            currSalesMap = new TreeMap<LocalDate, Sales>();
        }
        Sales s = currSalesMap.get(crc);
        if(s == null){
            s = new Sales();
        }
        s.setRcAvgSalesActual(rcAvgSalesActual);
        s.setRcAvgSales(rcAvgSales);
        currSalesMap.put(crc, s);
        salesMap.put(yr, currSalesMap);
    }

    private void resetWeeklyMatrics(){
        rcSales = 0;
        rcSalesActual = 0;
        rcDemand = 0;
        rcDemandActual = 0;
    }

    public double getWeightedWeight1(long age, double defaultWeight) {
        if (age <= 0) {
            return defaultWeight;
        } else {
            return Math.max(getWeight1(age), defaultWeight * (1.0d / age));
        }
    }

    private Double getWeight1(long age){
        //Weight that will be used when status_cd = ACTIVE
        if(age <= 0){
            return SystemDao.getDefaultWeight();
        }
        return  Math.max(0.2, 1.0 / age);
    }


    public Boolean isTempOnHold(LocalDate dt){
        Years yr = Years.years(dt.getYear());
        Map<LocalDate, Boolean> map = tempOnHoldMap.get(yr);
        return map.get(dt);
    }

    public void setTempOnHold(LocalDate dt, Boolean val){
        Years yr = Years.years(dt.getYear());
        Map<LocalDate, Boolean> map = tempOnHoldMap.get(yr);
        if(map == null){
            map = new TreeMap<LocalDate, Boolean>();
        }
        map.put(dt, val);
        tempOnHoldMap.put(yr, map);
    }

    public Integer getEpEopInv() {
        return epEopInv;
    }

    public void setEpEopInv() {
        if(syncInventory){
            this.epEopInv = Vendor.getInventory(bopInv);
        }
        else if(invIn != 0 || invOut != 0){
            epEopInv = epEopInv + invIn - invOut;
        }

    }

    public void addEvent(LocalDate dt, Event e){
        Years yr = Years.years(dt.getYear());
        Map<LocalDate, List<Event>> currEventMap = eventsMap.get(yr);
        if(currEventMap == null){
            currEventMap = new TreeMap<LocalDate, List<Event>>();
        }

        List<Event> eventList = currEventMap.get(dt);
        if(eventList == null){
            eventList = new ArrayList<Event>();
        }
        eventList.add(e);
        currEventMap.put(dt, eventList);
        eventsMap.put(yr, currEventMap);
    }

    public String toString(){
        return new StringBuffer().append(salesToString()).append(demandToString()).toString();
    }

    private String salesToString(){
        StringBuffer epSalesActualBf = new StringBuffer();
        epSalesActualBf.append("epSalesActual|");
        StringBuffer epSalesBf = new StringBuffer();
        epSalesBf.append("epSales|");

        StringBuffer rcSalesBf = new StringBuffer();
        rcSalesBf.append("rcSales|");
        StringBuffer rcAvgSalesBf = new StringBuffer();
        rcAvgSalesBf.append("rcAvgSales|");

        StringBuffer rcSalesActualBf = new StringBuffer();
        rcSalesActualBf.append("rcSalesActual|");
        StringBuffer rcAvgSalesActualBf = new StringBuffer();
        rcAvgSalesActualBf.append("rcAvgSalesActual|");

        Iterator<Years> yrItr = salesMap.keySet().iterator();
        Map<LocalDate, Sales> currSalesMap;
        LocalDate currDate;
        Years currYr;
        Sales currSale;
        //Populate Sale buffers
        while(yrItr.hasNext()){
            currYr = yrItr.next();
            currSalesMap = salesMap.get(currYr);
            Iterator<LocalDate> itr = currSalesMap.keySet().iterator();
            while(itr.hasNext()){
                currDate = itr.next();
                currSale = currSalesMap.get(currDate);
                //epSaleActual
                epSalesActualBf.append(currDate);
                epSalesActualBf.append(":");
                epSalesActualBf.append(currSale.getEpSalesActual());
                epSalesActualBf.append("|");
                //epSale
                epSalesBf.append(currDate);
                epSalesBf.append(":");
                epSalesBf.append(currSale.getEpSales());
                epSalesBf.append("|");
                //rc sales actual
                rcSalesActualBf.append(currDate);
                rcSalesActualBf.append(":");
                rcSalesActualBf.append(currSale.getRcSalesActual());
                rcSalesActualBf.append("|");
                //rc average sales actual
                rcAvgSalesActualBf.append(currDate);
                rcAvgSalesActualBf.append(":");
                rcAvgSalesActualBf.append(currSale.getRcAvgSalesActual());
                rcAvgSalesActualBf.append("|");
                //rc sales
                rcSalesBf.append(currDate);
                rcSalesBf.append(":");
                rcSalesBf.append(currSale.getRcSales());
                rcSalesBf.append("|");
                //rc sales average
                rcAvgSalesBf.append(currDate);
                rcAvgSalesBf.append(":");
                rcAvgSalesBf.append(currSale.getRcAvgSales());
                rcAvgSalesBf.append("|");

            }
        }
        epSalesActualBf.append(";\n");
        epSalesBf.append(";\n");
        rcSalesActualBf.append(";\n");
        rcAvgSalesActualBf.append(";\n");
        rcSalesBf.append(";\n");
        rcAvgSalesBf.append(";\n");

        StringBuffer retBf = new StringBuffer();
        retBf.append(epSalesActualBf.toString());
        retBf.append(epSalesBf.toString());
        retBf.append(rcSalesActualBf.toString());
        retBf.append(rcAvgSalesActualBf.toString());
        retBf.append(rcSalesBf.toString());
        retBf.append(rcAvgSalesBf.toString());
        retBf.append("\n");

        return retBf.toString();
    }

    private String demandToString(){
        Iterator<Years> yrItr = demandMap.keySet().iterator();
        Map<LocalDate, Demand> currDemandMap;
        Years currYr;
        LocalDate currDate;
        Demand currDemand;

        StringBuffer epDemandBf = new StringBuffer();
        epDemandBf.append("epDemand|");
        StringBuffer rcActualDemandBf = new StringBuffer();
        rcActualDemandBf.append("rcActualDemand|");
        StringBuffer rcDemandBf = new StringBuffer();
        rcDemandBf.append("rcDemand|");
        StringBuffer rcAvgDemandBf = new StringBuffer();
        rcAvgDemandBf.append("rcAvgDemand|");
        StringBuffer rcAvgDemandActualBf = new StringBuffer();
        rcAvgDemandActualBf.append("rcAvgDemandActual|");


        while(yrItr.hasNext()){
            currYr = yrItr.next();
            currDemandMap = demandMap.get(currYr);
            Iterator<LocalDate> itr = currDemandMap.keySet().iterator();
            while(itr.hasNext()){
                currDate = itr.next();
                currDemand = currDemandMap.get(currDate);
                //ep demand
                epDemandBf.append(currDate);
                epDemandBf.append(":");
                epDemandBf.append(currDemand.getEpDemand());
                epDemandBf.append("|");
                //actual demand
                rcActualDemandBf.append(currDate);
                rcActualDemandBf.append(":");
                rcActualDemandBf.append(currDemand.getRcDemandActual());
                rcActualDemandBf.append("|");
                //rc demand
                rcDemandBf.append(currDate);
                rcDemandBf.append(":");
                rcDemandBf.append(currDemand.getRcDemand());
                rcDemandBf.append("|");
                //rc average demand
                rcAvgDemandBf.append(currDate);
                rcAvgDemandBf.append(":");
                rcAvgDemandBf.append(currDemand.getRcAvgDemand());
                rcAvgDemandBf.append("|");
                //rc average demand actual
                rcAvgDemandActualBf.append(currDate);
                rcAvgDemandActualBf.append(":");
                rcAvgDemandActualBf.append(currDemand.getRcAvgDemandActual());
                rcAvgDemandActualBf.append("|");
            }
        }
        rcActualDemandBf.append(";\n");
        epDemandBf.append(";\n");
        rcDemandBf.append(";\n");
        rcAvgDemandBf.append(";\n");
        rcAvgDemandActualBf.append(";\n");

        StringBuffer retBf = new StringBuffer();
        retBf.append(epDemandBf.toString());
        retBf.append(rcActualDemandBf.toString());
        retBf.append(rcDemandBf.toString());
        retBf.append(rcAvgDemandBf.toString());
        retBf.append(rcAvgDemandActualBf.toString());
        retBf.append("\n");

        return retBf.toString();
    }
/*
    private String inventoryToString(){
        StringBuffer bopBf = new StringBuffer();
        bopBf.append("bopInv|");
        StringBuffer invInBf = new StringBuffer();
        invInBf.append("invIn|");
        StringBuffer epAvgInvInBf = new StringBuffer();
        epAvgInvInBf.append("epAvgInvIn|");
        StringBuffer invOutBf = new StringBuffer();
        invOutBf.append("invOut|");
        StringBuffer epEopInvBf = new StringBuffer();
        epEopInvBf.append("epEopInv|");

        Iterator<Years> yrItr = salesMap.keySet().iterator();
        Map<LocalDate, Sales> currSalesMap;
        LocalDate currDate;
        Years currYr;
        Sales currSale;
        //Populate Sale buffers
        while(yrItr.hasNext()){
            currYr = yrItr.next();
            currSalesMap = salesMap.get(currYr);
            Iterator<LocalDate> itr = currSalesMap.keySet().iterator();
            while(itr.hasNext()){
                currDate = itr.next();
                currSale = currSalesMap.get(currDate);
                //rc sales actual
                rcSalesActualBf.append(currDate);
                rcSalesActualBf.append(":");
                rcSalesActualBf.append(currSale.getRcSalesActual());
                rcSalesActualBf.append("|");
                //rc average sales actual
                rcAvgSalesActualBf.append(currDate);
                rcAvgSalesActualBf.append(":");
                rcAvgSalesActualBf.append(currSale.getRcAvgSalesActual());
                rcAvgSalesActualBf.append("|");
                //rc sales
                rcSalesBf.append(currDate);
                rcSalesBf.append(":");
                rcSalesBf.append(currSale.getRcSales());
                rcSalesBf.append("|");
                //rc sales average
                rcAvgSalesBf.append(currDate);
                rcAvgSalesBf.append(":");
                rcAvgSalesBf.append(currSale.getRcAvgSales());
                rcAvgSalesBf.append("|");

            }
        }
        rcSalesActualBf.append(";\n");
        rcAvgSalesActualBf.append(";\n");
        rcSalesBf.append(";\n");
        rcAvgSalesBf.append(";\n");

        StringBuffer retBf = new StringBuffer();
        retBf.append(rcSalesActualBf.toString());
        retBf.append(rcAvgSalesActualBf.toString());
        retBf.append(rcSalesBf.toString());
        retBf.append(rcAvgSalesBf.toString());
        retBf.append("\n");

        return retBf.toString();
    }
    */
}
