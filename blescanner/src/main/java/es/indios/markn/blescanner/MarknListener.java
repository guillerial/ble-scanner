package es.indios.markn.blescanner;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

import es.indios.markn.blescanner.models.Topology.Indication;

/**
 * Created by guille on 10/03/18.
 */

public interface MarknListener {

    void notifyBluetoothActivationRequired();

    void onBeaconsDetected(ArrayList<Beacon> beacons);

    void onNewIndication(Indication indication);
}
