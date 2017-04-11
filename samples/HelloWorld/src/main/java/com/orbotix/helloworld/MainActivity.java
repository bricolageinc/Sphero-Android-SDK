package com.orbotix.helloworld;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello World Sample
 * Connect either a Bluetooth Classic or Bluetooth LE robot to an Android Device, then
 * blink the robot's LED on or off every two seconds.
 *
 * This example also covers turning on Developer Mode for LE robots.
 */

public class MainActivity extends Activity implements RobotChangedStateListener, DiscoveryAgentEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

    private Map<String, ConvenienceRobot> mRobotMap = new HashMap<String, ConvenienceRobot>();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            Associate a listener for robot state changes with the DualStackDiscoveryAgent.
            DualStackDiscoveryAgent checks for both Bluetooth Classic and Bluetooth LE.
            DiscoveryAgentClassic checks only for Bluetooth Classic robots.
            DiscoveryAgentLE checks only for Bluetooth LE robots.
       */
        //DualStackDiscoveryAgent.getInstance().addRobotStateListener( this );

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.e( TAG, "Location permission has not already been granted" );
                List<String> permissions = new ArrayList<String>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()] ), REQUEST_CODE_LOCATION_PERMISSION );
            } else {
                Log.d( TAG, "Location permission already granted" );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        startDiscovery();
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    //Turn the robot LED on or off every two seconds
    private void blink(final ConvenienceRobot cRobot, final boolean lit ) {
        if( cRobot == null )
            return;

        if( lit ) {
            cRobot.setLed( 0.0f, 0.0f, 0.0f );
        } else {
            cRobot.setLed( 0.0f, 0.0f, 1.0f );
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                blink(cRobot, !lit);
            }
        }, 2000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            startDiscovery();
        }
    }

    private void startDiscovery() {
/*
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery(getApplicationContext());
            } catch (DiscoveryException e) {
                Log.e(TAG, "DiscoveryException: " + e.getMessage());
            }
        }
*/
        DiscoveryAgentLE.getInstance().addDiscoveryListener(this);
        DiscoveryAgentLE.getInstance().addRobotStateListener(this); //TODO: ここを変更する必要がある

        RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
        robotRadioDescriptor.setNamePrefixes(new String[]{"BB-"});
        DiscoveryAgentLE.getInstance().setRadioDescriptor(robotRadioDescriptor);

        try {
            DiscoveryAgentLE.getInstance().startDiscovery(this);
        } catch (DiscoveryException e) {
            Log.e(TAG, "Discovery Error: " + e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        //If the DiscoveryAgent is in discovery mode, stop it.
//        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
//            DualStackDiscoveryAgent.getInstance().stopDiscovery();
//        }
        if(DiscoveryAgentLE.getInstance().isDiscovering()){
            DiscoveryAgentLE.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
//        if( mRobot != null ) {
//            mRobot.disconnect();
//            mRobot = null;
//        }

        for(ConvenienceRobot cRobot : mRobotMap.values()){
            cRobot.disconnect();
        }
        mRobotMap.clear();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
        DiscoveryAgentLE.getInstance().addRobotStateListener(null);
    }

    @Override
    public void handleRobotChangedState( Robot robot, RobotChangedStateNotificationType type ) {
        switch (type) {
/*
            case Online: {
                //If robot uses Bluetooth LE, Developer Mode can be turned on.
                //This turns off DOS protection. This generally isn't required.
                if( robot instanceof RobotLE) {
                    ( (RobotLE) robot ).setDeveloperMode( true );
                }

                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot( robot );

                //Start blinking the robot's LED
                blink( false );
                break;
            }
        }
*/
            case Online:
                Log.i(TAG, "Robot " + robot.getName() + " Online!");
                //If robot uses Bluetooth LE, Developer Mode can be turned on.
                //This turns off DOS protection. This generally isn't required.
                if (robot instanceof RobotLE) {
                    ((RobotLE) robot).setDeveloperMode(true);
                }

                //Save the robot as a ConvenienceRobot for additional utility methods
                ConvenienceRobot cRobot = new ConvenienceRobot(robot);

                if(!mRobotMap.containsKey(robot.getName())){
                    mRobotMap.put(robot.getName(), cRobot);
                }

                //Start blinking the robot's LED
                blink(cRobot, false);
                break;
            case Connecting:
                Log.i(TAG, "Robot " + robot.getName() + " Connecting!");
                break;
            case Connected:
                Log.i(TAG, "Robot " + robot.getName() + " Connected!");
                break;
            // Handle other cases
        }
    }

    @Override
    public void handleRobotsAvailable(List<Robot> robots) {
        Log.i(TAG, "Found " + robots.size() + " robots");

        for (Robot robot : robots) {
            Log.i(TAG, "  " + robot.getName());
        }
    }



}