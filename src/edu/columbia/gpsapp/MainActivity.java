package edu.columbia.gpsapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MainActivity extends Activity implements OnItemSelectedListener {
  final Context context = this;
  private GPSTracker gpsTracker;
  private TextView textView;
  private Spinner spinner;
  private long spinnerId = -1;
  private long interval = 5;
  AlertDialog.Builder builder;
  private final int periodicDialogId = 100;
  private ScheduledThreadPoolExecutor exec;

  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
      // An item was selected. You can retrieve the selected item using
      // parent.getItemAtPosition(pos)
      Spinner spinner = (Spinner) parent;
      if(spinner.getId() == R.id.spinner1)
      {
          Log.e("JIKK:", "onItemSelected: " + pos + ":" + id + ": spinner1:" +
                  R.id.spinner1 + ":" + spinner.getId());

          spinnerId = pos;
      }
      else
      {
        //do this
      }
  }

  public void onNothingSelected(AdapterView<?> parent) {
      // Another interface callback
  }

  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		spinner = (Spinner) findViewById(R.id.spinner1);
		spinner.setOnItemSelectedListener(this);
		gpsTracker = new GPSTracker(this);
		exec = new ScheduledThreadPoolExecutor(1);
		builder = new AlertDialog.Builder(context);
		builder.setTitle("Test");
		builder.setMessage("HI");
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

			}
		});
		
		builder.setNegativeButton("Stop", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				exec.shutdown();
				dialog.cancel();

			}
		});
		exec.scheduleWithFixedDelay (new Runnable() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						AlertDialog alertDialog =builder.create();
						alertDialog.show();
						
					}
				});
				
			}
		}, 0, interval, TimeUnit.SECONDS);
	}

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public void sendMessage(View view) {
    Log.e("JIKK:", "sendMessage");
    gpsTracker.getLocation();
    double latitude = gpsTracker.getLatitude();
    double longitude = gpsTracker.getLongitude();
    String deviceID, country, mobileNetworkCode, countryID, mobileNetworkCodeID, msin, city;

    EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
    EditText portTxt = (EditText) findViewById(R.id.port);
    textView = (TextView) findViewById(R.id.textView1);

    if (spinnerId <0) {
    } else {
        String msg = String.valueOf(spinner.getSelectedItem());

        if (spinnerId == 0) {  // Getting Location
        	if (latitude == 37.33 && longitude == 126.58) {
        		country = "South Korea";
        		city = "Seoul";
        	} else if (latitude == 40.42 && longitude == 74.0) {
        		country = "USA";
        		city = "New York";
        	} else if (latitude == 38.54 && longitude == 77.2) {
        		country = "USA";
        		city = "Washington";
        	} else if (latitude == 37.59 && longitude == 23.43) {
        		country = "Greece";
        		city = "Athens";
        	} else if (latitude == 40.25 && longitude == 3.42) {
        		country = "Spain";
        		city = "Madrid";
        	} else if (latitude == 51.30 && longitude == 0.7) {
        		country = "Great Britain";
        		city = "London";
        	} else if (latitude == 48.51 && longitude == 2.21) {
        		country = "France";
        		city = "Paris";
        	} else if (latitude == 39.54 && longitude == 116.24) {
        		country = "China";
        		city = "Beijing";
        	} else if (latitude == 41.53 && longitude == 12.28) {
        		country = "Italy";
        		city = "Rome";
        	} else if (latitude == 35.41 && longitude == 139.41) {
        		country = "Japan";
        		city = "Tokyo";
        	} else {
        		country = "Unknown";
        		city = "Unknown";
        	}
        	
        	msg += ": Country: " + country + " city: " + city + " ->	 " + Double.toString(latitude) + "," + Double.toString(longitude); 
        	//msg += ":" + Double.toString(latitude) + "," + Double.toString(longitude);

        } else if (spinnerId == 1) {
        	deviceID = findDeviceID();
        	countryID = deviceID.substring(0, 2);
        	mobileNetworkCodeID = deviceID.substring(3, 5);
        	msin = deviceID.substring(6);
        	if (countryID.compareTo("450") == 0) {
        		country = "South Korea";
        		if (mobileNetworkCodeID.compareTo("002") == 0)
        			mobileNetworkCode = "KT";
        		else if (mobileNetworkCodeID.compareTo("003") == 0)
        			mobileNetworkCode = "Power 017";
        		else
        			mobileNetworkCode = "Unknown";
        	} else if (deviceID.substring(0, 2).compareTo("234") == 0) {
        		country = "Great Britain";
        		if (mobileNetworkCodeID.compareTo("000") == 0)
        			mobileNetworkCode = "BT";
        		else if (mobileNetworkCodeID.compareTo("001") == 0)
        			mobileNetworkCode = "Vectone Mobile";
        		else
        			mobileNetworkCode = "Unknown";
        	} else if (deviceID.substring(0, 2).compareTo("310") == 0) {
        		country = "USA";
        		if (mobileNetworkCodeID.compareTo("053") == 0)
        			mobileNetworkCode = "Virgin Mobile";
        		else if (mobileNetworkCodeID.compareTo("054") == 0)
        			mobileNetworkCode = "Alltel US";
        		else
        			mobileNetworkCode = "Unknown";
        	} else {
        		country = "Unknown";
        		mobileNetworkCode = "Unknown";
        	}
        	msg += ": Country: " + country + " Mobile Network Code: " + mobileNetworkCode + " -> " + deviceID;
        		
//            msg += ":" + findDeviceID();
        }
        SocketTask task = new SocketTask(this, ipAddrTxt.getText().toString(),
                portTxt.getText().toString());
        task.execute(new String[] {msg});
    }
  }

  private String findDeviceID() {
    String deviceID = null;

    TelephonyManager m_telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
    int deviceType = m_telephonyManager.getPhoneType();

    switch (deviceType) {
        case (TelephonyManager.PHONE_TYPE_GSM):
          Log.e("JIKK:", "TelephonyManager.PHONE_TYPE_GSM");
          break;
        case (TelephonyManager.PHONE_TYPE_CDMA):
          Log.e("JIKK:", "TelephonyManager.PHONE_TYPE_CDMA");
          break;
        case (TelephonyManager.PHONE_TYPE_NONE):
          Log.e("JIKK:", "TelephonyManager.PHONE_TYPE_NONE");
          break;
        default:
          Log.e("JIKK:", "TelephonyManager.default");
          break;
      }

    deviceID = m_telephonyManager.getSubscriberId();
    Log.e("JIKK:", "deviceID:" + deviceID);
    return deviceID;
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

    	Intent i = new Intent(MainActivity.this, ResultActivity.class);
    	i.putExtra("result", result);
    	startActivity(i);
      //textView.setText(result);
    }
  }
}
