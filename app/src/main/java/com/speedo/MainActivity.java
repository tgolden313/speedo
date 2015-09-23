package com.speedo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.Viewport;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;


public class MainActivity extends Activity {

	// Thread for processing input asynchronously from UI thread
	private Thread processThread;

	// Boolean to control processThread - keeps running if runThread is true,
	// otherwise completes.
	private boolean runThread; 

	private MenuItem btMenuItem;
	private PlaceholderFragment uiFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// keep screen on - don't let it time out
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        if (savedInstanceState == null) {
        	uiFragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, uiFragment)
                    .commit();
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	updateBtIcon();
    }

    @Override
    protected void onDestroy() {
		// stop thread
		runThread = false;
		try {
			processThread.interrupt();
			processThread.join();
		} catch (Exception e) {
		}
		processThread = null;

		super.onDestroy();
    }

    /**
     * Determines state of bluetooth and updates icon acccordingly.
     */
    private void updateBtIcon() {
    	if (btMenuItem != null) {
	    	if ((processThread != null) && (processThread.isAlive())) {
	    		// BT is connecting or connected
	    		btMenuItem.setIcon(R.drawable.ic_action_bluetooth_searching_light);
	    	}
	    	else {
	    		// BT not started
	    		btMenuItem.setIcon(R.drawable.ic_action_bluetooth_light);
	    	}
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        btMenuItem = menu.findItem(R.id.action_connect_to_module);
        updateBtIcon();
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        	case R.id.action_settings:
        		return true;
        	case R.id.action_connect_to_module:
        		handleActionConnect();
    			return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle bluetooth connect/disconnect action.  For connect, start processThread to connect
     * and handle input.  For testing, kicks off simulated data loop for testing UI components.
     * For disconnect, stops process thread.
     */
    private void handleActionConnect() {
    	
    	if ((processThread != null) && (processThread.isAlive())) {
    		// we want to disconnect

    		runThread = false;
    		try {
				processThread.interrupt();
    			processThread.join();
    		} catch (Exception e) {
    		}
    		processThread = null;
    		
    		btMenuItem.setIcon(R.drawable.ic_action_bluetooth_light);
    	}
    	else {
    		// we want to connect

    		runThread = true;
    		processThread = new Thread(new Runnable() {
    			
    			private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    			
    			private int lastXVal = 0;
    			private PrintWriter os = null;
    			private BufferedReader is = null;
    			
    			protected void attemptBtConnect() {
        			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        			BluetoothSocket bs;

        			// TBD
        			//if (!mBluetoothAdapter.isEnabled()) {
        			//}

        			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        			BluetoothDevice btDev;
    				
        			for (int i = 0; i < pairedDevices.size(); i++) {
        				try {
        					btDev = ((BluetoothDevice) pairedDevices.toArray()[i]);

        					if (btDev.getName().equals("CAN Relay")) {
        						bs = btDev.createRfcommSocketToServiceRecord(SPP_UUID);
        						bs.connect();

        						os = new PrintWriter(bs.getOutputStream(), true);
        						is = new BufferedReader(new InputStreamReader(bs.getInputStream()));

        						break; //Needed in the case that there are multiple devices
        					}
        				} catch (Exception e) {
        				}
        			}
    			}
    			
	            @Override
	            public void run() {
	            	while (runThread == true) {
	            		
	            		/*if (is == null) {
	            			// connect via bluetooth
	            			attemptBtConnect();
	            			
	            			if (is == null) {
	            				// still not connected, delay and retry
	    		            	try {
	    		            		Thread.sleep(3000);
	    		            	} catch (Exception e) {
	    		            	}
	            			}
	            		}
	            		
	            		// BT is connected, so read next packet
	            		else {
	            			// TBD
	            			while ((runThread == true) && (is != null)) {
	            				String dataString;
	            				try {
	            					dataString = is.readLine();
	            					// TBD - parse data
	            				} catch (Exception e) {
	            					// BT failed
	            					is = null;
	            					os = null;
	            				}
	            			}
	            			
	            		}*/
	            		
	            		//
	            		// FOR TESTING ONLY!
	            		//
	            		
		            	lastXVal++;
		            	uiFragment.volts          = lastXVal % 140;
		            	uiFragment.amps           = lastXVal % 50  * 5;
		            	uiFragment.rpms           = lastXVal % 100 * 2;
		            	uiFragment.motorTemp      = lastXVal%200;
		            	uiFragment.controllerTemp = lastXVal%200;
		            	
		            	// update the UI fragment
		            	if (uiFragment.getView() != null) {
			            	uiFragment.getView().post(uiFragment.updateViews);
		            	}
		            	
		            	try {
		            		Thread.sleep(1000);
		            	} catch (Exception e) {
		            	}
	            	}
	            }
    		});
    		processThread.start();
    		
    		btMenuItem.setIcon(R.drawable.ic_action_bluetooth_searching_light);
    	}
    }

    /**
     * Main fragment that contains all of our gauges.
     */
    public static class PlaceholderFragment extends Fragment {
        
        public Runnable updateViews;

        // data
        public float amps;
        public float volts;
        public float rpms;
        public float motorTemp;
        public float controllerTemp;

        // graph data
        private LineGraphSeries<DataPoint> series;
        private int seriesXVal;

        // views
        private NeedleGauge ampGauge;
        private NeedleGauge rpmGauge;
        private GraphView   graph;
        private CapacityGauge batGauge;
        private MotorTempGauge motorTempGauge;
        private ControllerTempGauge controllerTempGauge;
        private TextView voltsText;
        private TextView ampsText;
        private TextView wattsText;
        private TextView ahText;
        private TextView whText;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            
            ampGauge            = (NeedleGauge)         rootView.findViewById(R.id.ampGauge);
            rpmGauge            = (NeedleGauge)         rootView.findViewById(R.id.rpmGauge);
            graph               = (GraphView)           rootView.findViewById(R.id.graph);
            batGauge            = (CapacityGauge)       rootView.findViewById(R.id.capacityGauge);
            motorTempGauge      = (MotorTempGauge)      rootView.findViewById(R.id.motorTempGauge);
            controllerTempGauge = (ControllerTempGauge) rootView.findViewById(R.id.controllerTempGauge);
            voltsText           = (TextView)            rootView.findViewById(R.id.voltsTextView);
            ampsText            = (TextView)            rootView.findViewById(R.id.ampsTextView);
            wattsText           = (TextView)            rootView.findViewById(R.id.wattsTextView);
            ahText              = (TextView)            rootView.findViewById(R.id.ahTextView);
            whText              = (TextView)            rootView.findViewById(R.id.whTextView);
            
            if (rootView.findViewById(R.id.tabletLayout) == null) {
            	// for small screen layouts, change text sizes
            	ampGauge.setLabelTextSize(16);
            	rpmGauge.setLabelTextSize(16);
            	motorTempGauge.setLabelTextSize(16);
            	controllerTempGauge.setLabelTextSize(16);
            }
            
            // Add label converter
            ampGauge.setLabelConverter(new NeedleGauge.LabelConverter() {
                @Override
                public String getLabelFor(double progress, double maxProgress) {
                    return String.valueOf((int) Math.round(progress));
                }
            });
            // configure value range and ticks
            ampGauge.setMaxSpeed(600);
            ampGauge.setMajorTickStep(100);
            ampGauge.setMinorTicks(3);
            ampGauge.addColoredRange(0,   400, Color.GREEN);
            ampGauge.addColoredRange(400, 500, Color.YELLOW);
            ampGauge.addColoredRange(500, 600, Color.RED);
            ampGauge.setTitle("Amps");

            // Add label converter
            rpmGauge.setLabelConverter(new NeedleGauge.LabelConverter() {
                @Override
                public String getLabelFor(double progress, double maxProgress) {
                    return String.valueOf((int) Math.round(progress));
                }
            });
            // configure value range and ticks
            rpmGauge.setMaxSpeed(300);
            rpmGauge.setMajorTickStep(50);
            rpmGauge.setMinorTicks(3);
            rpmGauge.addColoredRange(30, 140, Color.GREEN);
            rpmGauge.addColoredRange(140, 180, Color.YELLOW);
            rpmGauge.addColoredRange(180, 400, Color.RED);
            rpmGauge.setTitle("RPM");

            seriesXVal = 0;
            series = new LineGraphSeries<DataPoint>();
            series.setThickness(2);
            series.setColor(Color.CYAN);
            graph.addSeries(series);
            
            GridLabelRenderer rend = graph.getGridLabelRenderer();
            rend.setVerticalLabelsColor(Color.WHITE);
            rend.setHorizontalLabelsVisible(false);
            rend.setVerticalAxisTitleColor(Color.WHITE);
            rend.setGridColor(Color.WHITE);

            Viewport viewPort = graph.getViewport();
            viewPort.setYAxisBoundsManual(true);
            viewPort.setMinY(0.0);
            viewPort.setMaxY(160.0);
            
            updateViews = new Runnable() {
	            @Override
	            public void run() {
	            	series.appendData(
	            			new DataPoint(
	            					seriesXVal++,
	            					volts), true, 40);
	            	ampGauge.setSpeed(amps);
	            	rpmGauge.setSpeed(rpms);
	            	batGauge.setCapacity((int)Math.round((volts / 120.0) * 100));
	            	motorTempGauge.setTemp(Math.round(motorTemp));
	            	controllerTempGauge.setTemp(Math.round(controllerTemp));

	            	// these only update for tablet mode
	            	if (voltsText != null) {
	            		voltsText.setText(String.format("%.1f V", volts));
	            		ampsText.setText(String.format("%.1f A", amps));
	            		wattsText.setText(String.format("%.1f kW", volts*amps/1000.0f));
	            		ahText.setText(String.format("%.1f Ah", 0.0f));
	            		whText.setText(String.format("%.1f kWh", 0.0f));
	            	}
	            }
			};
            
            
            return rootView;
        }
    }
}
