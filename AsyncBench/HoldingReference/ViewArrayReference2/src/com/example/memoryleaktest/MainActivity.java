package com.example.memoryleaktest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private ArrayReferenceAsyncTask mArrayAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mArrayAsyncTask = new ArrayReferenceAsyncTask(findViewById(R.id.view1));
		mArrayAsyncTask.execute("");
		mArrayAsyncTask.cancel(true);
		
	}

}
