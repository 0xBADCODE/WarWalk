/* WarWalk 1.0 beta
 * Written by Xeon Nov 2012
 */

package net.blackhack.warwalk;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WarWalk extends Activity{

/* TODO
* map draw features // next version
* settings and preferences activity
* add sort database
* fix list numbering
* 
*/
	
	private static CheckBox geolocation;
	private static Button clear;
	private static TextView wifitext;
	private static TextView delay;
	private static TextView gpstext;
	private static WifiManager wifi;
	private static BroadcastReceiver receiver;
	
	protected static GeoLocate geolocate;
    protected static DatabaseHandler db;
    
	private boolean exitbool = false;
	private long sysTime;
	
	protected PowerManager.WakeLock mWakeLock;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_war_walk);
        
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "net.blackhack.warwalk.mWakeLock");
        Syslog.systemLog(0, "Starting WarWalk application");
        
        // Setup UI
        Log.d("UI setup: ", "Setting up UI...");
        clear = (Button) this.findViewById(R.id.clearscreen);
        wifitext = (TextView) this.findViewById(R.id.wifitext);
        wifitext.setFocusableInTouchMode(true);
        wifitext.requestFocus();
        delay = (TextView) this.findViewById(R.id.delaynumber);
        gpstext = (TextView) this.findViewById(R.id.gpstext);
        geolocation = (CheckBox) findViewById(R.id.geolocation);
        
        // Setup backend
        geolocate = new GeoLocate(this);
        db = new DatabaseHandler(this);
        
        // Check GPS status
        Syslog.systemLog(0, "Initializing GPS");
        if (geolocate.canGetLocation)
        	gpstext.setText("GPS: Ready");
        else {
        	gpstext.setText("GPS: Unavailable");
        	Syslog.systemLog(2, "Failed to initialize GPS");
        }
   
        // Setup WiFi
        wifi = (WifiManager) this.getSystemService(WIFI_SERVICE);
        Syslog.systemLog(0, "Initializing Wifi");
        startWifi();
    }
	
	private void startWifi(){
		while (wifi.isWifiEnabled() == false)
    	{
    		Toast.makeText(getApplicationContext(), "Starting Wifi...", Toast.LENGTH_SHORT).show();
    		wifi.setWifiEnabled(true);
    		delay(2);
    	} 
		wifitext.append("\n\nWifi ready. Press Scan.");
	}
	
	private void receiverSetup(){
		    
        // Register Broadcast Receiver
        if (receiver == null)
			receiver = new WiFiScanReceiver(this);

		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.d("Receiver status: ", "Receiver enabled");
	}
	
	private void populate() {
		String 	text = "",
				openap = "",
				apStr = "",
				sleep = delay.getText().toString(),
				location = Double.toString(geolocate.getLatitude()) + " " + Double.toString(geolocate.getLongitude());
		
		List<ScanResult> access_points = wifi.getScanResults();
		
		ScanResult ap;
		
		for(int i = 0; i < access_points.size(); i++){
			ap = access_points.get(i);
			
			//Check security
			apStr = ap.capabilities;
			if (apStr.contains("WPA") || apStr.contains("WEP") 
					|| apStr.contains("WPS") || apStr.contains("WPA2")) {
				text += "\n\n>SSID: " + ap.SSID 
						+ "\n>MAC: " + ap.BSSID 
						+ "\n>Security: " + ap.capabilities 
						+ "\n>Frequency: " + ap.frequency 
						+ " Hz\n>txPower: " + ap.level;
				if (geolocate.canGetLocation && checkedGPS())
						text  += " dBm\n>GPS: " + location;
			}
			else {
				if (apStr.contentEquals("")){
					openap += "\n\n>SSID: " + ap.SSID 
							+ "\n>MAC: " + ap.BSSID 
							+ "\n>Frequency: " + ap.frequency
							+ " Hz\n>txPower: " + ap.level;
					if (geolocate.canGetLocation && checkedGPS())
						openap  +=" dBm\n>GPS: " + location;
				}
			}
		}
		wifitext.setText(text + openap);
		
		addNetworkToDatabase(access_points);
		geotag();
		delay(Integer.parseInt(sleep));
		wifi.startScan();
	}
	
	// CRUD Operations
    // Adding networks 
	private void addNetworkToDatabase(List<ScanResult> access_points){
	
		ScanResult ap;
		Network network;
		String SSID,BSSID,SEC;
		int TXPOWER,FREQ;
		double LAT,LONG;
		float ACCURACY;
		
		for(int i = 0; i < access_points.size(); i++){
			ap = access_points.get(i);
			
			SSID = ap.SSID;
			BSSID = ap.BSSID;
			SEC = ap.capabilities;
			TXPOWER = ap.level;
			FREQ = ap.frequency;
			LAT = geolocate.getLatitude();
			LONG = geolocate.getLongitude();
			ACCURACY = geolocate.getAccuracy();
	
			if (checkedGPS() && geolocate.canGetLocation)
				network = new Network(SSID,BSSID,SEC,TXPOWER,FREQ,LAT,LONG,ACCURACY);
			else
				network = new Network(SSID,BSSID,SEC,TXPOWER,FREQ);
			
			if (!db.doesNetworkExist(ap.BSSID)){
				try {
					db.addNetwork(network);
				} catch (Exception e){
					Log.d("Adding network: ", "Failed");
					e.printStackTrace();
				}
			}
			else {
				if (ACCURACY != 0 && ACCURACY < db.getNetworkByBSSID(ap.BSSID).getAccuracy()){
					try {
						db.updateNetwork(network);
					} catch (Exception e){
						Log.d("Updating network: ", "Failed");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
    // Reading all networks
	protected static List<Network> listNetworks(){
		List<Network> networkList = db.getAllNetworks();
		return networkList;
	}
	
	class WiFiScanReceiver extends BroadcastReceiver {
		
		WarWalk wifiScanner;
		
		protected WiFiScanReceiver(WarWalk wifiStart) {
		super();
		this.wifiScanner = wifiStart;
		}
		
		@Override
		public void onReceive(Context c, Intent intent) {
			exitbool = false;
			clear.setText("Clear");
			wifiScanner.populate();
		}
	}
	
	public void onToggleClick(View view) {
	    // Is the toggle on?
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	    	if (wifi.isWifiEnabled() == false)
        		startWifi();
	    	delay.setFocusable(false);
	    	receiverSetup();
	    	this.mWakeLock.acquire();
			Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT).show();
			sysTime = System.currentTimeMillis();
			Syslog.systemLog(0, "Starting to scan all wifi networks in range");
	    	wifi.startScan();
	    } else {
	    	delay.setFocusableInTouchMode(true);
			Toast.makeText(getApplicationContext(), "Scan stopped.", Toast.LENGTH_SHORT).show();
			Syslog.systemLog(1, "Scanning halted after " + Long.toString(System.currentTimeMillis() - sysTime) + " ms");
	    	disableReceiver();
	    }
	}
	
	protected void delay(int num){
        if (num != 0){
        	try {
        		synchronized(this){
        			this.wait(num*1000);
            		}
        		}
        		catch(InterruptedException e){
        			e.printStackTrace();
        	}
        }
    }
	
	protected static boolean checkedGPS(){
		return geolocation.isChecked();
	}

	public void geotag() {
		if (checkedGPS() && geolocate.canGetLocation){
			
			double latitude = geolocate.getLatitude();
			double longitude = geolocate.getLongitude();
			
			String ns = "N ", we = "E ";
			if (latitude < 0) ns = "S ";
			if (longitude < 0) we = " W ";
			
			gpstext.setText("GPS: " + ns + geolocate.getStrLatitude() 
							+ "\n\t\t" + we + geolocate.getStrLongitude());
		} 
		else if (checkedGPS()){
			gpstext.setText("GPS: Unavailable");
		}
		else {
			gpstext.setText("GPS: Disabled");
		}
	}
	
	public void cls(View view){
		if (!exitbool && view.getId() == R.id.clearscreen){
			wifitext.setText("\n\nCleared. Press Scan.");
			Toast.makeText(getApplicationContext(), "Clearing...", Toast.LENGTH_SHORT).show();
			exitbool = true;
			clear.setText("Exit");
		}
		else close();
	}
	
	private void disableReceiver(){
		this.mWakeLock.release();
		try {
		if (receiver != null)
			unregisterReceiver(receiver);
		Log.d("Receiver status: ", "Receiver disabled");
		} catch (Exception e) {
            e.printStackTrace();
		}
	}
	
	// Menu functions
	private void showAuthor() {
	    Intent author = new Intent(this, Author.class);
	    startActivityForResult(author, 0);
	}
	
	private void showDBViewer() {
	    Intent dbview = new Intent(this, DBViewer.class);
	    startActivityForResult(dbview, 0);
	}
	
	private void showSyslog() {
		Intent logview = new Intent(this, Syslog.class);
		startActivityForResult(logview, 0);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_select, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        
        switch (item.getItemId())
        {
        case R.id.menu_dbviewer:
        	showDBViewer();
            return true;
        case R.id.menu_preferences:
        	//showPreferences(); //TODO
        	Tester.testDB();
            return true;
        case R.id.menu_syslog:
        	showSyslog();
        	return true;
        case R.id.menu_author:
        	showAuthor();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		close();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
	} 
	
	protected void close(){
		try {
			if (db.getDatabaseSD().exists())
				db.getDatabaseSD().delete();
			if (db.getEncryptedDatabase().exists());
				db.getEncryptedDatabase().delete();
			if (Syslog.logFile.exists())
				Syslog.logFile.delete();
			disableReceiver();
			geolocate.stopUsingGPS();
			if (mWakeLock.isHeld())
				this.mWakeLock.release();
		} catch (Exception e) {
            e.printStackTrace();
        }
		Toast.makeText(getApplicationContext(), "Exiting...", Toast.LENGTH_SHORT).show();
		finish();
	}
}