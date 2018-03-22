package es.indios.markn.blescanner;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

import es.indios.markn.blescanner.models.Topology.Indication;
import es.indios.markn.blescanner.models.Topology.Route;

/**
 * Created by guille on 20/03/18.
 */

public class PathGuide {
    private ArrayList<Route> mTopology;
    private ArrayList<Indication> mIndications;
    private Beacon mLastBeacon;
    private String mActualDestination;

    public PathGuide(ArrayList<Route> topology, ArrayList<Indication> indications){
        mTopology = topology;
        mIndications = indications;
    }

    public void onBeaconsDetected(ArrayList<Beacon> beacons) {
        if(beacons.size() >0 ){
            if(beacons.get(0).getId2() != mLastBeacon.getId2()
                    || beacons.get(0).getId3() != mLastBeacon.getId3()){
                //Stuff here.
            }
        }
    }

    public void setDestination(String destination) {
        mActualDestination = destination;
    }
}
