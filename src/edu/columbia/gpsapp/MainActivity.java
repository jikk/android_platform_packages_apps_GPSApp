package edu.columbia.gpsapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private GPSTracker gpsTracker;
	private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gpsTracker = new GPSTracker(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    public void sendMessage(View view) {        
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        
        EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
        EditText portTxt = (EditText) findViewById(R.id.port);
        textView = (TextView) findViewById(R.id.textView1);
        
        String msg = Double.toString(latitude) + "," + Double.toString(longitude);
        
		SocketTask task = new SocketTask(this, 	ipAddrTxt.getText().toString(), portTxt.getText().toString());
		task.execute(new String[] {msg});
    }
    
    private class SocketTask extends AsyncTask<String, Void, String> {
    	private Activity act;
    	private String dstName;
    	private int port;
    	
    	public SocketTask(Activity act_, String dstName_, String port_) {
    		super();
    		act = act_;
    		dstName = dstName_;
    		try {
    			port = Integer.parseInt(port_);
    		} catch (Exception e) {
    			Toast.makeText(act, e.toString(), Toast.LENGTH_LONG).show();
    		}
    	}
    	
    	protected String doInBackground(String... msgLst) {
    		String ret = "";    		
    		for (String msg: msgLst) {
	    		try {
	    			Socket client = new Socket(dstName, port);    			
	
	    			OutputStream outToServer = client.getOutputStream();
	    			DataOutputStream out = new DataOutputStream(outToServer);        	        	
	    			InputStream inFromServer = client.getInputStream();
	    			BufferedReader in = new BufferedReader(new InputStreamReader(inFromServer));
	            
	    			out.writeUTF(msg);
	    			String response;
	    			while((response = in.readLine()) != null) {
	    				ret += response;
	    			}
	    			
	    			client.close();
	    		} catch (IOException e) {
	    			return e.toString();
	    		}
    		}  	
    		
    		return ret;
    	}    	
		@Override
	    protected void onPostExecute(String result) {
	        textView.setText(result);
	    }    	
    }
}
