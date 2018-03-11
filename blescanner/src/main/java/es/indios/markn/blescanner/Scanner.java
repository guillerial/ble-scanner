package es.indios.markn.blescanner;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;
import java.util.Objects;

import timber.log.Timber;

public abstract class Scanner implements BeaconConsumer{

    private Context mContext;
    private MarknListener mListener;
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconManager mBeaconManager;

    private static final Region MARKN_REGION = new Region("com.indios.markn.ble-scanner", null, null, null);

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
                switch (state){
                    case BluetoothAdapter.STATE_ON:
                        Timber.i("Bluetooth connected");
                        startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Timber.i("Bluetooth disconnected");
                        stopDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Timber.i("Disconnecting Bluetooth");
                        break;
                }
            }
        }
    };

    public void init(Context context, MarknListener listener){
        mContext = context;
        mListener = listener;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter==null){
            Timber.i("We cant use Bluetooth.");
        }else if(mBluetoothAdapter.isEnabled()){
            startDiscovery();
        }else{
            mListener.notifyBluetoothActivationRequired();
        }
    }

    private void startDiscovery() {
        if (mContext == null) {
            Timber.i("Can't start discovery without a context");
            return;
        }
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Timber.i("Bluetooth Low Energy not available");
            return;
        }

        if (mBeaconManager == null) {
            Timber.i("Creating BeaconManager");
            BeaconManager.getInstanceForApplication(mContext).unbind(this);
            mBeaconManager = BeaconManager.getInstanceForApplication(mContext);
            BeaconParser iBeaconParser = new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24");
            mBeaconManager.getBeaconParsers().clear();
            mBeaconManager.getBeaconParsers().add(iBeaconParser);
        }

        mBeaconManager.bind(this);
        Timber.i("Discovery started");
    }

    private void stopDiscovery() {
        if(mBeaconManager!=null){
            try {
                mBeaconManager.stopRangingBeaconsInRegion(MARKN_REGION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mBeaconManager.unbind(this);
        }

    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Timber.i("The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                    mListener.onBeaconsDetected(beacons);
                }
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(MARKN_REGION);
        } catch (RemoteException e) {    }
    }
}
