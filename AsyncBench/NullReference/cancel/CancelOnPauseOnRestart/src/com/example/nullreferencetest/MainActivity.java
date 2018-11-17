package com.example.nullreferencetest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends Activity {
	
	private TextView view1;
	private AsyncTask task1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view1 = (TextView) findViewById(R.id.view1);
       
      
    }
    
    @Override
    protected void onPause() {
    	task1.cancel(true);
    	super.onPause();
    	
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	task1 = new Tasker(view1);
    }
}
