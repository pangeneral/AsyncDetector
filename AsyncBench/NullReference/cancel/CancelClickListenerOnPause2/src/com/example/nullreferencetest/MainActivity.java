package com.example.nullreferencetest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class MainActivity extends Activity {
	
	private TextView view1;
	private AsyncTask task1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view1 = (TextView) findViewById(R.id.view1);
        view1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				if(!task1.cancel(true)){
					 task1.cancel(true);
				}
				
			}
		});
        
    }
    
    @Override
    protected void onPause() {
    	task1 = new Tasker(view1);
    	super.onPause();
    }
}
