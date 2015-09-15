package models;

import java.util.List;

/**
 * Created by skorti on 8/31/15.
 */
public class Event {
    double eventPercentage;
    double demandUpliftPct;

    public Event(int eventPercentage){
        setEventPercentage(eventPercentage);
    }

    public Event(double demandUpliftPct){
        this.demandUpliftPct = demandUpliftPct;
        eventPercentage = (demandUpliftPct * 100) + 100;
    }

    public double getEventPercentage() {
        return eventPercentage;
    }

    public void setEventPercentage(double eventPercentage) {
        if( eventPercentage > -100 || eventPercentage <= 1000) {
            this.eventPercentage = eventPercentage;
        }
        else{
            this.eventPercentage = 1.0;
        }
        demandUpliftPct = (this.eventPercentage/100.0) + 1;

    }

    public double getDemandUpliftPct() {
        return demandUpliftPct;
    }

    public Event add(Event e){
        double demandLift = ((demandUpliftPct - 1) + (e.getDemandUpliftPct() - 1)) + 1;
        return new Event(demandLift);
    }

    public static double calculateDemandUplift(List<Event> events){
        if(events.isEmpty()){
            return 1.0;
        }

        double retVal = 0.0;
        for(Event e : events ){
            retVal += e.getDemandUpliftPct() - 1;
        }
        return retVal + 1;
    }
}
