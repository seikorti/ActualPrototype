package Processors;

import models.Location;


import java.util.*;

/**
 * Created by skorti on 8/21/15.
 */
public class LocationDao {

    private  Map<String, Location> locationMap = new HashMap<String, Location>();
    private static LocationDao instance;

    private LocationDao(){
        Location loc1 = new Location("loc1");
        locationMap.put(loc1.getId(), loc1);
    }

    public  Location getLocation(String id){
        return locationMap.get(id);
    }

    public  void addLocation(Location loc){
        locationMap.put(loc.getId(), loc);
    }

    public static LocationDao getInstance(){
        if(instance == null){
            instance = new LocationDao();
        }
        return  instance;
    }

}
