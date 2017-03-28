package com.example.motorcontroller;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

	public final int DEFAULT_SEND_PORT = 8888;
	public final int DEFAULT_RECEIVE_PORT = 6000;
	public final String DEFAULT_LOCAL_IP = "192.168.1.177";
	
	public UDP_Client Client = new UDP_Client();
	public UDP_Server Server = new UDP_Server();
	public int currentVelocity = 120;
	public String currentAndroidIP = "0.0.0.0";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		EditText stepperspeed_tb = (EditText) findViewById(R.id.stepperspeed_tb);
		stepperspeed_tb.setText("0");

		EditText androidip_tb = (EditText) findViewById(R.id.androidip_tb);
		currentAndroidIP = Utils.getIPAddress(true);
		androidip_tb.setText(currentAndroidIP);
		
		EditText controllerip_tb = (EditText) findViewById(R.id.controllerip_tb);
		controllerip_tb.setText(DEFAULT_LOCAL_IP);

    	Switch onoffswitch = (Switch) findViewById(R.id.onoffswitch);
		onoffswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	EditText stepperspeed_tb = (EditText) findViewById(R.id.stepperspeed_tb);
		    	Button minus_btn = (Button) findViewById(R.id.minus_btn);
		    	Button plus_btn = (Button) findViewById(R.id.plus_btn);
		    	Switch directionswitch = (Switch) findViewById(R.id.directionswitch);
				if(isChecked){
					stepperspeed_tb.setEnabled(true);
					plus_btn.setEnabled(true);
					minus_btn.setEnabled(true);
					directionswitch.setEnabled(true);
					stepperspeed_tb.setText(Integer.toString(currentVelocity));
					
					SendMessage("v"+currentVelocity);
					SendMessage("1");
				}
				else{
					stepperspeed_tb.setEnabled(false);
					plus_btn.setEnabled(false);
					minus_btn.setEnabled(false);
					directionswitch.setEnabled(false);
					stepperspeed_tb.setText("0");
					
					SendMessage("v"+currentVelocity);
					SendMessage("0");
				}
		    }
		});

    	Switch directionswitch = (Switch) findViewById(R.id.directionswitch);
    	directionswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					SendMessage("cd1");
				}
				else{
					SendMessage("cd0");
				}
		    }
		});
	} 
	
	public void checkButtonClick(View view) {
		
		EditText status_tb = (EditText) findViewById(R.id.status_tb);
		EditText controllerip_tb = (EditText) findViewById(R.id.controllerip_tb);
		String response = Utils.ping(controllerip_tb.getText().toString());
		if(response.contains("Unreachable")){
			status_tb.setText("UNREACHABLE");
			status_tb.setTextColor(Color.RED);
		}
		else if(response.contains("0 received")){
			status_tb.setText("OFFLINE");
			status_tb.setTextColor(Color.RED);
		}
		else{
			status_tb.setText("ONLINE");
			status_tb.setTextColor(Color.GREEN);

			Server.runUdpServer(mHandler, mUpdateResults, 6000);
		}
	}
	
	public void setSensorSpeedInView(String speed){
		TextView sensorspeedresult_txv = (TextView) findViewById(R.id.sensorspeedresult_txv);
		sensorspeedresult_txv.setText(speed);
	}
	
	final Handler mHandler = new Handler();

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	setSensorSpeedInView(UDP_Server.sensorspeed);
        }
    };

	public void minusButtonClick(View view) {
		
		EditText stepperspeed_tb = (EditText) findViewById(R.id.stepperspeed_tb);
		int velocity = Integer.parseInt(stepperspeed_tb.getText().toString());
		if (velocity > 0) {
			velocity--;
			currentVelocity = velocity;
			stepperspeed_tb.setText(Integer.toString(velocity));

			SendMessage("v"+velocity);
		}
	}

	public void plusButtonClick(View view) {

		EditText stepperspeed_tb = (EditText) findViewById(R.id.stepperspeed_tb);
		int velocity = Integer.parseInt(stepperspeed_tb.getText().toString());
		if (velocity < 500) {
			velocity++;
			currentVelocity = velocity;
			stepperspeed_tb.setText(Integer.toString(velocity));
			
			SendMessage("v"+velocity);
		}
	}
	
	public void SendMessage(String message){
		
		//Send the message
		EditText controllerip_tb = (EditText) findViewById(R.id.controllerip_tb);
		String address = controllerip_tb.getText().toString();
		Client.Send(message, address, DEFAULT_SEND_PORT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
