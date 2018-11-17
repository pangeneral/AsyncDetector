package com.example.memoryleaktest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private MapReferenceAsyncTask mMapAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mMapAsyncTask = new MapReferenceAsyncTask(findViewById(R.id.view1));
		mMapAsyncTask.execute("");
		mMapAsyncTask.cancel(true);

		
	}

}
