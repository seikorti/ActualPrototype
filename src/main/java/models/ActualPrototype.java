package models;

import Processors.*;
import org.joda.time.LocalDate;
import util.SystemDao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by skorti on 8/17/15.
 */


//Preprocessing
// 1: calculate average demand per department
// 2: calculate weekly average Sales (daily ave sales is not calculated)
// 3: truncate qr_metrics, not sure what the stored procedure DBTOOLS.TRUNCATE_TABLE does

//models.Actual Processing
//for each Non TOH P/L
// 1. Get aggregateDemand (qr_aggregate_demand)
// 2. Calculate/set OverAbundance (set to true only if high seasonality/lift false otherwise)
// 3. Calculate/set Derived Demand (only if we have high seasonality/lift)
// 4. Setup DOW indices
// 5. setupData() Evict objects we don't saved in the DB
// 6. In status not NEW, execute Preprocessing hooks "actualProductLocationProcessingWorkerPreProcessingHooks"
// 7. execute processing hooks (bean id: actualProcessingHooks)
//actualSyncInventoryProcessingHook
//actualResetDataAndCountersHook
//updateDailyMetricsProcessingHook
//updateAvgEOHOnHandInvHook
//actualRoundedDemandHook
//saveEpDataHook
//updateReviewCycleMetricsHook
//updateNewMetricsHook


//models.Actual Post Processing
//update and store EC data, and process scoring (find the best model)
//do retrospective sales update - qr_trans_pl_day
//calculate daily learning (accumulate daily demand and sales for the week) - QR_EP_DATA (holds demand info for a product location)
//do "M&S" outlier checks
//do event learning index setting

//update target inventory
//update scoring metrics
//update client metrics

public class ActualPrototype {
    List<ProcessingHook> processingHooks = new LinkedList<ProcessingHook>();
    Collection<Location> locations =  LocationDao.getInstance().getLocations();

    public ActualPrototype(){
        LocalDate crc = new LocalDate().withYear(2013).withMonthOfYear(1).withDayOfMonth(6);
        SystemDao.setCrc(crc);
        processingHooks.add(new PreProcessingHook());
        processingHooks.add(new OutlierCheckProcessingHook());
        processingHooks.add(new DemandLearningHook());
        processingHooks.add(new UpdateDailyMetricsProcessingHook());
        processingHooks.add(new UpdateWeeklyMetricsProcessingHook());
        processingHooks.add(new PostProcessingHook());
        processingHooks.add(new ResetDataAndCounterHook());
    }

    public static void main(String[] args){
        ActualPrototype actualPrototype = new ActualPrototype();
        //do model selection
        QModel model = actualPrototype.getModel();

        actualPrototype.setup();

        Random randGen = new Random();
        LocalDate strtDt = SystemDao.getCrc();
        LocalDate endDt = SystemDao.getCrc().plusWeeks(16);
        Location loc1;
        Product product = null;

        while(!strtDt.isAfter(endDt)){
            strtDt = strtDt.plusDays(1);
            loc1 = LocationDao.getInstance().getLocation("loc1");
            product = loc1.getProduct("prod1");

            //perform a sale at this location
            int numberSold = randGen.nextInt(100);
            loc1.processSale(product, numberSold);

            //now that the day is over run actual
            actualPrototype.runActual();

            SystemDao.updateCrc();
        }
        System.out.println(product.toString());
        actualPrototype.saveData(new StringBuffer().append(product.toString()));

    }

    protected void runActual(){

        for (Location loc : locations){
            for(ProcessingHook hook : processingHooks){
                hook.execute(loc);
            }
        }
    }

    public void setup(){
        LocalDate crc = SystemDao.getCrc();
        Product p1 = new Product("prod1");
        p1.setFirstReceiptDate(crc);
        p1.setStoreOpenDate(crc);
        p1.setNumberOfDaysToBecomeActive(1);
        p1.setDisableLearning(false);
        p1.setBopInv(1000);
        p1.setOnRange(true);

        Location loc1 = LocationDao.getInstance().getLocation("loc1");
        loc1.addProduct(p1);
        //SystemDao.addLocation(loc1);
    }


    public QModel getModel(){
        return new QModel();
    }



    public StringBuffer printSaleData(String title, Map<Integer, ? extends Number> data){
        Iterator<Integer> itr = data.keySet().iterator();
        Integer weekOfYr;
        StringBuffer bf = new StringBuffer();
        bf.append(title);
        while (itr.hasNext()){
            weekOfYr = itr.next();
            bf.append(weekOfYr).append( ":").append(data.get(weekOfYr)).append("; ");
        }
        bf.append("%");
        System.out.println(bf.toString());
        return bf;
    }

    public void saveData(StringBuffer bf){
        BufferedWriter output = null;
        try {
            File file = new File("./src/data/actualDemoData.txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write(bf.toString());
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
