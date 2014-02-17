package com.AlvaroFerran.controlbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ServiceLoader;
import java.util.UUID;

import com.AlvaroFerran.controlbot.R;



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


    String IP="163.117.90.12";
    int PORT= 3005;
    Socket mysocket;
    PrintWriter out;


    ToggleButton closeClaw;
    SeekBar servoL1,servoL2,servoL3,servoL4;
    private WebView webView1;
    private String url="http://192.168.42.1:8080/javascript_simple.html";
    private Button Reset, Up, Down,Left, Right,Stop, ButtonURL;
    private Switch leftRight;
    private CheckBox symmetry;

    private int  upState=0, downState=0, leftState=0, rightState=0, stopState=0; //Buttons pressed or not
    public String sendToArduino;



    /********ON CREATE**************************************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Keep screen on while using the app so webview doesn't stop

        new Thread(new ClientThread()).start();

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



    }

    /********ON PAUSE***************************************************************************************/

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            out.flush();
            mysocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
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

        //sendData(sendToArduino);    //Message format: "010,100,095,120,1,0,0,1,0,0,0,0+" -> '+' signals end of string
        out.write(sendToArduino);
        out.flush();
    }


    /********ERROR EXIT*************************************************************************************/

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }


    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                mysocket = new Socket(serverAddr, PORT);
                out = new PrintWriter(mysocket.getOutputStream(),true);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }




}
