package net.blackhack.warwalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DBViewer extends Activity {
	
	private ListView dblistview;
	private TextView viewing;
	private ArrayAdapter<String> adapter;
	private LinkedHashMap<String,String> map;
	
	private int count;
	//private Point p = null;
	private List<Network> listNetworks = WarWalk.listNetworks();

	private String[] 	netstr,
						bssid;
	private String 		state;
	
	private final String DATABASE_BACKUP = "networks.db.bkup";

	private File internal = WarWalk.db.getDatabaseInternal();
	private File external = new File(Environment.getExternalStorageDirectory(), 
			"/Android/data/net.blackhack.warwalk/databases/" + DATABASE_BACKUP);
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_dbviewer);
        
        dblistview = (ListView)findViewById(R.id.list);
        viewing = (TextView)findViewById(R.id.viewing);
        
        state = Environment.getExternalStorageState();
		Log.d("External media state: ", state);
		
		if (!Environment.MEDIA_MOUNTED.equals(state)) {

			showExternalMediaAlert();
			Log.d("SDCARD: ", "not present");
			Syslog.systemLog(2, "External media unavailable");
		} 
		filterAll(listNetworks);
	}
	
	private void generateView(final LinkedHashMap<String,String> map){
	
		bssid = map.keySet().toArray(new String[map.size()]);
		netstr = map.values().toArray(new String[map.size()]);
		
		adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.text, netstr);
		
		dblistview.setAdapter(adapter);
		Toast.makeText(getApplicationContext(), "Running filter...", Toast.LENGTH_SHORT).show();
		Log.d("Setting adapter","done");
		dblistview.setClickable(true);
		dblistview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg2) {
				customContextMenu(position);
				return false;
			}
		});
	} 	
	
	private void customContextMenu(final int position){
		CharSequence[] items = new CharSequence[]{"Show Details","Copy to Clipboard","Sort Database","Delete Network"};
		
		new AlertDialog.Builder(this)
	    .setTitle("Operations")
	    .setIcon(R.drawable.icon_more)
	    .setItems(items, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int which) {
	        	   if (which == 0){
	        	   		String s = listNetworks.get((position)).toString();
	        	   		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
	        	   }
	        	   else
	        		   if (which == 1){
	        			   ClipboardManager clipboard = (ClipboardManager)
					       getSystemService(Context.CLIPBOARD_SERVICE);
	        			   clipboard.setText(listNetworks.get((position)).toString());
	        			   String s = listNetworks.get(position).getSSID() + " copied to clipboard";
	        			   Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	        		   }
					else
	        		   if (which == 2)
	        			   sortMenu();
	        	   else
	        		   if (which == 3)
	        			   showDeleteWarning(position);
	           }
	    })
	    .show();
	}
	
	private void sortMenu(){
		CharSequence[] items = new CharSequence[]{"Alphabetically","Signal strength","Distance"};
		
		new AlertDialog.Builder(this)
	    .setTitle("Sort")
	    .setIcon(R.drawable.icon_more)
	    .setItems(items, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int which) {
	        	   if (which == 0){
	        	   		sortAlphabetically();
	        	   }
	        	   else
	        		   if (which == 1){
	        			   sorttxPower();
	        		   }
	        	   else
	        		   if (which == 2){
	        			   sortDistance();
	        		   }
	           }
	    })
	    .show();
	}
	
	private void sortAlphabetically() {
		//TODO
	}
	
	private void sorttxPower(){
		//TODO
	}
	
	private void sortDistance(){
		//TODO
	}
	
	private void filterAll(List<Network> listNetworks){
		
		map = new LinkedHashMap<String,String>();
		count = 0;
		
		for (Network n : listNetworks){
			map.put(n.getBSSID(), n.toMenuString());
			count++;
		}
		viewing.setText("(Viewing " + count + " networks: All)");
		Log.d("Creating hashmap","done");
		generateView(map);
	}
	private void filterOpen(List<Network> listNetworks){
		
		map = new LinkedHashMap<String,String>();
		count = 0;
			
		for (Network n : listNetworks){
			if (n.getSecurity().contentEquals("")){
				map.put(n.getBSSID(), n.toMenuString());
				count++;
			}
		}
		viewing.setText("(Viewing " + count + " networks: Open)");
		generateView(map);
	}
	
	private void filterVuln(List<Network> listNetworks){
		
		map = new LinkedHashMap<String,String>();
		count = 0;
			
		for (Network n : listNetworks){
			if (n.getSecurity().contains("WEP") || n.getSecurity().contains("WPS")){
				map.put(n.getBSSID(), n.toMenuString());
				count++;
			}
		}
		viewing.setText("(Viewing " + count + " networks: Vulnerable)");
		generateView(map);
	}
	
	private void searchBySSID(String str){
		map = new LinkedHashMap<String,String>();
		count = 0;
			
		for (Network n : listNetworks){
			if (n.getSSID().contains(str)){
				map.put(n.getBSSID(), n.toMenuString());
				count++;
			}
		}
		viewing.setText("(Viewing " + count + " networks: Search results)");
		generateView(map);
	}
	
	private void search(){
		final EditText input = new EditText(this);
		
		 new AlertDialog.Builder(this)
		 .setTitle("Search SSID")
		 .setView(input)
		 .setIcon(R.drawable.icon_search)
		 .setPositiveButton("Go", new DialogInterface.OnClickListener() {
		     public void onClick(DialogInterface dialog, int id) {
		         searchBySSID(input.getText().toString().trim());
		     }
		 })
		 .setNegativeButton("Cancel", null)
		 .show();
	}
	
	private static void copyFile(File src, File dst) throws IOException {
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	        Log.d("Copying Database...", "done");
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
            Log.d("WriteDatabaseToSD: ", "File not found");
		} catch (IOException e) {
			e.printStackTrace();
            Log.d("WriteDatabaseToSD: ", "Failed, IO error");
		}
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}
	
	private void emailDatabase() throws IOException {		
		try {
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				viewing.setText("Write this down!\nSecret Encryption Key: " + DatabaseHandler.encryptDatabase());
				generateView(new LinkedHashMap<String,String>());
				DatabaseHandler.createFilesystem();
				copyFile(WarWalk.db.getEncryptedDatabase(), WarWalk.db.getDatabaseSD());
		
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
				emailIntent.setType("**/**"); 
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "WarWalk networks database"); 
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "**** all the things");
				emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ WarWalk.db.getDatabaseSD().toString()));
				startActivity(emailIntent);
				Syslog.systemLog(0, "Emailing encrypted database");
			}
			else {
				showExternalMediaAlert();
				Syslog.systemLog(2, "Failed to email database");
			}
		} catch (Exception e) {
			e.printStackTrace();
	        Log.d("Composing email: ", "Failed");
		} 
	} 
	
	private void backupDatabase() throws IOException {
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			DatabaseHandler.createFilesystem();
			File src = internal;
			File dst = external;
			try {
				copyFile(src, dst);
				Log.d("backup","done");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dst.exists()) {
				Toast.makeText(this, "Database has been backed up to SDcard", Toast.LENGTH_SHORT).show();
				Syslog.systemLog(0, "Backing up database");
			}
		}
		else {
			showExternalMediaAlert();
			Syslog.systemLog(2, "Failed to backup database");
		}
	}
	
	private void restoreDatabase() throws IOException {
		if (external.exists()) {
			File src = external;
			File dst = internal;
			try {
				copyFile(src, dst);
				Log.d("restore","done");
			} catch (Exception e) {
				e.printStackTrace();
			}
			listNetworks = WarWalk.listNetworks();
		   	filterAll(listNetworks);
			Toast.makeText(this, "Backup has been restored", Toast.LENGTH_SHORT).show();
			Syslog.systemLog(0, "Restoring database from backup");
		}
		else {
			Toast.makeText(this, "Backup does not exist", Toast.LENGTH_SHORT).show();
			Syslog.systemLog(2, "Failed to restore database");
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.dbmenu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        
        switch (item.getItemId())
        {
        case R.id.menu_search:
        	search();
            return true;
        case R.id.menu_view:
        	filterMenu();
            return true;
        case R.id.menu_backup:
        	backupMenu();
            return true;
        case R.id.menu_share:
        	try {
				emailDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}
            return true;
        case R.id.menu_delete:
        	showWipeDatabaseWarning();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	private void backupMenu(){
		CharSequence[] items = new CharSequence[]{"Backup","Restore"};
		
		new AlertDialog.Builder(this)
	    .setTitle("Backup/Restore")
	    .setIcon(R.drawable.icon_bookmark)
	    .setItems(items, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int which) {
	        	   if (which == 0){
	        	   		try {
							backupDatabase();
	        	   		} catch (IOException e) {
							e.printStackTrace();
						}
	        	   }
	        	   else
	        		   if (which == 1){
	        			   try {
							restoreDatabase();
	        			   } catch (IOException e) {
	        				   e.printStackTrace();
	        			   }
	        		   }
	           }
	    })
	    .show();
	}
	
	private void filterMenu() {
		
		final CharSequence[] items = { "All", "Vulnerable", "Open" }; 

		new AlertDialog.Builder(this)
		.setTitle("Filters")
		.setIcon(R.drawable.icon_more)
		.setSingleChoiceItems(items, 0, null)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
                int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                switch (position){
                	case 0:
                		filterAll(listNetworks);
                        break;
                	case 1:
                		filterVuln(listNetworks);
                        break;
                	case 2:
                		filterOpen(listNetworks);
                		break;
                }
               
			}
	 	})
		.show();
	}
	
	private void showDeleteWarning(final int position){
		new AlertDialog.Builder(this)
	     .setTitle("Delete network?")
	     .setIcon(R.drawable.icon_delete)
	     .setMessage("Are you sure you want to delete this network from the database?")
	     .setNegativeButton("Cancel", null)
	     .setPositiveButton("Delete", new AlertDialog.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int which){
	    		 WarWalk.db.deleteNetworkByBSSID(bssid[position]);
	    		 Log.d("Deleting network: ",bssid[position]);
	    		 listNetworks = WarWalk.listNetworks();
	    		 filterAll(listNetworks);
	         }
	    })
	   .show();
	}
	
	private void showWipeDatabaseWarning(){
		new AlertDialog.Builder(this)
	     .setTitle("Wipe database?")
	     .setIcon(R.drawable.icon_delete)
	     .setMessage("Are you sure you want to remove ALL networks from the database?")
	     .setNegativeButton("Cancel", null)
	     .setPositiveButton("Wipe", new AlertDialog.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int which){
	    		 WarWalk.db.deleteAllNetworks();
	    		 Log.d("Wiping database: ", "done");
	    		 listNetworks = WarWalk.listNetworks();
	    		 filterAll(listNetworks);
	         }
	    })
	   .show();
	}
	
	protected void showExternalMediaAlert(){
        new AlertDialog.Builder(this)
        .setTitle("Cannot locate external media")
        .setMessage("SDcard is not available.")
        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            }
        })
        .show();
    }
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		finishActivity(0);
	}

}
