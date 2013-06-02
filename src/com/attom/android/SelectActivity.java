package com.attom.android;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		Button btnMaster = (Button) findViewById(R.id.btn_master);
		Button btnSlave = (Button) findViewById(R.id.btn_slave);
		
		btnSlave.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent slaveIntent = new Intent(SelectActivity.this, MainActivity.class);
				startActivity(slaveIntent);
			}
		});
		
		btnMaster.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent masterIntent = new Intent(SelectActivity.this, MainActivity.class);
				masterIntent.putExtra("master", true);
				startActivity(masterIntent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select, menu);
		return true;
	}

}
