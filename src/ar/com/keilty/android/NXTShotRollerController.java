package ar.com.keilty.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class NXTShotRollerController {

	// TODO move to SharedPreferences
	private static final String DEVICE_NAME = "NXT";
	// paired movement constants & elapsed times
	private static final int ROTATION_SPEED_RIGHT = 45;
	private static final int ROTATION_SPEED_LEFT = -1 * ROTATION_SPEED_RIGHT;
	private static final long ROTATION_ELAPSED_TIME_MS = 350;
	private static final int AIM_SPEED_DOWN = 40;
	private static final int AIM_SPEED_UP = -1 * AIM_SPEED_DOWN;
	private static final long AIM_ELAPSED_TIME_MS = 300;
	private static final int FIRE_SPEED = 90;
	private static final long FIRE_ELAPSED_TIME_MS = 500;
	
	private NXT_BTCommandHandler btHandler = new NXT_BTCommandHandler();
	
	public boolean powerOn() {
		return btHandler.linkToDevice(DEVICE_NAME);
	}
	
	public void powerOff() throws IOException {
		btHandler.unlink();
	}

	public void connect() {
		btHandler.connect();
	}

	public void disconnect() {
		btHandler.disconnect();
	}

	public void moveRight() throws IOException {
		if (btHandler.isConnected())
			btHandler.moveMotorForward(btHandler.MOTOR_B, ROTATION_SPEED_RIGHT, ROTATION_ELAPSED_TIME_MS);
	}
	
	public void moveLeft() throws IOException {
		if (btHandler.isConnected())
			btHandler.moveMotorForward(btHandler.MOTOR_B, ROTATION_SPEED_LEFT, ROTATION_ELAPSED_TIME_MS);
	}
	
	public void aimUp() throws IOException {
		if (btHandler.isConnected())
			btHandler.moveMotorForward(btHandler.MOTOR_A, AIM_SPEED_DOWN, AIM_ELAPSED_TIME_MS);
	}
	
	public void aimDown() throws IOException {
		if (btHandler.isConnected())
			btHandler.moveMotorForward(btHandler.MOTOR_A, AIM_SPEED_UP, AIM_ELAPSED_TIME_MS);
	}
	
	public void fire() throws IOException {
		if (btHandler.isConnected())
			btHandler.moveMotorForward(btHandler.MOTOR_C, FIRE_SPEED, FIRE_ELAPSED_TIME_MS);
	}

}

class NXT_BTCommandHandler {

	// aiming up/down
	public final int MOTOR_A = 0;
	// for turning left/right
	public final int MOTOR_B = 1;
	// firing
	public final int MOTOR_C = 2;
	private static final int ZERO = 0;
	private final String TAG = this.getClass().getSimpleName();

	private BluetoothSocket channel;
	private InputStream input;
	private OutputStream output;
	private boolean isConnected;
	
	boolean linkToDevice(String deviceName) {
		try {
			BluetoothAdapter btInterface = BluetoothAdapter.getDefaultAdapter();
			Log.i(TAG, "Local BT Interface name is [" + btInterface.getName() + "]");
			Set<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();
			Log.i(TAG, "Found [" + pairedDevices.size() + "] devices.");
			Iterator<BluetoothDevice> it = pairedDevices.iterator();
			while (it.hasNext()) {
				BluetoothDevice bd = it.next();
				Log.i(TAG, "Name of peer is [" + bd.getName() + "]");
				if (bd.getName().equalsIgnoreCase(deviceName)) {
					Log.i(TAG, "Found Robot!");
					Log.i(TAG, bd.getAddress());
					Log.i(TAG, bd.getBluetoothClass().toString());
					connectToThisDevice(bd);
					return true;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed trying to connect to robot " + e.getMessage());
		}
		return false;
	}

	private void connectToThisDevice(BluetoothDevice bd) throws IOException {
		try {
			int state = bd.getBondState();
			if (BluetoothDevice.BOND_BONDED == state &&
					channel != null) {
				channel.close();
				Log.d(TAG, "Closing socket conn w/ device");
			}
			Log.d(TAG, "Binding to BT device" + bd.getName());
			channel = bd.createRfcommSocketToServiceRecord(java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			channel.connect();
		} catch (Exception e) {
			Log.e(TAG, "Error interacting with remote device [" + e.getMessage() + "]");
			throw e;
		}
	}

	void unlink() throws IOException {
		try {
			Log.i(TAG, "Attempting to break BT connection");
			if (channel != null) {
				channel.close();
				channel = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in disconnect [" + e.getMessage() + "]");
			throw e;
		}
	}

    void connect() {
		try {
			input = channel.getInputStream();
			output = channel.getOutputStream();
			isConnected = true;
		} catch (Exception e) {
			input = null;
			output = null;
			try {
				unlink();
			} catch (IOException e1) {
				// intentionally left blank
			}
		}
		// TODO stop motors
		//movementMask = MOTOR_B_STOP + MOTOR_C_STOP;
		//updateMotors();
    }
    
    void disconnect() {
    	if (input != null) {
    		try {
				input.close();
				input = null;
			} catch (IOException e) {
				// intentionally left blank
			}
    	}
    	if ( output != null) {
    		try {
				output.close();
				output = null;
			} catch (IOException e) {
				// intentionally left blank
			}
    	}
		isConnected = false;
    }
    
	public boolean isConnected() {
		return isConnected;
	}

	void moveMotorForward(int motorId, int speed, long durationMillis) throws IOException {
		activateMotor(motorId, speed);
		try {
			Thread.sleep(durationMillis);
		} catch (InterruptedException e) {
			// intentionally left blank
		}
		activateMotor(motorId, ZERO);
	}
	

	/*
	 * Copypaste notice: The following code has been copy-pasted from 
	 * 
	 * UA2E_SenseBot application
	 * written for Unlocking Android, Second Edition
	 * http://manning.com/ableson2
	 * Author: Frank Ableson
	 */
	private void activateMotor(int motor, int speed) {
		try {
			Log.i(TAG, "activate Motor");
			Log.i(TAG, "Attempting to move [" + motor + "[]" + speed + "]");

			byte[] buffer = new byte[14];

			buffer[0] = (byte) (14 - 2); // length lsb
			buffer[1] = 0; // length msb
			buffer[2] = 0; // direct command (with response)
			buffer[3] = 0x04; // set output state
			buffer[4] = (byte) motor; // output 1 (motor B)
			buffer[5] = (byte) speed; // power
			buffer[6] = 1 + 2; // motor on + brake between PWM
			buffer[7] = 0; // regulation
			buffer[8] = 0; // turn ration??
			buffer[9] = 0x20; // run state
			buffer[10] = 0;
			buffer[11] = 0;
			buffer[12] = 0;
			buffer[13] = 0;

			output.write(buffer);
			output.flush();
			byte response[] = readResponse(4);
			if (response == null) {
				Log.e(TAG, "No response??");
			} else {
				for (int i = 0; i < response.length; i++) {
					Log.i(TAG, "Byte[" + i + "][" + response[i] + "]");
				}
			}

		} catch (Exception e) {
			Log.e(TAG, "Error in MoveForward(" + e.getMessage() + ")");
		}
	}

	private byte[] readResponse(int expectedCommand) {
		try {

			// attempt to read two bytes
			int attempts = 0;
			int bytesReady = 0;
			byte[] sizeBuffer = new byte[2];
			while (attempts < 5) {
				bytesReady = input.available();
				if (bytesReady == 0) {
					attempts++;
					Thread.sleep(50);
					Log.i(TAG, "Nothing there, let's try again");
				} else {
					Log.i(TAG, "There are [" + bytesReady + "] waiting for us!");
					break;
				}
			}
			if (bytesReady < 2) {
				return null;
			}
			int bytesRead = input.read(sizeBuffer, 0, 2);
			if (bytesRead != 2) {
				return null;
			}
			// calculate response size
			bytesReady = 0;
			bytesReady = sizeBuffer[0] + (sizeBuffer[1] << 8);
			Log.i(TAG, "Bytes to read input [" + bytesReady + "]");
			byte[] retBuf = new byte[bytesReady];
			bytesRead = input.read(retBuf);
			if (bytesReady != bytesRead) {
				Log.e(TAG, "Unexpected data returned!?");
				return null;
			}
			if (retBuf[1] != expectedCommand) {
				Log.e(TAG, "Thinput was an unexpected response");
				return null;
			}
			return retBuf;
		} catch (Exception e) {
			Log.e(TAG, "Error in Read Response [" + e.getMessage() + "]");
			return null;
		}
	}


}
