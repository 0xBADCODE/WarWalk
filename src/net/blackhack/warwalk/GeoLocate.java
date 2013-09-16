package net.blackhack.warwalk;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GeoLocate extends Service implements LocationListener {
	
	private final Context mContext;
	 
    // flag for GPS status
	protected boolean isGPSEnabled = false;

	protected boolean canGetLocation = false;
 
    Location location = null;
    private double 	latitude;
    private double 	longitude;
    private double 	timestamp;
    private float 	accuracy;
    private String 	strLatitude,
    				strLongitude;
    
 
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 15; // 15 meters
    private static final long MIN_TIME_BW_UPDATES = 10000; // 10 seconds
 
    private LocationManager locationManager;
 
    protected GeoLocate(Context context) {
        this.mContext = context;
        
        getLocation();
    }
    
    private Location getLocation() {
        try {
        	locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
     //       locationManager.addGpsStatusListener((Listener) this);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPSEnabled) {
                // no GPS provider is enabled
            	showSettingsAlert();
                this.canGetLocation = false;
                Log.d("GPS status: ", "GPS disabled");
            } else {
                if (isGPSEnabled) {
                    Log.d("GPS status: ", "GPS enabled");
                    // if GPS Enabled get coords. using GPS Services
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            this.canGetLocation = true;
                            Log.d("GPS status: ", "Updating...");
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                this.latitude = location.getLatitude();
                                this.longitude = location.getLongitude();
                                this.timestamp = location.getTime();
                                if (location.hasAccuracy())
                                	this.accuracy = location.getAccuracy();
                                this.strLatitude = Location.convert(latitude, 2);
                                this.strLongitude = Location.convert(longitude, 2);
                            }
                            else this.canGetLocation = false;
                        }
                    }
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
    
    protected double getLatitude(){
		if(this.canGetLocation)
            return this.latitude;
		else return 0;
    }
 
    protected double getLongitude(){
        if(this.canGetLocation)
            return this.longitude;
        else return 0;
    }
    
    
    protected double getTimestamp(){
    	if(this.canGetLocation)
    		return this.timestamp;
    	else return 0;
    }
    
    protected float getAccuracy(){
    	if(this.canGetLocation && location.hasAccuracy())
    		return this.accuracy;
    	else return 0;
    }
  	
    protected String getStrLatitude(){
    	if(this.canGetLocation)
    		return this.strLatitude;
    	else return null;
    }
   	
    protected String getStrLongitude(){
    	if(this.canGetLocation)
    		return this.strLongitude;
    	else return null;
    }
   	
    protected void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GeoLocate.this);
        }
        Log.d("GPS status: ", "Stopped");
    }
    
    protected void showSettingsAlert(){
        new AlertDialog.Builder(mContext)
        .setTitle("Cannot get location")
        .setMessage("GPS is not enabled. Continue without GPS logging?")
        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .show();
    }
    
    public void onLocationChanged(Location location) {
    	getLocation();
    }
 
    public void onProviderDisabled(String provider) {
    	
    }
 
    public void onProviderEnabled(String provider) {
    	
    }
 
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	
    /*	GpsStatus gpsStatus = locationManager.getGpsStatus(null); //TODO
        if(gpsStatus != null) {
            Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite>sat = satellites.iterator();
            int i=0;
            String strGpsStats = null;
			while (sat.hasNext()) {
                GpsSatellite satellite = sat.next();
                strGpsStats+= (i++) + ": " + satellite.getPrn() + "," + satellite.usedInFix() + "," + satellite.getSnr() + "," + satellite.getAzimuth() + "," + satellite.getElevation()+ "\n\n";
            }
            WarWalk.setWifiText(strGpsStats);
        } */
    }
 
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
