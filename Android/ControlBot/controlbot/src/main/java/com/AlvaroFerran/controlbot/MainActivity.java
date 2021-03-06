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


import android.support.v7.app.ActionBar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

    //String IP="163.117.90.12";
    String IP="192.168.42.1"; //Given to Raspberry Pi automatically when it turns on
    //String IP="192.168.1.132";
    int PORT= 3005;
    Socket mysocket;
    PrintWriter out;


    ToggleButton closeClaw;
    SeekBar servoL1,servoL2,servoL3,servoL4;
    private int progress1=90, progress2=90, progress3=90, progress4=90;
    private WebView webView1;
    private String url="http://192.168.42.1:8080/javascript_simple.html";
    private Button Reset, Up, Down,Left, Right,Stop, ButtonURL;
    private Switch leftRight;
    private CheckBox symmetry;

    private int  upState=0, downState=0, leftState=0, rightState=0, stopState=0; //Buttons pressed or not
    public String sendToArduino;
    public boolean connected=false;
    public TextView text;


    /********ON CREATE**************************************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(new ClientThread()).start();

        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.textView);
     /*  if(connected==true)
           text.setText("connected");
       else
           text.setText("nope");
        //  setContentView(R.layout.activity_init_screen);
*/

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Keep screen on while using the app so webview doesn't stop

/*Put webView1 in onResume to see if it works better*/
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        //return super.onCreateOptionsMenu(menu);
        return true;
    }


    /********ON RESUME**************************************************************************************/


/*try: public void onStart(){*/ //or not-> after quitting it passes through resume again
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

    /********ON STOP****************************************************************************************/

    @Override
    public void onStop() {
        super.onStop();
        try {
            out.write("quit");
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
                progress1=progress;
                ReadValuesAndSend();
            }
            public void onStartTrackingTouch(SeekBar seekBar){ // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar){}
        });

        servoL2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progress2=progress;
                ReadValuesAndSend();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {// TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        servoL3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progress3=progress;
                ReadValuesAndSend();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {// TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        servoL4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progress4=progress;
                ReadValuesAndSend();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {//TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
                progress1=90;
                progress2=90;
                progress3=90;
                progress4=90;
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

        //String SL1= String.format("%03d", servoL1.getProgress());   //Force seekbar values to be in 3 digit format
        String SL1= String.format("%03d", progress1);
        String SL2= String.format("%03d", progress2);
        String SL3= String.format("%03d", progress3);
        String SL4= String.format("%03d", progress4);

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


    /********SOCKET CLIENT THREAD***************************************************************************/

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                mysocket = new Socket(serverAddr, PORT);

                out = new PrintWriter(mysocket.getOutputStream(),true);
               // if(mysocket.isBound())
                 //   connected=true;
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }




}
