package com.example.memoryleaktest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private SetReferenceAsyncTask mSetAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSetAsyncTask = new SetReferenceAsyncTask(findViewById(R.id.view1));
		mSetAsyncTask.execute("");
		mSetAsyncTask.cancel(true);
		
	}

}
