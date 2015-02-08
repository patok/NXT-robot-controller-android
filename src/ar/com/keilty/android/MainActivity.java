package ar.com.keilty.android;

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private final String TAG = this.getClass().getSimpleName();
	private NXTShotRollerController controller;
	private BroadcastReceiver connHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// TODO register NXTShotRollerController as App object
		controller = new NXTShotRollerController();

		connHandler = new BroadcastReceiver() {
	    	@Override 
	    	public void onReceive(Context context,Intent intent) {
	    		if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) {
	    			controller.connect();
	    		}
	    		if (intent.getAction().equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
	    			controller.disconnect();
	    		}		
	    	}
	    };

		Button onButton = (Button) findViewById(R.id.onButton);
		onButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				controller.powerOn();
			}
		});

		
		Button offButton = (Button) findViewById(R.id.offButton);
		offButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					controller.powerOff();
				} catch (IOException e) {
					Log.i(TAG, "Error happened when trying turn off!");
				}
			}
		});
		
		Button upButton = (Button) findViewById(R.id.upButton);
		upButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					controller.aimUp();
				} catch (IOException e) {
					Log.i(TAG, "Error happened when trying to aim up!");
				}
			}
		});

		Button leftButton = (Button) findViewById(R.id.leftButton);
		leftButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					controller.moveLeft();
				} catch (IOException e) {
					Log.i(TAG, "Error happened when trying to move left!");
				}
			}
		});
		
		Button rightButton = (Button) findViewById(R.id.rightButton);
		rightButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					controller.moveRight();
				} catch (IOException e) {
					Log.i(TAG, "Error happened when trying to move right!");
				}
			}
		});

		Button fireButton = (Button) findViewById(R.id.fireButton);
		fireButton.setOnLongClickListener( new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					controller.fire();
					return true;
				} catch (IOException e) {
					Log.i(TAG, "Error happened when trying to move left!");
				}
				return false;
			}
		});

		Button downButton = (Button) findViewById(R.id.downButton);
		downButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					controller.aimDown();
				} catch (IOException e) {
					Log.i(TAG, "Error happened when trying to aim down!");
				}
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		registerReceiver(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		unregisterReceiver(this);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy ...");
		try {
			controller.powerOff();
		} catch (IOException e) {
			// intenttionally left blank
		}
	}

	private void registerReceiver(Context context) {
		context.registerReceiver(connHandler, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
		context.registerReceiver(connHandler, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
	}

	private void unregisterReceiver(Context context) {
		context.unregisterReceiver(connHandler);
	}

}
