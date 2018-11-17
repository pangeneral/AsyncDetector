package com.example.repeatstart;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {

	Tasker test = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (test != null) {
			test.execute("");
		}
	}

	@Override
	protected void onDestroy() {

		if (test != null && test.getStatus() == AsyncTask.Status.RUNNING) {
			test.cancel(true);
		}
		super.onDestroy();
	}

}
