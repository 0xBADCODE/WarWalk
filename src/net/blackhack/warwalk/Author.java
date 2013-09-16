package net.blackhack.warwalk;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Author extends Activity{
	
	private TextView author_info;
	private TextView db_info;
	protected Button statsbutton;
	private String BTCaddress = "1BBo4XCpXFVar1SHbpUf1atb3Sck3iLfQY";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_author);
        
        author_info = (TextView) this.findViewById(R.id.author_info);
        db_info = (TextView) this.findViewById(R.id.db_info);
        statsbutton = (Button) this.findViewById(R.id.statsbutton);
       
        writeInfo();
	}
	
	private void writeInfo(){
			author_info.append("Software version 1.0b"
								+ "\n\nAuthored by Xeon 2012"
								+ "\nVisit www.blackhack.net"
								+ "\n\nPlease use this software responsibly."
								+ "\n\nIf you enjoy this software please donate to help fund other projects." 
								+ "\n\nBTC: " + BTCaddress);
		
		author_info.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View arg0, MotionEvent arg1) {
				ClipboardManager clipboard = (ClipboardManager)
				        getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(BTCaddress);
				Toast.makeText(getApplicationContext(), "BTC address copied to clipboard", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}
	
	public void writeStatistics(View view){
		if (view.getId() == R.id.statsbutton){
			statsbutton.setVisibility(View.GONE);
			
			List<Network> networkList = WarWalk.listNetworks();
			Network net = null;
			int 	openCount = 0,
					weakCount = 0,
					secureCount = 0;
			
			db_info.setText("Some statistics...\n\n" 
								+ Long.toString(networkList.size()) + " networks in database."
								+ "\n\nDatabase filepath: " + getDatabasePath(WarWalk.db.getDatabaseName()).getPath());
		
			for (Network n : networkList){
			
				net = n;
				if (net.getSecurity().contentEquals(""))
						openCount++;
				else
					if (net.getSecurity().contains("WEP") || net.getSecurity().contains("WPS"))
						weakCount++;
				else
					if (net.getSecurity().contains("WPA"))
						secureCount++;
			}
			db_info.append("\n\nFound " + secureCount + " secure networks.");
			db_info.append("\n\nFound " + weakCount + " vulnerable networks.");
			db_info.append("\n\nFound " + openCount + " open networks.");
			
			if (WarWalk.geolocate.canGetLocation){
				db_info.append("\n\nCurrent GPS coords. " + WarWalk.geolocate.getStrLatitude()
										+ " " + WarWalk.geolocate.getStrLongitude());
				db_info.append("\n\nCurrently accurate to within " + Float.toString(WarWalk.geolocate.getAccuracy()) + " metres.");
			}
		}
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		finishActivity(0);
	}
}