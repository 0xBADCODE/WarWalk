package net.blackhack.warwalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.widget.TextView;

public class Syslog  extends Activity {
	
	private TextView logtext;
	protected static File logFile = new File(Environment.getDataDirectory(), 
			"/data/net.blackhack.warwalk/log.tmp");

	@Override
	protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_syslog);
        logtext = (TextView) this.findViewById(R.id.syslog);
        
        logview();
	}
	
	protected void logview() {
		File log = logFile;

		try {
		    BufferedReader br = new BufferedReader(new FileReader(log));
		    String line;

		    while ((line = br.readLine()) != null) {
		    	logtext.append(Html.fromHtml(line));
		        logtext.append("\n");
		    }
		}
		catch  (Exception e) { 
			e.printStackTrace();
		}
	}
	
	protected static void systemLog(int arg, String str) {
		try {
			if (!logFile.exists())
				logFile.createNewFile();
			
			BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
			if (arg == 0)
				buffer.append("<font color='green'><b>[+]</b></font> " + str);
			else if (arg == 1)
				buffer.append("<font color='white'>[@]</font> " + str);
			else if (arg == 2)
				buffer.append("<font color='red'><b>[!!]</b> WARNING</font> " + str);
			
			buffer.newLine();
			buffer.flush();
			buffer.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}		
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		finishActivity(0);
	}
}
