package com.AlvaroFerran.controlbluetooth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import com.AlvaroFerran.controlbluetooth.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private static final String TAG = "bluetooth1";

    ToggleButton closeClaw;
    SeekBar servoL1,servoL2,servoL3,servoL4;
    private WebView webView1;
    private String url="http://192.168.1.134:8080/javascript_simple.html";
    private Button Reset, Up, Down,Left, Right,Stop, ButtonURL;
    private Switch leftRight;
    private CheckBox symmetry;



    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null; //original
    //private DataOutputStream outStream=null;
    // SPP UUID service

    //private static final UUID MY_UUID = UUID.fromString("0x0000000000001000800000805F9B34FB");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:12:02:10:00:94";
    //private static String address = "00:11:11:29:05:30";

///////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void procesamiento(){

//android:background="#000000"
        servoL1=(SeekBar) findViewById(R.id.seekBar);
        servoL2=(SeekBar) findViewById(R.id.seekBar2);
        servoL3=(SeekBar) findViewById(R.id.seekBar3);
        servoL4=(SeekBar) findViewById(R.id.seekBar4);

        closeClaw=(ToggleButton) findViewById(R.id.toggleButton);
        Reset=(Button) findViewById(R.id.button);
        Up=(Button) findViewById(R.id.buttonU);
        Down=(Button) findViewById(R.id.buttonD);
        Left=(Button) findViewById(R.id.buttonL);
        Right=(Button) findViewById(R.id.buttonR);
        Stop=(Button)findViewById(R.id.buttonS);



        servoL1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                //sendData("SL1"+progressChanged);

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

              //  seekBar.setProgress(50);

            }
        });

        servoL2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                //sendData("L2"+progressChanged);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        servoL3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                //sendData("L3"+progressChanged);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        servoL4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                //sendData("L4"+progressChanged);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });



        closeClaw.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (closeClaw.isChecked()) {

                    Toast.makeText(getBaseContext(), "Closing ", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(getBaseContext(), "Opening ", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Reset.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                servoL1.setProgress(90);
                servoL2.setProgress(90);
                servoL3.setProgress(90);
                servoL4.setProgress(90);
            }
        });



        
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Keep screen on while using the app so webview doesn't stop

        ButtonURL= (Button) findViewById(R.id.button2);
        ButtonURL.setOnClickListener(new View.OnClickListener(){
            public void onClick(View g)
            {
                Intent activity1 = new Intent(MainActivity.this,SetUrl.class);
                startActivityForResult(activity1,0);
            }
        });



        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();  //pregunta si encender el bluetooth


        webView1 = (WebView) findViewById(R.id.webView1);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView1.setWebViewClient(new WebViewer());
        webView1.loadUrl(url);

       // procesamiento();

    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(data!=null){
            String dato=data.getStringExtra("Test");
            url=dato;
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void ReadValuesAndSend(){

      String sendToArduino="Msg";


      double SL1= servoL1.getProgress();
      sendToArduino.concat(Double.toString(SL1));
      double SL2= servoL2.getProgress();
      double SL3= servoL3.getProgress();
      double SL4= servoL4.getProgress();
      int clawState;
      if (closeClaw.isChecked())
          clawState=1;
      else
          clawState=0;
      int symState;
      if(symmetry.isChecked())
          symState=1;
      else
          symState=0;
      int LRState;
      if(leftRight.isChecked())
          LRState=1;
      else
          LRState=0;









    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////

  private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke( MY_UUID,device);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onResume() {
        super.onResume();

        webView1.loadUrl(url);
        procesamiento();


        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        try {
            //outStream = new DataOutputStream(btSocket.getOutputStream());
            outStream = btSocket.getOutputStream();//original
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////

   private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////


   private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            String msg= "Phone not connected to client's Bluetooth";
            //if (address.equals("00:00:00:00:00:00"))
            //    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            //msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }



}


