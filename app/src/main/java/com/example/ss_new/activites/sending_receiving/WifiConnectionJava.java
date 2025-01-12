package com.example.ss_new.activites.sending_receiving;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ss_new.adapters.recycler_adapter.WifiAvailableDeviceAdapter;
import com.example.ss_new.ads.NativeAdManager;
import com.example.ss_new.app_utils.data_classes.WifiReceiver;
import com.example.ss_new.app_utils.data_classes.PORTNO;
import com.example.ss_new.app_utils.data_classes.connection.ConnectionHelper;
import com.example.ss_new.app_utils.data_classes.connection.Sockets;
import com.example.ss_new.app_utils.data_classes.my_interfaces.MyClickCallbackInterface;
import com.example.ss_new.app_utils.data_classes.my_interfaces.SuccessAndFailureInterface;
import com.example.ss_new.databinding.ActivityWifiConnectionBinding;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import timber.log.Timber;

public class WifiConnectionJava extends AppCompatActivity implements SuccessAndFailureInterface {

    private static final String TAG = WifiConnectionJava.class.getSimpleName();
    public static final String phoneKey = "phone";
    final ArrayList<WifiP2pDevice> peersList = new ArrayList<>();
    private final ArrayList<String> devicesArrayList = new ArrayList<>();
    private String phone;

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WifiReceiver wifiReceiver;
    private IntentFilter intentFilter;

    private ActivityWifiConnectionBinding binding;
    private WifiAvailableDeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWifiConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        new NativeAdManager(this).loadNative(this, binding.adView);

        phone = getIntent().getStringExtra(phoneKey);
//        initView();
        scanning();
        binding.btnBack.setOnClickListener(view -> {
            disconnectWifiDirect();
            onBackPressed();
        });


     /*   adapter = new WifiAvailableDeviceAdapter(this, devicesArrayList, new MyClickCallbackInterface() {
            @Override
            public void onItemClick(int position) {
                requestPairing(position);
            }
        });*/
        Log.e("TESTTAG", "devicesArrayList " + devicesArrayList);

//        binding.recSearchedDevices.setLayoutManager(new LinearLayoutManager(this));
//        binding.recSearchedDevices.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectWifiDirect();
    }

    private void scanning() {
        p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = p2pManager.initialize(this, Looper.getMainLooper(), null);
        disconnectWifiDirect();
        wifiReceiver = new WifiReceiver(this, p2pManager, p2pChannel, this);
        intentFilter = ConnectionHelper.INSTANCE.getIntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        // Register the receiver
        registerReceiver(wifiReceiver, intentFilter);

        scanDevices();
    }

    @SuppressLint("HardwareIds")
    public void scanDevices() {
        Log.e(TAG, "scanDevices: called");
        if (this != null) {

            if (checkLocationPermission(this)) {
                Log.e(TAG, "scanDevices: calle in if"+checkLocationPermission(this));

            }else {
                requestLocationPermission();
                Log.e(TAG, "scanDevices: calle in else"+checkLocationPermission(this));

                Toast.makeText(this, "else of permission", Toast.LENGTH_SHORT).show();
            }

            if (p2pManager != null && p2pChannel != null) {
                p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int reason) {
                    }
                });
            }

            if (Build.VERSION.SDK_INT >= 33) {
                WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wInfo = wifiManager.getConnectionInfo();
                    String macAddress = wInfo.getMacAddress();
                    if (p2pManager != null && p2pChannel != null) {
                        p2pManager.setConnectionRequestResult(
                                p2pChannel,
                                MacAddress.fromString(macAddress),
                                WifiP2pManager.CONNECTION_REQUEST_ACCEPT,
                                new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.e("TESTTAG", "onSuccess: connection success For android 33");
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.e("TESTTAG", "onFailure: connection failed For android 33 " + reason);
                                    }
                                });
                    }
                }
            }
        }
    }
    public boolean checkLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int coarseLocationPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION);
            int fineLocationPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION);
            int nearbyPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.NEARBY_WIFI_DEVICES);

            return coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                    && fineLocationPermission == PackageManager.PERMISSION_GRANTED
                    && nearbyPermission == PackageManager.PERMISSION_GRANTED;
        } else {
            int coarseLocationPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION);
            int fineLocationPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION);

            return coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                    && fineLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
    }
    public void requestLocationPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.NEARBY_WIFI_DEVICES,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Toast.makeText(WifiConnectionJava.this, "Location Granted", Toast.LENGTH_SHORT).show();

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                 if (deniedPermissions != null) {
                    for (String permission : deniedPermissions) {
                        Log.e("TESTTAG deniedPermissions", permission);
                    }
                }
                Toast.makeText(WifiConnectionJava.this, "Location Permissions required!", Toast.LENGTH_SHORT).show();

            }
        });
    }
//    private void initView() {
//        if (this != null) {
//            adapter = new WifiAvailableDeviceAdapter(this, devicesArrayList, new ClickInterface() {
//                @Override
//                public void onItemClick(int position) {
//                    // Uncomment the following block if needed
//                    // if (userType != "sender")
//                    // if (pairingSuccess) {
//                    requestPairing(position);
//                    // Log.e("TESTTAG", "onItemClick pairingSuccess iff: " + pairingSuccess);
//                    // } else {
//                    // Log.e("TESTTAG", "onItemClick pairingSuccess else: " + pairingSuccess);
//                    // }
//                    // else {
//                    //     Toast.makeText(requireContext(),
//                    //         "only receiver can request to connect",
//                    //         Toast.LENGTH_SHORT).show();
//                    // }
//                }
//            });
//
//            if (binding != null) {
//                binding.recSearchedDevices.setLayoutManager(new LinearLayoutManager(this));
//                binding.recSearchedDevices.setAdapter(adapter);
//            }
//        }
//    }

//    private void scanDevices() {
//        if (hasLocationPermission()) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    Log.e(TAG, "Discovery initiated successfully");
//                }
//
//                @Override
//                public void onFailure(int reason) {
//                    Log.e(TAG, "Discovery initiation failed: " + reason);
//                }
//            });
//        } else {
//            Toast.makeText(this, "Please add location permissions from Settings>Apps>Smart Switch>Permissions", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void requestPairing(int pos) {
        WifiP2pDevice device = peersList.get(pos);
        WifiP2pConfig wifiConfig = new WifiP2pConfig();
        wifiConfig.deviceAddress = device.deviceAddress;
        wifiConfig.wps.setup = WpsInfo.PBC;

        if (checkLocationPermission(this)) {
            p2pManager.connect(p2pChannel, wifiConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e(TAG, "Peer connection initiated");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Peer connection initiation failed: " + reason);
                }
            });
        } else {
            Toast.makeText(this, "Location permission is required for Wi-Fi Direct", Toast.LENGTH_SHORT).show();
        }
    }

    public final WifiP2pManager.ConnectionInfoListener connectionListener = info -> {
        if (info.groupFormed) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.isGroupOwner) {
                final ServerSocket[] serverSocket = {null};
                Thread thread = new Thread(() -> {
                    try {
                        serverSocket[0] = new ServerSocket(PORTNO.PORT);
                        serverSocket[0].setReuseAddress(true);

                        Socket socket = serverSocket[0].accept();

                        if (socket != null) {
                            Sockets.INSTANCE.setSocket(socket);
                            onSuccess();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception in server socket: " + e.getMessage());
                        onFailure(e.getMessage());
                    }
                });

                thread.start();
            } else {
                Thread thread = new Thread(() -> {
                    try {
                        Log.e(TAG, "Connecting to host address: " + groupOwnerAddress.getHostAddress());
                        Socket socket = new Socket();
                        InetSocketAddress socketAddress = new InetSocketAddress(groupOwnerAddress.getHostAddress(), PORTNO.PORT);
                        socket.connect(socketAddress, 10000);
                        socket.setSoTimeout(10000);

                        Log.e(TAG, "Connected to group owner address: " + groupOwnerAddress.getHostAddress());
                        Sockets.INSTANCE.setSocket(socket);
                        onSuccess();
                    } catch (IOException e) {
                        onFailure(e.getMessage());
                    }
                });

                thread.start();
            }
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    public final WifiP2pManager.PeerListListener peerListListener = peerList -> {
        if (!peerList.getDeviceList().equals(peersList)) {
            peersList.clear();
            peersList.addAll(peerList.getDeviceList());
            devicesArrayList.clear();

            for (WifiP2pDevice device : peerList.getDeviceList()) {
                devicesArrayList.add(device.deviceName);
                Log.e("TESTTAG", "Device Name: " + device.deviceName);
            }

            adapter.notifyDataSetChanged();
        }
    };

    private void disconnectWifiDirect() {
        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            p2pManager.requestGroupInfo(p2pChannel, group -> {
                if (group != null) {
                    p2pManager.removeGroup(p2pChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG, "removeGroup onSuccess");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.e(TAG, "removeGroup onFailure: " + reason);
                        }
                    });
                }
            });
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wifiReceiver != null) {
            registerReceiver(wifiReceiver, intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (wifiReceiver != null) {
                unregisterReceiver(wifiReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregister exception: " + e);
        }
    }

    @Override
    public void onSuccess() {
        if ("old".equals(phone)) {
            startActivity(new Intent(this, FilerDataSendingActivityJava.class));
            finish();
        } else {
            startActivity(new Intent(this, FilerDataReceivingActivity.class));
            finish();
        }
    }

    @Override
    public void onFailure(String reason) {
        Log.e(TAG, "failure: " + reason);
        scanning();
    }
}


//package com.example.ss_new.activites.sending_receiving;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.net.wifi.WpsInfo;
//import android.net.wifi.p2p.WifiP2pConfig;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pInfo;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.os.Bundle;
//import android.os.Looper;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import com.example.ss_new.adapters.recycler_adapter.WifiAvailableDeviceAdapter;
//import com.example.ss_new.ads.NativeAdManager;
//import com.example.ss_new.app_utils.data_classes.WifiReceiver;
//import com.example.ss_new.app_utils.data_classes.PORTNO;
//import com.example.ss_new.app_utils.data_classes.connection.ConnectionHelper;
//import com.example.ss_new.app_utils.data_classes.connection.Sockets;
//import com.example.ss_new.app_utils.data_classes.my_interfaces.MyClickCallbackInterface;
//import com.example.ss_new.app_utils.data_classes.my_interfaces.SuccessAndFailureInterface;
//import com.example.ss_new.databinding.ActivityWifiConnectionBinding;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.ArrayList;
//
//import timber.log.Timber;
//
//public class WifiConnectionJava extends AppCompatActivity implements SuccessAndFailureInterface {
//
//    private static final String TAG = WifiConnectionJava.class.getSimpleName();
//    public static final String phoneKey = "phone";
//    final ArrayList<WifiP2pDevice> peersList = new ArrayList<>();
//    private final ArrayList<String> devicesArrayList = new ArrayList<>();
//    private String phone;
//
//    private WifiP2pManager p2pManager;
//    private WifiP2pManager.Channel p2pChannel;
//    private WifiReceiver wifiReceiver;
//    private IntentFilter intentFilter;
//
//    private ActivityWifiConnectionBinding binding;
//    private WifiAvailableDeviceAdapter adapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityWifiConnectionBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        new NativeAdManager(this).loadNative(this, binding.adView);
//
//        phone = getIntent().getStringExtra(phoneKey);
//
//        binding.btnBack.setOnClickListener(view -> {
//            disconnectWifiDirect();
//            onBackPressed();
//        });
//
//        binding.refresh.setOnClickListener(view -> scanning());
//
//        adapter = new WifiAvailableDeviceAdapter(this, devicesArrayList, new MyClickCallbackInterface() {
//            @Override
//            public void onItemClick(int position) {
//                requestPairing(position);
//            }
//        });
//        Log.e("TESTTAG", "devicesArrayList " + devicesArrayList);
//
//        binding.recSearchedDevices.setLayoutManager(new LinearLayoutManager(this));
//        binding.recSearchedDevices.setAdapter(adapter);
//
//        scanning();
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        disconnectWifiDirect();
//    }
//
//    private void scanning() {
//        p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        p2pChannel = p2pManager.initialize(this, Looper.getMainLooper(), null);
//        disconnectWifiDirect();
//        wifiReceiver = new WifiReceiver(this, p2pManager, p2pChannel, WifiConnectionJava.this);
//        intentFilter = ConnectionHelper.INSTANCE.getIntentFilter();
//        scanDevices();
//    }
//
//    private void scanDevices() {
//        if (hasLocationPermission()) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//
//            p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    Timber.e("$TAG Success");
//                }
//
//                @Override
//                public void onFailure(int reason) {
//                    Timber.e("$TAG Failed: $reason");
//                }
//            });
//        } else {
//            Toast.makeText(this, "Please add location permissions from Settings>Apps>Smart Switch>Permissions", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void requestPairing(int pos) {
//        WifiP2pDevice device = peersList.get(pos);
//        WifiP2pConfig wifiConfig = new WifiP2pConfig();
//        wifiConfig.deviceAddress = device.deviceAddress;
//        wifiConfig.wps.setup = WpsInfo.PBC;
//
//        if (hasLocationPermission()) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//
//            p2pManager.connect(p2pChannel, wifiConfig, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    Timber.e("$TAG: peer connection");
//                }
//
//                @Override
//                public void onFailure(int reason) {
//                    Timber.e("$TAG: failed peer connection");
//                }
//            });
//        } else {
//            // Handle the case where location permission is not granted
//        }
//    }
//
//    public final WifiP2pManager.ConnectionInfoListener connectionListener = info -> {
//        WifiP2pInfo wifiP2pInfo = info;
//        if (wifiP2pInfo.groupFormed) {
//            InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
//
//            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
//                final ServerSocket[] serverSocket = {null};
//                Thread thread = new Thread(() -> {
//                    try {
//                        serverSocket[0] = new ServerSocket(PORTNO.PORT);
//                        serverSocket[0].setReuseAddress(true);
//
//                        Socket socket = serverSocket[0].accept();
//
//                        if (socket != null) {
//                            Sockets.INSTANCE.setSocket(socket);
//                            onSuccess();
//                        }
//                    } catch (IOException e) {
//                        Timber.e("$TAG Exception server 1: ${e.getMessage()}");
//                        onFailure(e.getMessage());
//                    }
//                });
//
//                thread.start();
//            } else if (wifiP2pInfo.groupFormed) {
//                Thread thread = new Thread(() -> {
//                    try {
//                        Timber.e("run: host address" + groupOwnerAddress.getHostAddress());
//                        Socket socket = new Socket();
//                        InetSocketAddress socketAddress = new InetSocketAddress(groupOwnerAddress.getHostAddress(), PORTNO.PORT);
//                        socket.connect(socketAddress, 10000);
//                        socket.setSoTimeout(10000);
//
//                        Timber.e("run: local groupOwnerAddress" + groupOwnerAddress.getHostAddress());
//                        Sockets.INSTANCE.setSocket(socket);
//                        onSuccess();
//                    } catch (IOException e) {
//                        onFailure(e.getMessage());
//                    }
//                });
//
//                thread.start();
//            }
//        }
//    };
//
//    public final WifiP2pManager.PeerListListener peerListListener = peerList -> {
//        if (!peerList.getDeviceList().equals(peersList)) {
//            peersList.clear();
//            peersList.addAll(peerList.getDeviceList());
//            devicesArrayList.clear();
//
//            for (WifiP2pDevice device : peerList.getDeviceList()) {
//                devicesArrayList.add(device.deviceName);
//                Log.e("TESTTAG", "Devices Name" + device.deviceName);
//            }
//
//            adapter.notifyDataSetChanged();
//        }
//    };
//
//    private void disconnectWifiDirect() {
//        if (hasLocationPermission()) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//
//            p2pManager.requestGroupInfo(p2pChannel, group -> {
//                if (group != null) {
//                    p2pManager.removeGroup(p2pChannel, new WifiP2pManager.ActionListener() {
//                        @Override
//                        public void onSuccess() {
//                            Timber.e(TAG, "removeGroup onSuccess -");
//                        }
//
//                        @Override
//                        public void onFailure(int reason) {
//                            Timber.e("$TAG removeGroup onFailure -$reason");
//                        }
//                    });
//                }
//            });
//        }
//    }
//
//    private boolean hasLocationPermission() {
//        String[] storagePerB13 = new String[]{
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//        };
//
//        for (String permission : storagePerB13) {
//            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (wifiReceiver != null) {
//            registerReceiver(wifiReceiver, intentFilter);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        try {
//            if (wifiReceiver != null) {
//                unregisterReceiver(wifiReceiver);
//            }
//        } catch (Exception e) {
//            Timber.e("unregister exception: $e");
//        }
//    }
//
//    @Override
//    public void onSuccess() {
//        if ("old".equals(phone)) {
//            startActivity(new Intent(this, FilerDataSendingActivityJava.class));
//            finish();
//        } else {
//            startActivity(new Intent(this, FilerDataReceivingActivity.class));
//            finish();
//        }
//    }
//
//    @Override
//    public void onFailure(String reason) {
//        Timber.e("$TAG: failure $reason");
//        scanning();
//    }
//}
//
