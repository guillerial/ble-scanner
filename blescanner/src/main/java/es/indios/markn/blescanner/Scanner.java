package es.indios.markn.blescanner;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import es.indios.markn.blescanner.models.Topology.Indication;
import es.indios.markn.blescanner.models.Topology.Route;
import timber.log.Timber;

public class Scanner implements BeaconConsumer, PathGuide.PathGuideListener{

    private static Scanner instance;
    private Context mContext;
    private ArrayList<MarknListener> mListeners = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconManager mBeaconManager;
    private PathGuide mPathGuide;

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

    public static Scanner getInstance(){
        if(instance==null){
            instance = new Scanner();
            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
            }
        }
        return instance;
    }

    public void init(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter==null){
            Timber.i("We cant use Bluetooth.");
        }else if(mBluetoothAdapter.isEnabled()){
            startDiscovery();
        }else{
            if(!mListeners.isEmpty())
                for (MarknListener listener : mListeners) {
                    listener.notifyBluetoothActivationRequired();
                }
        }
    }

    public void subscribeListener(MarknListener listener){
        if(!mListeners.contains(listener))
            mListeners.add(listener);
    }

    public void deleteListener(MarknListener listener){
        if(mListeners.contains(listener))
            mListeners.remove(listener);
    }

    public void deleteListeners(){
        mListeners = new ArrayList<>();
    }

    public void setTopology(ArrayList<Route> topology, ArrayList<Indication> indications){
        mPathGuide = new PathGuide(topology, indications, this);
    }

    public void setDestination(String destinationId){
        if(mPathGuide!=null)
            mPathGuide.setDestination(destinationId);
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
                    ArrayList<Beacon> beaconList = new ArrayList<>(beacons);
                    Collections.sort(beaconList,new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon b1, Beacon b2) {
                            return Double.compare(b1.getDistance(), b2.getDistance());
                        }
                    });
                    Timber.i("The first beacon I see is about "+beaconList.get(0).getDistance()+" meters away.");
                    if(mPathGuide!=null)
                        mPathGuide.onBeaconsDetected(beaconList);
                    if(!mListeners.isEmpty())
                        for(MarknListener listener : mListeners)
                            listener.onBeaconsDetected(beaconList);
                }
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(MARKN_REGION);
        } catch (RemoteException e) {    }
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        mContext.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return mContext.bindService(intent, serviceConnection, i);
    }

    @Override
    public void onNewIndication(Indication indication) {
        if(!mListeners.isEmpty())
            for (MarknListener listener : mListeners)
                listener.onNewIndication(indication);
    }
}
