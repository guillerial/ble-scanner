package es.indios.markn.blescanner;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

/**
 * Created by guille on 10/03/18.
 */

public interface MarknListener {


    void notifyBluetoothActivationRequired();

    void onBeaconsDetected(Collection<Beacon> beacons);
}
