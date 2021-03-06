package util;

import models.Location;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skorti on 8/31/15.
 */
public class SystemDao {
    static LocalDate crc = new LocalDate().withYear(2013).withMonthOfYear(1).withDayOfMonth(6);
    private static LocalDate reviewCycleStartDate = crc;
    private static LocalDate reviewCycleEndDate = getReviewCycleEndDate();
    private static double defaultWeight = 1.0;
    //These values were obtained from qlauncher.properties
    private static int specialPurchaseOrderWassMultiplier = 3;
    private static int specialPurchaseOrderSizeMultipler = 2;
    private static double weight_5 = 0.3;



    public static LocalDate getCrc() {
        return crc;
    }
    private static List<Location> locations = new ArrayList<Location>();

    public static void setCrc(LocalDate dt){
        crc = dt;
        reviewCycleEndDate = getReviewCycleEndDate();
        reviewCycleStartDate = crc;
    }

    public static LocalDate getReviewCycleEndDate(){
        int weekday = DateTimeConstants.SATURDAY;
        return (crc.getDayOfWeek() < weekday)?crc.withDayOfWeek(weekday):crc.plusWeeks(1).withDayOfWeek(weekday);
    }

    public static double getDefaultWeight() {
        return defaultWeight;
    }

    public static void setDefaultWeight(double defaultWeight) {
        SystemDao.defaultWeight = defaultWeight;
    }

    public static boolean isEndOfRunCycle() {
        if(crc.getDayOfWeek() == DateTimeConstants.SATURDAY){
            return true;
        }
        else{
            return  false;
        }
    }

    public static boolean isBeginOfRunCycle() {
        if(crc.getDayOfWeek() == DateTimeConstants.SUNDAY){
            return true;
        }
        else{
            return  false;
        }
    }

    public static LocalDate updateCrc(){

        crc = crc.plusDays(1);
        return crc;
    }



    public static LocalDate getReviewCycleStartDate(){
        int weekday = DateTimeConstants.SUNDAY;
        LocalDate retVal;
        retVal =  (crc.getDayOfWeek() < weekday) ? crc.minusDays(crc.getDayOfWeek()) : crc.minusWeeks(1);
        return retVal;
    }

    private static LocalDate getPreviousCRCStartDate(){
        int weekday = DateTimeConstants.SUNDAY;
        LocalDate previousReviewCycle = crc.minusWeeks(1);
        LocalDate retVal = (previousReviewCycle.getDayOfWeek() < weekday)?previousReviewCycle.minusDays(previousReviewCycle.getDayOfWeek()):previousReviewCycle;
        return retVal;
    }


    public static int getSpecialPurchaseOrderWassMultiplier() {
        return specialPurchaseOrderWassMultiplier;
    }

    public static int getSpecialPurchaseOrderSizeMultipler() {
        return specialPurchaseOrderSizeMultipler;
    }

    public static double getWeight_5() {
        return weight_5;
    }
}
