package com.example.memoryleaktest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	private ArrayReferenceAsyncTask mArrayAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		View[] views = new View[]{};
		views[0] = findViewById(R.id.view1);
		mArrayAsyncTask = new ArrayReferenceAsyncTask(views);
		mArrayAsyncTask.execute("");
		mArrayAsyncTask.cancel(true);
		
	}

}
