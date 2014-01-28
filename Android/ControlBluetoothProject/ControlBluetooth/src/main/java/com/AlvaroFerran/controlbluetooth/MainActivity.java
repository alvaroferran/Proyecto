package com.AlvaroFerran.controlbluetooth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ServiceLoader;
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

    private static final String TAG = "bluetooth1"; //solo para log-> borrar

    ToggleButton closeClaw;
    SeekBar servoL1,servoL2,servoL3,servoL4;
    private WebView webView1;
    private String url="http://192.168.1.134:8080/javascript_simple.html";
    private Button Reset, Up, Down,Left, Right,Stop, ButtonURL;
    private Switch leftRight;
    private CheckBox symmetry;

    private int  upState=0, downState=0, leftState=0, rightState=0, stopState=0; //Buttons pressed or not
    public String sendToArduino;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null; //original
    private static String address = "00:12:02:10:00:94"; // MAC-address of Bluetooth module
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /********ON CREATE**************************************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Keep screen on while using the app so webview doesn't stop

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();  //pregunta si encender el bluetooth
        // procesamiento();

        webView1 = (WebView) findViewById(R.id.webView1);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView1.setWebViewClient(new WebViewer());
        webView1.loadUrl(url);

        ButtonURL= (Button) findViewById(R.id.button2);
        ButtonURL.setOnClickListener(new View.OnClickListener(){
            public void onClick(View g)
            {
                Intent activity1 = new Intent(MainActivity.this,SetUrl.class);
                startActivityForResult(activity1,0);
            }
        });

    }


    /********ON RESUME**************************************************************************************/

    @Override
    public void onResume() {
        super.onResume();

        webView1.loadUrl(url);
        procesamiento();


        BluetoothDevice device = btAdapter.getRemoteDevice(address);    //Pointer to BT in Robot

        try {
            btSocket = createBluetoothSocket(device);   //Create Socket to Device
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();    //Discovery consumes resources -> Cancel before connecting

        try {
            btSocket.connect();     //Connect to Robot
        } catch (IOException e) {
            try {
                btSocket.close();   //If unable to connect, close socket
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        try {
            outStream = btSocket.getOutputStream(); //Create output stream
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    /********ON PAUSE***************************************************************************************/

    @Override
    public void onPause() {
        super.onPause();

        if (outStream != null) {
            try {
                outStream.flush();  //If output stream is not empty, send data
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();   //Close socket
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    /********ON ACTIVITY RESULT*****************************************************************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(data!=null){
            String dato=data.getStringExtra("Test");    //Get string from SetUrl.java
            url=dato;
        }
    }

    /********WIDGET MANAGEMENT******************************************************************************/

    private void procesamiento(){

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
        symmetry=(CheckBox)findViewById(R.id.checkBox);
        leftRight=(Switch)findViewById(R.id.switch1);

        servoL1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

            }
            public void onStartTrackingTouch(SeekBar seekBar){ // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar){ReadValuesAndSend();}
        });

        servoL2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

            }
            public void onStartTrackingTouch(SeekBar seekBar) {// TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {ReadValuesAndSend();}
        });

        servoL3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

            }
            public void onStartTrackingTouch(SeekBar seekBar) {// TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {ReadValuesAndSend();}
        });

        servoL4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

            }
            public void onStartTrackingTouch(SeekBar seekBar) {//TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {ReadValuesAndSend();}
        });

        closeClaw.setOnClickListener(new OnClickListener() {
            //@Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ReadValuesAndSend();
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
                ReadValuesAndSend();
            }
        });

        Up.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                upState=1;
                ReadValuesAndSend();
                upState=0;
            }
        });

        Down.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                downState=1;
                ReadValuesAndSend();
                downState=0;
            }
        });

        Left.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                leftState=1;
                ReadValuesAndSend();
                leftState=0;
            }
        });

        Right.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                rightState=1;
                ReadValuesAndSend();
                rightState=0;
            }
        });

        Stop.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                stopState=1;
                ReadValuesAndSend();
                stopState=0;
            }
        });
    }

    /********READ VALUES OF WIDGETS*************************************************************************/

    public void ReadValuesAndSend(){

        String SL1= String.format("%03d", servoL1.getProgress());   //Force seekbar values to be in 3 digit format
        String SL2= String.format("%03d", servoL2.getProgress());
        String SL3= String.format("%03d", servoL3.getProgress());
        String SL4= String.format("%03d", servoL4.getProgress());

        String clawState;
        if (closeClaw.isChecked())
            clawState="1";
        else
            clawState="0";

        String symState;
        if(symmetry.isChecked())
            symState="1";
        else
            symState="0";

        String LRState;
        if(leftRight.isChecked())
            LRState="1";
        else
            LRState="0";

        sendToArduino=SL1+","+SL2+","+SL3+","+SL4+","+clawState+","+symState+","+LRState+","+Integer.toString(upState)+","+Integer.toString(downState)
                +","+Integer.toString(leftState)+","+Integer.toString(rightState)+","+Integer.toString(stopState)+"+"; //'+' as End Of String

        sendData(sendToArduino);    //Message format: "010,100,095,120,1,0,0,1,0,0,0,0+" -> '+' signals end of string

    }

    /********CHECK BT STATE*********************************************************************************/

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

    /********CREATE BT SOCKET*******************************************************************************/

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

    /********SEND DATA**************************************************************************************/

    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            //outStream.write(msgBuffer);

            outStream.write(msgBuffer);
            outStream.flush();

        } catch (IOException e) {
            //String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            String msg= "Phone not connected to client's Bluetooth";
            //if (address.equals("00:00:00:00:00:00"))
            //    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            //msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    /********ERROR EXIT*************************************************************************************/

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }


}
