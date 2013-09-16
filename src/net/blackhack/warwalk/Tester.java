package net.blackhack.warwalk;

import android.util.Log;

public class Tester {
	
	//TEST functions
	public static void testDB(){
		Log.d("Test: ", "Running test...");
        
		//String str;
		//Network testnet = new Network("testnet01", "00:11:22:33:44:55", "WPA", -55, 2361, -52.124284, 12.235742);
			
		// add
		//WarWalk.db.addNetwork(testnet);
			
		// get by id
		//str = WarWalk.db.getNetwork(1).toString();
        //Log.d("GetByID Test: ", str);
	       
        // get by ssid
        //str = WarWalk.db.getNetworkBySSID("testnet01").toString();
        //Log.d("GetBySSID Test: ", str);
	        
        // get by bssid
        //str = WarWalk.db.getNetwork("90:01:3B:18:2D:5A").toString();
        //Log.d("GetByBSSID Test: ", str);
	        
        // search
        //if (WarWalk.db.doesNetworkExist("13:37:13:37:13:37"))
    	//	Log.d("SearchTest: ","Network exists");
        //else
        //	Log.d("SearchTest: ","Network does not exist");
	        
        // update
        //testnet = new Network("hackednet", "00:11:22:33:44:55", "WPA", -55, 2361, -52.124284, 12.235742);
        //WarWalk.db.updateNetwork(testnet);
	        
        // delete
        //WarWalk.db.deleteNetwork("SKY82D59");
        //WarWalk.db.deleteNetwork(WarWalk.db.getNetworkBySSID("hackednet"));
        //WarWalk.db.doesNetworkExist("90:01:3B:18:2D:5A");
        //WarWalk.db.deleteAllNetworks();
        
        // get all networks
        //WarWalk.db.getAllNetworks();
        //WarWalk.db.deleteAllNetworks();
        
        //test network.getChannel()
    }	
}
