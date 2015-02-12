package edu.columbia.gpsapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
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
  private long interval = 5;
  private ScheduledThreadPoolExecutor exec;
  private boolean dialogBeingShown = false;
  private SecureRandom rand;

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
        rand = new SecureRandom();
        final int threads = rand.nextInt(4);

        //recursive thread, always spawned.
        //only the first branch choice (spinnerId) should
        //remain after multiple iterations
		exec.scheduleWithFixedDelay (new Runnable(){
			@Override
			public void run() {
				if (spinnerId == -1 || spinnerId == 0) {
					String txt = null;
					int file0;
					String filename = null;
					file0 = rand.nextInt(2);
					if (file0 != 0) {
						//write to uifile2
						filename = "/data/local/tmp/thread1_1";
					} else {
						//write to uifile3
						filename = "/data/local/tmp/thread1_0";
					}
					try {
						txt = new String("Threads: " + threads);
						FileOutputStream fio = new FileOutputStream(filename);
						fio.write(txt.getBytes());
						fio.close();
					} catch (IOException io) {
						//do nothing
					}
				} else {
					//never taken
				}
				// TODO Auto-generated method stub

			}
		}, 1, interval, TimeUnit.SECONDS);
		//one time thread if executed, should be eliminated as a whole
        if (threads >= 1) {
            Runnable r1 = new Runnable() {
                public void run() {
                    //thread 2
                    gpsTracker.getLocation();
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();
                    int file1 = -1;
                    String filename = "";

                    String txt = Double.toString(latitude) + ":" + Double.toString(longitude);
                    if (spinnerId == -1 || spinnerId == 0) {
                        file1 = rand.nextInt(2);
                        if (file1 != 0) {
                        	filename = "/data/local/tmp/thread2_1";
                        } else {
                        	filename = "/data/local/tmp/thread2_0";
                        }
                        try {
                        	FileOutputStream fio = new FileOutputStream(filename);
                        	fio.write(txt.getBytes());
                        	fio.close();
                        } catch (IOException io) {
                        	//do nothing
                        }
                    } else {
                        //never taken
                    }
                }
            };
            new Thread(r1).start();
        }
        //thread that if spawned is recursive, should be eliminated as a whole
        if (threads >= 2) {
        	exec.scheduleWithFixedDelay (new Runnable(){
    			@Override
    			public void run() {
    				if (spinnerId == -1 || spinnerId == 0) {
    					String txt = null;
    					int file0;
    					String filename = null;
    					file0 = rand.nextInt(2);
    					if (file0 != 0) {
    						//write to uifile2
    						filename = "/data/local/tmp/thread3_1";
    					} else {
    						//write to uifile3
    						filename = "/data/local/tmp/thread3_0";
    					}
    					try {
    						txt = new String("Threads: " + threads);
    						FileOutputStream fio = new FileOutputStream(filename);
    						fio.write(txt.getBytes());
    						fio.close();
    					} catch (IOException io) {
    						//do nothing
    					}
    				} else {
    					//never taken
    				}
    				// TODO Auto-generated method stub

    			}
    		}, rand.nextInt(10), interval, TimeUnit.SECONDS);
        }
        //thread that always does the same thing, but is not always spawned
        //should be eliminated as a whole
        if (threads == 3) {
        	exec.scheduleWithFixedDelay (new Runnable(){
        		@Override
        		public void run() {
        			String filename = null;
        			String txt = null;
        			if (spinnerId == -1 || spinnerId == 0) {

        				filename = "/data/local/tmp/thread4_1";

        				try {
        					txt = new String("Threads: " + threads);
        					FileOutputStream fio = new FileOutputStream(filename);
        					fio.write(txt.getBytes());
        					fio.close();
        				} catch (IOException io) {
        					//do nothing
        				}
        			} else {
        				//never taken
        			}
        			// TODO Auto-generated method stub

        		}
        	}, rand.nextInt(10), interval, TimeUnit.SECONDS);
        }
	}

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  //called when pressing button. Only first and last branch choice should survive
  public void sendMessage(View view) {
    EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
    EditText portTxt = (EditText) findViewById(R.id.port);
    String txt = null;
    String filename = null;
    int file2 = -1;
    
    if (spinnerId < 0) {
        //Error processing needed from here.
    } else {
    	if (spinnerId == 0) {  // Getting Location
    		gpsTracker.getLocation();
    		double latitude = gpsTracker.getLatitude();
    		double longitude = gpsTracker.getLongitude();
    		
    		txt = Double.toString(latitude) + ":" + Double.toString(longitude);
    		//msg = new Msg(latitude, longitude);


    		file2 = rand.nextInt(2);
    		if (file2 != 0)
    			filename = "/data/local/tmp/thread0_1";
    		else
    			filename = "/data/local/tmp/thread0_0";
    		try {
    			FileOutputStream fio = new FileOutputStream(filename);
    			fio.write(txt.getBytes());
    			fio.close();
    		} catch (IOException e) {
    		}
    	} else {
    		//do nothing
    	}
    	if (file2 < 2) {
    		SocketTask task = new SocketTask(this, ipAddrTxt.getText().toString(),
                portTxt.getText().toString());

    		//task.execute(new String[] {txt, "DATE: " + msg.getDateFormat(), "MSG: " + msg.toString()});
    		task.execute(txt);
    	}
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
