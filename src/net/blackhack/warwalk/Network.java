package net.blackhack.warwalk;

public class Network {
	
	//private variables
		private int 	id,
						txpower,
						freq;
		
		private double	latitude,
						longitude;
		
		private float 	accuracy;
		
		private String 	ssid,
						bssid,
						security;
		
		// constructor
		protected Network(){}
		
		protected Network(String ssid, String bssid, String security, int txpower, int freq){
			
			this.ssid = ssid;
			this.bssid = bssid.toLowerCase();
			this.security = security;
			this.txpower = txpower;
			this.freq = freq;
		}
		
		protected Network(String ssid, String bssid, String security, int txpower, int freq, 
						double latitude, double longitude, float accuracy){
			
			this.ssid = ssid;
			this.bssid = bssid.toLowerCase();
			this.security = security;
			this.txpower = txpower;
			this.freq = freq;
			this.latitude = latitude;
			this.longitude = longitude;
			this.accuracy = accuracy;
		}
		
		// getting id
		protected int getID(){
			return this.id;
		}
		
		// setting id
		protected void setID(int id){
			this.id = id;
		}
		
		// getting ssid
		protected String getSSID(){
			return this.ssid;
		}
		
		// setting ssid
		protected void setSSID(String ssid){
			this.ssid = ssid;
		}
		
		// getting bssid
		protected String getBSSID(){
			return this.bssid;
		}
		
		// setting bssid
		protected void setBSSID(String bssid){
			this.bssid = bssid.toLowerCase();
		}
		
		// getting security
		protected String getSecurity(){
			return this.security;
		}
				
		// setting security
		protected void setSecurity(String security){
			this.security = security;
		}
		
		// getting txpower
		protected int gettxPower(){
			return this.txpower;
		}
				
		// setting txpower
		protected void settxPower(int txpower){
			this.txpower = txpower;
		}
		
		// getting frequency
		protected int getFreq(){
			return this.freq;
		}
				
		// setting frequency
		protected void setFreq(int freq){
			this.freq = freq;
		}
		
		// getting latitude
		protected double getLatitude(){
			return this.latitude;
		}
				
		// setting latitude
		protected void setLatitude(double latitude){
			this.latitude = latitude;
		}
		
		// getting longitude
		protected double getLongitude(){
			return this.longitude;
		}
									
		// setting longitude
		protected void setLongitude(double longitude){
			this.longitude = longitude;
		}
		
		// getting accuracy
		protected double getAccuracy(){
			return this.accuracy;
		}
											
		// setting accuracy
		protected void setAccuracy(float accuracy){
			this.accuracy = accuracy;
		}
		
		// get channel
		protected int getChannel() {
			
			int 	channel = -1,
					freq = getFreq();
			
			if (freq == 2412) channel = 1;
			else 
				if (freq == 2417) channel = 2;
			else 
				if (freq == 2422) channel = 3;
			else 
				if (freq == 2427) channel = 4;
			else 
				if (freq == 2432) channel = 5;
			else 
				if (freq == 2437) channel = 6;
			else 
				if (freq == 2442) channel = 7;
			else 
				if (freq == 2447) channel = 8;
			else 
				if (freq == 2452) channel = 9;
			else 
				if (freq == 2457) channel = 10;
			else 
				if (freq == 2462) channel = 11;
			else 
				if (freq == 2467) channel = 12;
			else 
				if (freq == 2472) channel = 13;
			else 
				if (freq == 2484) channel = 14;
			
			return channel;
		}
		
		public String toMenuString(){
			String menustr;
			
			if (getSecurity().contentEquals(""))
				menustr = getSSID() + "\n" + getBSSID() + "\nAnalysis: Open";
			else 
				if (getSecurity().contains("WEP") || getSecurity().contains("WPS"))
				menustr = getSSID() + "\n" + getBSSID() + "\nAnalysis: Vulnerable";
			else 
				if (getSecurity().contains("WPA"))
				menustr = getSSID() + "\n" + getBSSID() + "\nAnalysis: Secure";
			else
				menustr = getSSID() + "\n" + getBSSID() + "\nAnalysis: Unknown";
			return menustr;
		}
		
		public String toString(){
			String str;
			if (getSecurity().contentEquals("")){
				if (getLatitude() != 0 && getLongitude() != 0){
					str = getSSID() + "\n" + getBSSID() + "\nSignal: " 
							+ gettxPower() + " dBm\nChannel: " + getChannel() + " (" + getFreq() + " Hz)" + "\nGPS: " 
							+ getLatitude() + " " + getLongitude() + " /" + getAccuracy();
				}
				else {
					str = getSSID() + "\n" + getBSSID() + "\nSignal: " 
							+ gettxPower() + " dBm\nChannel: " + getChannel() + " (" + getFreq() + " Hz)";
				}
			}
			else {
				if (getLatitude() != 0 && getLongitude() != 0){
					str = getSSID() + "\n" + getBSSID() + "\n" + getSecurity() + "\nSignal: " 
							+ gettxPower() + " dBm\nChannel: " + getChannel() + " (" + getFreq() + " Hz)" + "\nGPS: " 
							+ getLatitude() + " " + getLongitude() + " /" + getAccuracy();
				}
				else {
					str = getSSID() + "\n" + getBSSID() + "\n" + getSecurity() + "\nSignal: " 
							+ gettxPower() + " dBm\nChannel: " + getChannel() + " (" + getFreq() + " Hz)";
				}
			}
			return str;
		}
}
