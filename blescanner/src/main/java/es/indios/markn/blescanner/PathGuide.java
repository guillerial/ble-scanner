package es.indios.markn.blescanner;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.HashMap;

import es.indios.markn.blescanner.models.Topology.Indication;
import es.indios.markn.blescanner.models.Topology.Route;

/**
 * Created by guille on 20/03/18.
 */

public class PathGuide {
    private HashMap<String, Route> mTopology;
    private HashMap<String, Indication> mIndications;
    private Beacon mLastBeacon;
    private String mActualDestination;
    private PathGuideListener mListener;

    public PathGuide(ArrayList<Route> topology, ArrayList<Indication> indications, PathGuideListener listener){
        mTopology = new HashMap<>();
        for (Route route : topology){
            mTopology.put(route.getRoute(), route);
        }

        mIndications = new HashMap<>();
        for (Indication indication : indications) {
            mIndications.put(indication.getRoute(), indication);
        }

        mListener = listener;
    }

    public void onBeaconsDetected(ArrayList<Beacon> beacons) {
        if(!beacons.isEmpty() && mActualDestination != null){
            if(mLastBeacon == null || beacons.get(0).getId2().toInt() != mLastBeacon.getId2().toInt()
                    || beacons.get(0).getId3().toInt() != mLastBeacon.getId3().toInt()){
                //Stuff here.
                mLastBeacon = beacons.get(0);
                String actualBeacon = beacons.get(0).getId2().toString().concat(beacons.get(0).getId3().toString());
                Route actualRoute = mTopology.get(actualBeacon+"-"+mActualDestination);
                mListener.onNewIndication(mIndications.get(actualBeacon+"-"+actualRoute.getNext()));
            }
        }
    }

    public void setDestination(String destination) {
        mActualDestination = destination;
    }


    public interface PathGuideListener{

        void onNewIndication(Indication indication);
    }
}
