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
import android.content.Context;
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
  private GPSTracker gpsTracker;
  private TextView textView;
  private Spinner spinner;
  private long spinnerId = -1;

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

    EditText ipAddrTxt = (EditText) findViewById(R.id.addr);
    EditText portTxt = (EditText) findViewById(R.id.port);
    textView = (TextView) findViewById(R.id.textView1);

    if (spinnerId <0) {
    } else {
        String msg = String.valueOf(spinner.getSelectedItem());

        if (spinnerId == 0) {  // Getting Location
            msg += ":" + Double.toString(latitude) + "," + Double.toString(longitude);

        } else if (spinnerId == 1) {
            msg += ":" + findDeviceID();
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
      textView.setText(result);
    }
  }
}
