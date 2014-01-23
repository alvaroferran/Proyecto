package com.AlvaroFerran.controlbluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by alvaro on 1/23/14.
 */
public class SetUrl extends Activity {

    private Button ButtonOK;
    private EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        url= (EditText) findViewById(R.id.editText);


        ButtonOK= (Button) findViewById(R.id.button);
        ButtonOK.setOnClickListener(new View.OnClickListener(){
            public void onClick(View g)
            {
                Intent activity1=new Intent();
                String mensaje=url.getText().toString().trim();
                activity1.putExtra("Test",mensaje);
                setResult(MainActivity.RESULT_OK, activity1);
                finish();
            }
        });


    }





}
