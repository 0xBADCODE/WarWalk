package net.blackhack.warwalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	
	// Database Version
	private static final int DATABASE_VERSION = 1;
	
	// Database Name
	private static final String DATABASE_NAME = "networks.db";
	private static final String ENCRYPTED_DATABASE_NAME = "networks.db.des";

	// Database file setup
	private static File externalDir = Environment.getExternalStorageDirectory();
	private static File internalDir = Environment.getDataDirectory();
	private static File DATABASE = new File(internalDir, 
			"/data/net.blackhack.warwalk/databases/" + DATABASE_NAME);
	private static File ENCRYPTED_DATABASE = new File(internalDir, 
			"/data/net.blackhack.warwalk/databases/" + ENCRYPTED_DATABASE_NAME);
	private static File DATABASE_SD = new File(externalDir, 
			"/Android/data/net.blackhack.warwalk/databases/" + ENCRYPTED_DATABASE_NAME);
	private static File filesystem = new File(externalDir,"/Android/data/net.blackhack.warwalk/databases/");
	
	private static final String

	// Table name
	TABLE_NAME = "Networks",

	// Table columns names
	KEY_ID = "ID",
	KEY_SSID = "SSID",
	KEY_BSSID = "BSSID",
	KEY_SEC = "Security",
	KEY_TXPOWER = "txPower",
	KEY_FREQ = "Frequency",
	KEY_LAT =  "Latitude",
	KEY_LONG = "Longitude",
	KEY_ACCURACY = "Accuracy";

	// Init db
	private SQLiteDatabase db;
	private Cursor cursor;

	protected DatabaseHandler(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	protected String getDatabaseName(){
		return DATABASE_NAME;
	}
	
	protected String getEncryptedDatabaseName(){
		return ENCRYPTED_DATABASE_NAME;
	}
	
	File getDatabaseInternal(){
		return DATABASE;
	}
	
	File getEncryptedDatabase(){
		return ENCRYPTED_DATABASE;
	}
	
	File getDatabaseSD(){
		createFilesystem();
		return DATABASE_SD;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Syslog.systemLog(0, "Initializing database");
		if (checkDataBase()){
			// do nothing, database found
			Log.d("Database: ", "database found");
		}
		else{
	        Log.d("Database: ", "database not found");
	        Syslog.systemLog(1, "Failed to initialize database, creating...");
	        
	    	// Create table
			String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ KEY_SSID + " TEXT,"
				+ KEY_BSSID + " TEXT,"
				+ KEY_SEC + " TEXT," 
				+ KEY_TXPOWER + " INTEGER,"
				+ KEY_FREQ + " INTEGER,"
				+ KEY_LAT + " REAL,"
				+ KEY_LONG + " REAL,"
				+ KEY_ACCURACY + " REAL" + ")";
			Log.d("Table create: ", "creating table...");
			db.execSQL(CREATE_TABLE);
		}
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	private boolean checkDataBase() {
		Log.d("Database: ", "checking db...");
		db = null;
        try {
        	db = SQLiteDatabase.openDatabase(DATABASE.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        	if (db != null)
        		db.close();
        } catch (Exception e) {
        //	e.printStackTrace();
        	Log.d("Database: ", "check complete");
        }
        return db != null ? true : false;
	}

	/*
	* All CRUD(Create, Read, Update, Delete) Operations
	*/

	// Adding new network
	protected void addNetwork(Network network) {
		db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SSID, network.getSSID()); // SSID
		values.put(KEY_BSSID, network.getBSSID()); // BSSID
		values.put(KEY_SEC, network.getSecurity()); // Security Capabilities
		values.put(KEY_TXPOWER, network.gettxPower()); // txPower
		values.put(KEY_FREQ, network.getFreq()); // Frequency (Channel)
		if (WarWalk.geolocate.canGetLocation && WarWalk.checkedGPS()){
			values.put(KEY_LAT, network.getLatitude()); // Latitude
			values.put(KEY_LONG, network.getLongitude()); // Longitude
			values.put(KEY_ACCURACY, network.getAccuracy()); // Accuracy
		}
		Log.d("Adding network: ", network.getSSID());
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	
	// Get single network by ID
	protected Network getNetwork(int id) {

		Network network = null;
		
		try {
			db = this.getReadableDatabase();
			cursor = db.query(TABLE_NAME, new String[] { 
					KEY_ID, KEY_SSID, KEY_BSSID, KEY_SEC, KEY_TXPOWER, KEY_FREQ, KEY_LAT, KEY_LONG, KEY_ACCURACY },
					KEY_ID + "=?",
					new String[] { String.valueOf(id) }, null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
		
			Log.d("Getting network: ", "reading...");
			network = new Network(cursor.getString(1), cursor.getString(2), cursor.getString(3),
					(cursor.getInt(4)),(cursor.getInt(5)),(cursor.getDouble(6)),(cursor.getDouble(7)),
					(cursor.getFloat(8)));
			db.close();
			} catch (Exception e){
				Log.d("Getting network: ", "failed");
			}
		return network;
	} 
	
	// Get single network by SSID
	protected Network getNetworkBySSID(String ssid) {
		
		Network network = null;
		
		try {
			db = this.getReadableDatabase();
			cursor = db.query(TABLE_NAME, new String[] { 
					KEY_ID, KEY_SSID, KEY_BSSID, KEY_SEC, KEY_TXPOWER, KEY_FREQ, KEY_LAT, KEY_LONG, KEY_ACCURACY },
					KEY_SSID + "=?",
					new String[] { ssid }, null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
		
			Log.d("Getting network: ", "reading...");
			network = new Network(cursor.getString(1), cursor.getString(2), cursor.getString(3),
					(cursor.getInt(4)),(cursor.getInt(5)),(cursor.getDouble(6)),(cursor.getDouble(7)),
					(cursor.getFloat(8)));
			db.close();
			} catch (Exception e){
				Log.d("Getting network: ", "failed");
			}
		return network;
	}
	
	// Get single network by BSSID
	protected Network getNetworkByBSSID(String bssid) {
		
		Network network = null;

		try {
			db = this.getReadableDatabase();
			cursor = db.query(TABLE_NAME, new String[] { 
					KEY_ID, KEY_SSID, KEY_BSSID, KEY_SEC, KEY_TXPOWER, KEY_FREQ, KEY_LAT, KEY_LONG, KEY_ACCURACY },
					KEY_BSSID + "=?",
					new String[] { bssid.toLowerCase(Locale.getDefault()) }, null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
			
			Log.d("Getting network: ", "reading...");
			network = new Network(cursor.getString(1), cursor.getString(2), cursor.getString(3),
					(cursor.getInt(4)),(cursor.getInt(5)),(cursor.getDouble(6)),(cursor.getDouble(7)),
					(cursor.getFloat(8)));
			db.close();
			} catch (Exception e){
				Log.d("Getting network: ", "failed");
			}
		return network;
	} 

	// Getting All Networks
	protected List<Network> getAllNetworks() {
		List<Network> networkList = new ArrayList<Network>();
		
		db = this.getReadableDatabase();
		Network net = null;
		cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

		if (cursor.moveToFirst()) {
			do {
				net = new Network();
				net.setID(cursor.getInt(0));
				net.setSSID(cursor.getString(1));
				net.setBSSID(cursor.getString(2));
				net.setSecurity(cursor.getString(3));
				net.settxPower(cursor.getInt(4));
				net.setFreq(cursor.getInt(5));
				net.setLatitude(cursor.getDouble(6));
				net.setLongitude(cursor.getDouble(7));
				net.setAccuracy(cursor.getFloat(8));
				
				// Adding network to list
				networkList.add(net);
			} while (cursor.moveToNext());
		}
		db.close();

		Log.d("Generating network list... ", "done");
		return networkList;
	}

	// Update network
	protected void updateNetwork(Network network) {
		db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SSID, network.getSSID()); // SSID
		values.put(KEY_BSSID, network.getBSSID()); // BSSID
		values.put(KEY_SEC, network.getSecurity()); // Security Capabilities
		values.put(KEY_TXPOWER, network.gettxPower()); // txPower
		values.put(KEY_FREQ, network.getFreq()); // Frequency (Channel)
		if (WarWalk.geolocate.canGetLocation && WarWalk.checkedGPS()){
			values.put(KEY_LAT, network.getLatitude()); // Latitude
			values.put(KEY_LONG, network.getLongitude()); // Longitude
			values.put(KEY_ACCURACY, network.getAccuracy()); // Accuracy
		}
		Log.d("Updating network: ", network.getSSID());
		db.update(TABLE_NAME, values, KEY_BSSID + " = ?",
				new String[] { network.getBSSID() });
	}

	// Deleting network
	protected void deleteNetwork(Network network) {
		try {
			db = this.getWritableDatabase();
			db.delete(TABLE_NAME, KEY_BSSID + " = ?",
					new String[] { network.getBSSID() });
			Log.d("Deleting network: ", network.getSSID());
			db.close();
		} catch (Exception e) {
			Log.d("Deleting network: ", "failed");
			e.printStackTrace();
		}
	}
	
	// Deleting network by ssid
	protected void deleteNetworkBySSID(String ssid) {
		try {
			db = this.getWritableDatabase();
			db.delete(TABLE_NAME, KEY_SSID + " = ?",
					new String[] { ssid });
			Log.d("Deleting network: ", ssid);
			db.close();
		} catch (Exception e) {
			Log.d("Deleting network: ", "failed");
			e.printStackTrace();
		}
	}
	
	// Deleting network by bssid
	protected void deleteNetworkByBSSID(String bssid) {
		try {
			db = this.getWritableDatabase();
			db.delete(TABLE_NAME, KEY_BSSID + " = ?",
					new String[] { bssid });
			Log.d("Deleting network: ", bssid);
			db.close();
		} catch (Exception e) {
			Log.d("Deleting network: ", "failed");
			e.printStackTrace();
		}
	}
	
	// Deleted all networks
	protected void deleteAllNetworks(){
		db = this.getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
		db.close();
		Log.d("Resetting database: ", "done");
	}
	
	
	// Check for network
	protected boolean doesNetworkExist(String bssid){
		Network s = null;
		try {
			s = getNetworkByBSSID(bssid.toLowerCase(Locale.getDefault()));
		} catch (Exception e){
			Log.d("Network exist?: ", "failed");
			e.printStackTrace();
		}
		if (s != null)
			Log.d("Network exist?: ", "true");
		else
			Log.d("Network exist?: ", "false");
		return s != null ? true : false;
	}

	// Network count
	protected long getNetworkCount() {
		db = this.getReadableDatabase();
		long count = DatabaseUtils.queryNumEntries(db,TABLE_NAME);
		db.close();
		return count;
	} 
	
	// create external filesystem
	protected static void createFilesystem(){
		if (!filesystem.isDirectory()){
			filesystem.mkdirs();
			Log.d("creating filesystem: ","done");
		}
	}
	
	// encrypt database
	protected static String encryptDatabase() throws IOException {
		
		Random rand = new Random();
		String key = Integer.toString(rand.nextInt(100000000-10000000) + 10000000);
	
		try {
			FileInputStream fis = new FileInputStream(DATABASE);
			FileOutputStream fos = new FileOutputStream(ENCRYPTED_DATABASE);
		
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey desKey = skf.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			CipherInputStream cis = new CipherInputStream(fis, cipher);
			
			byte[] bytes = new byte[64];
			int numBytes;
			while ((numBytes = cis.read(bytes)) != -1) {
				fos.write(bytes, 0, numBytes);
			}
			fos.flush();
			fos.close();
			cis.close();
			
			Log.d("Encrypting Database...", "done");
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.d("Encryption: ", "file not found");
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Encryption: ", "failed");
		}
		Syslog.systemLog(1, "Encryption key: " + key.toString());
		return key;
	}
}
