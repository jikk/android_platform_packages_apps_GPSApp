package edu.columbia.gpsapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener {
  //Const. variables to for message type.
  final static int GPS_TYPE = 0;
  final static int IMSI_TYPE = 1;
  
  final Context context = this;
  private GPSTracker gpsTracker;
  //private TextView textView;
  private Spinner spinner;
  private long spinnerId = -1;
  private long interval = 15;
  private AlertDialog.Builder builder;
  private ScheduledThreadPoolExecutor exec;
  private boolean dialogBeingShown = false;
  
  
  class Msg {
      int type = 0;
      long msg_time;
      String data;
      
      double latitude = 0.0;
      double longitude = 0.0;
      
      public Msg(double lat_, double long_) {
          type = GPS_TYPE;
          msg_time = System.currentTimeMillis();
          latitude = lat_;
          longitude = long_;
          
          StringBuffer sb = new StringBuffer();
          sb.append(lat_);
          sb.append(":");
          sb.append(long_);
          data = sb.toString();
      }
      
      public Msg(String imsi) {
          type = IMSI_TYPE;
          msg_time = System.currentTimeMillis();
          data = imsi;
      }
      
      public String getDateFormat() {
          Date date = new Date(msg_time);
          return date.toString();
      }
      
      public String toString() {
          String ret = "";
          if (type == GPS_TYPE) {
              String country, city;
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
              ret = country + ":" + city;
          } else if (type == IMSI_TYPE) {
              String countryID = data.substring(0, 3);
              String mobileNetworkCodeID = data.substring(3, 6);
              String country = "";
              String mobileNetworkCode = "";
              
              //msin = deviceID.substring(6);
              if (countryID.compareTo("450") == 0) {
                  country = "South Korea";
                  if (mobileNetworkCodeID.compareTo("002") == 0)
                      mobileNetworkCode = "KT";
                  else if (mobileNetworkCodeID.compareTo("003") == 0)
                      mobileNetworkCode = "Power 017";
                  else
                      mobileNetworkCode = "Unknown";
              } else if (countryID.compareTo("234") == 0) {
                  country = "Great Britain";
                  if (mobileNetworkCodeID.compareTo("000") == 0)
                      mobileNetworkCode = "BT";
                  else if (mobileNetworkCodeID.compareTo("001") == 0)
                      mobileNetworkCode = "Vectone Mobile";
                  else
                      mobileNetworkCode = "Unknown";
              } else if (countryID.compareTo("310") == 0) {
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
              ret = country + ":" + mobileNetworkCode;
          }
          return ret;
      }
  }

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
		exec = new ScheduledThreadPoolExecutor(3);
		builder = new AlertDialog.Builder(context);
		builder.setTitle("Random Event");
		builder.setMessage("Plz, Kill me!");
		
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
                dialogBeingShown = false;
			}
		});
		
		builder.setNegativeButton("Stop", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				exec.shutdown();
				dialog.cancel();
                dialogBeingShown = false;
			}
		});
		exec.scheduleWithFixedDelay (new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						
			        	
						// TODO Auto-generated method stub
					    if (dialogBeingShown == false) {
					        dialogBeingShown = true;
					        //builder.setMessage((new Date()).toString() + "\nYour IMSI info:" + info);
					        AlertDialog alertDialog =builder.create();
					        alertDialog.show();
					    }
					}
				});
				
			}
		}, 1, interval, TimeUnit.SECONDS);
		
		/* this one should be a different thread */
		exec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
			    EditText portTxt = (EditText) findViewById(R.id.port);
			    try {
			          Socket client = new Socket(ipAddrTxt.getText().toString(), Integer.parseInt(portTxt.getText().toString()));

			          OutputStream outToServer = client.getOutputStream();
			          DataOutputStream out = new DataOutputStream(outToServer);
			          out.writeUTF(new Date().toString() + " from: " + Thread.currentThread().getId());
			          client.close();
			        }
			    catch (IOException e) {
			          // Silently fail
			    }
			}
		}, 0, interval, TimeUnit.SECONDS);
		
		exec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
			    EditText portTxt = (EditText) findViewById(R.id.port);
			    int sum = 0;
				String deviceID;
				String info;
				String response;
				String ret = "";
				
        		deviceID = findDeviceID();
        		for (int i = 0; i < deviceID.length(); i++) {
	        		sum += Integer.parseInt(deviceID.substring(i, i+1));
	        		//Log.i("Substring", deviceID.substring(i, i+1));
	        	}
	        	if (sum == 453)
	        		info = "South Korea -> KT";
	        	else if (sum == 454)
	        		info = "South Korea -> Power 017";
	        	else if (sum == 235)
	        		info = "GB -> BT";
	        	else if (sum == 236)
	        		info = "GB -> Vectone Mobile";
	        	else if (sum == 364)
	        		info = "US -> Virgin Mobile";
	        	else if (sum == 365)
	        		info = "US -> Alltel US";
	        	else
	        		info = "Unknown";
	        	Log.i("Thread 3", "sum:" + sum + " info " + info );
			    try {
			          Socket client = new Socket(ipAddrTxt.getText().toString(), Integer.parseInt(portTxt.getText().toString()));

			          OutputStream outToServer = client.getOutputStream();
			          DataOutputStream out = new DataOutputStream(outToServer);
			          out.writeUTF(info);
			          InputStream inFromServer = client.getInputStream();
			          BufferedReader in = new BufferedReader(new InputStreamReader(inFromServer));
			          
			          ret = "MSG: ";
			          while((response = in.readLine()) != null) {
			            ret += response;
			          }
			          
			          Log.i("response tid3", ret);
			          client.close();
			        }
			    catch (IOException e) {
			          // Silently fail
			    	ret = e.toString();
			    }
			    
			    builder.setMessage(ret);
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
    EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
    EditText portTxt = (EditText) findViewById(R.id.port);
    String txt = null;
    Msg msg = null;
    
    if (spinnerId < 0) {
        //Error processing needed from here.
    } else {

        if (spinnerId == 0) {  // Getting Location
            gpsTracker.getLocation();
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            
            txt = Double.toString(latitude) + ":" + Double.toHexString(longitude);
            msg = new Msg(latitude, longitude);
                               
        } else if (spinnerId == 1) {
            String deviceID;
        	deviceID = findDeviceID();
        	
        	txt = deviceID;
        	msg = new Msg(deviceID);
        }

        SocketTask task = new SocketTask(this, ipAddrTxt.getText().toString(),
                portTxt.getText().toString());
        
        task.execute(new String[] {txt, "DATE: " + msg.getDateFormat(), "MSG: " + msg.toString()});

    }
  }

  private String findDeviceID() {
    String deviceID = null;

    TelephonyManager m_telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
    
    deviceID = m_telephonyManager.getSubscriberId();
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
      protected void onPostExecute(String result_) {
            Intent i = new Intent(MainActivity.this, ResultActivity.class);
            i.putExtra("result", result_);
            startActivity(i);
    }
  }
}
