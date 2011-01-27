/*
 *	  This file is part of the Bytewalla Project
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
 *    
 *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */
package se.kth.ssvl.tslab.bytewalla.androiddtn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNApps;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.DTNConfiguration;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.DTNConfigurationParser;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.InvalidDTNConfigurationException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.DiscoveriesSetting.AnnounceEntry;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.DiscoveriesSetting.DiscoveryEntry;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.InterfacesSetting.InterfaceEntry;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.LinksSetting.LinkEntry;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.config.RoutesSetting.RouteEntry;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.ContactManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Interface;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security.SPD;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security.SPD.spd_direction_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security.SPD.spd_policy_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.BundleStore;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.GlobalStorage;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.RegistrationStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Android Activity to configure, manage, and monitor DTNServer. The display of this activity is bound to the DTNManager app
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * @see DTNService 
 */
public class DTNManager extends Activity  {
	/**
	 * The String TAG for supporting Android Logging mechanism
	 */
	private final static String TAG = "DTNManager";

	/**
	 * The Android notification manager to shows to notification to user 
	 */
	private NotificationManager notification_manager_;
	/**
	 * Reference object to edit config Button according to DTNManager Layout
	 */
	private Button configurationButton;
	/**
	 * Reference object to start or stop Button according to DTNManager Layout
	 */
	private Button startStopButton;
	/**
	 * Reference object to restart Button according to DTNManager Layout
	 */
	private Button restartButton;
	
	/**
	 * Reference object to DTN Application Button according to DTNManager Layout
	 */
	private Button appsButton;
	/**
	 * Reference object to Quit Button according to DTNManager Layout
	 */
	private Button quitButton;
	/**
	 * Reference object to reset storage according to DTNManager Layout 
	 */
	private Button resetStorageButton;
	/**
	 * Reference to Outer object for the anonymous inner class
	 */
	private DTNManager self_;
	/**
	 * Saved DTNConfiguration object for using across the application
	 */
	private static DTNConfiguration dtn_config_;
	/**
	 * Reference object to DTNServiceTextView according to DTNManager Layout
	 */
	private TextView DTNServiceTextView;
	
	/**
	 * Reference object to StatusHeaderTextView according to DTNManager Layout
	 */
	private TextView StatusHeaderTextView;
	/**
	 * Reference object to StorageConsumptionTextView according to DTNManager Layout
	 */
	private TextView StorageConsumptionTextView;
	
	/**
	 * Reference object to NoBundlesRegistrationsTextView according to DTNManager Layout
	 */
	private TextView NoBundlesRegistrationsTextView;
	/**
	 * Reference object to NoBundlesUploadingDownloadingTextView according to DTNManager Layout
	 */
	private TextView NoBundlesUploadingDownloadingTextView;
	
	/**
	 * Reference object to NoActiveInterfacesLinksTextView according to DTNManager Layout
	 */
	private TextView NoActiveInterfacesLinksTextView;
	
	/**
	 * Reference object to DTNConfigurationTextView according to DTNManager Layout
	 */
	private TextView DTNConfigurationTextView;
	
	/**
	 * Reference object to StorageTypeQuotaTextView according to DTNManager Layout 
	 */
	private TextView StorageTypeQuotaTextView;
	
	/**
	 * Reference object to StoragePathTextView according to DTNManager Layout 
	 */
	private TextView StoragePathTextView;
	
	private TextView PolicyCaptionTextView;
	
	private TextView PolicyInTextView;
	
	private TextView PolicyOutTextView;	
	
	public static Activity me = null; // sd. to get the context later.
	
	public static final String PREF_MY_ENDPOINT = "PREF_MY_ENDPOINT";
	
	/**
	 * Reference object to InterfacesCaptionTextView according to DTNManager Layout 
	 */
	private TextView InterfacesCaptionTextView;
	
	/**
	 * Reference object to InterfacesListTextView according to DTNManager Layout 
	 */
	private TextView InterfacesListTextView;
	
	/**
	 * Reference object to LinksCaptionTextView according to DTNManager Layout 
	 */
	private TextView LinksCaptionTextView;
	
	/**
	 * Reference object to LinksListTextView according to DTNManager Layout 
	 */
	private TextView LinksListTextView;
	
	/**
	 * Reference object to RoutesCaptionTextView according to DTNManager Layout 
	 */
	private TextView RoutesCaptionTextView;
	
	/**
	 * Reference object to RouteLocalEIDTextView according to DTNManager Layout 
	 */
	private TextView RouteLocalEIDTextView;
	
	/**
	 * Reference object to RouterTypeTextView according to DTNManager Layout 
	 */
	private TextView RouterTypeTextView;
	
	/**
	 * Reference object to RoutesListTextView according to DTNManager Layout 
	 */
	private TextView RoutesListTextView;
	
	/**
	 * Reference object to DiscoveriesCaptionTextView according to DTNManager Layout 
	 */
	private TextView DiscoveriesCaptionTextView;
	
	/**
	 * Reference object to DiscoveriesListTextView according to DTNManager Layout 
	 */
	private TextView DiscoveriesListTextView;
	
	/**
	 * Reference object to AnnouncesCaptionTextView according to DTNManager Layout 
	 */
	private TextView AnnouncesCaptionTextView;
	
	/**
	 * Reference object to AnnouncesListTextView according to DTNManager Layout 
	 */
	private TextView AnnouncesListTextView;
	
	/**
	 * Reference object to BatteryStatusTextView according to DTNManager Layout 
	 */
	private TextView BatteryStatusTextView;
	
	/**
	 * Progress Dialog to show that the DTNServer is working and not making the user shutting down the application
	 */
	private ProgressDialog LoadingDialog;
	
	/**
	 * Handler message ID for application stop 
	 */
	final private static int APPLICATION_STOP_HANDLER_MESSAGE_ID = 0;
	
	/**
	 * Handler message ID for application start 
	 */
	final private static int APPLICATION_START_HANDLER_MESSAGE_ID = 1;
	
	/**
	 * Handler message ID for application restart 
	 */
	final private static int APPLICATION_RESTART_HANDLER_MESSAGE_ID = 2;
	
	/**
	 * Handler message ID for application quit 
	 */
	final private static int APPLICATION_QUIT_HANDLER_MESSAGE_ID = 3;
	
	/**
	 * Unique number for notification system in Android
	 */
	private static int NOTIFICATION_APPLICATION_ID = 0;
	
	/**
	 * Handler message ID for status update 
	 */
	private static int STATUS_UPDATE_HANDLER_MESSAGE_ID = 5;
	
	/**
	 * Status update handler for updating status display over specific period
	 */
	private StatusUpdateHandler status_update_handler_;
	
	/**
	 * Android Intent for stopping the service later
	 */
	private Intent DTNServer_intent_;

	/**
	 *  the amount of delay status update handler will execute. In other words,
	 *  it will update in every of this time ( Milliseconds )
	 */
	private long status_update_delay_ = 1000;
	
	/**
	 * Part of the singleton implementation of DTNManager
	 */
	private static DTNManager instance_;
	
	
	
	public static final String USER_PREFERENCE = "USER_PREFERENCES";
	
	public static final String IN_POLICY = "IN_POLICY";
	public static final String OUT_POLICY = "OUT_POLICY";
	
	public static final String useBAB_IN = "useBAB_IN";
	public static final String useCB_IN = "useCB_IN";
	public static final String usePSB_IN = "usePSB_IN";
	
	public static final String useBAB_OUT = "useBAB_OUT";
	public static final String useCB_OUT = "useCB_OUT";
	public static final String usePSB_OUT = "usePSB_OUT";

	SharedPreferences prefs;
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtnmanager);
		me = this; //to retrieve context
		init();
	}

	/**
	 * Singleton interface
	 * @return DTNManager instance
	 */
	public static DTNManager getInstance()
	{
		
		return instance_;
	}
	
	/**
	 * API for showing Android notification to users 
	 * @param text the main text to notify
	 * @param description the description will be shown in detailed description UI
	 */
	public void notify_user(String text, String description)
	{
		Intent intent = new Intent(this, DTNManager.class);
		
		Notification notification = new Notification(R.drawable.icon,
				text, System.currentTimeMillis());
        	notification.setLatestEventInfo(DTNManager.this,
        			text, description,
	                PendingIntent.getActivity(getBaseContext(), 0, intent,
        	                PendingIntent.FLAG_CANCEL_CURRENT));
	        notification_manager_.notify(NOTIFICATION_APPLICATION_ID++, notification);
		
		
	}
	
	/**
	 * Initialize function for DTNMnager. This will call other sub initailization functions for each component.
	 */
	private void init() {
		self_ = this;
		
		if (getResources().getString(
				(R.string.DTNCleanUpInitialize)).equals("true"))
		{
    		clean_up_initialize();
		}
		//create_dir(Environment.getExternalStorageDirectory() +"/dtn");
		init_config_file();
		//init_keystore_file();
		init_text_views();
		init_buttons();
		set_stop_configuration_text();
		init_status_update_handler();
		init_dtn_server_intent();
		notification_manager_ = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		instance_ = this;
		if (DTNService.is_running())
		{
			start_DTN_service_UI_update();
			set_running_user_interface(dtn_config_);
		}
		
	}

	/**
	 * initialize DTN Server communication intent
	 */
	private void init_dtn_server_intent() {
		DTNServer_intent_ = new Intent(DTNManager.this, DTNService.class);
	}

	
	/**
	 * Status update handler for updating status for particular interval
	 */
	class StatusUpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			// only handle message in particular message ID
			if (msg.what == STATUS_UPDATE_HANDLER_MESSAGE_ID) {
				this.removeMessages(STATUS_UPDATE_HANDLER_MESSAGE_ID);
				DTNManager.this.status_update();
				sendMessageDelayed(
						obtainMessage(STATUS_UPDATE_HANDLER_MESSAGE_ID),
						status_update_delay_);

			}
		}

	};

	/**
	 * Status update routine. This will fetch data from other components to be shown in the user interface
	 */
	private void status_update() {
		StorageConsumptionTextView.setText(String.format("Storage Consumption : %.4f MB", ((double) GlobalStorage
						.getInstance().get_total_size() / Math.pow(2, 20))

		));

		NoBundlesRegistrationsTextView.setText(String.format("No. Bundles / Registrations : %s / %s", BundleStore
				.getInstance().get_bundle_count() - ContactManager
				.getInstance().number_downloading_bundles()
				, RegistrationStore.getInstance().get_registration_count()
		));
		
		
		NoBundlesUploadingDownloadingTextView.setText(String.format(
				"No. Bundles Uploading / Downloading : %s / %s", ContactManager
				.getInstance().number_uploading_bundles(), ContactManager
				.getInstance().number_downloading_bundles()

		));
		
		
		NoActiveInterfacesLinksTextView.setText(String.format(
				"No. Active Interfaces / Links  : %s / %s", Interface.iface_counter(),
				Link.link_counter()));
		
		if (DTNService.battery_stat() != null)
		{
			BatteryStatusTextView.setText(String.format("Battery Raw / Scale / Level / Status : %d / %d / %d / %s",
			             DTNService.battery_stat().raw_level(),
			             DTNService.battery_stat().scale(),
			             DTNService.battery_stat().level(),
			             DTNService.battery_stat().status()
			));
		}

		PolicyInTextView.setText(String.format("Policy IN: "+SPD.getInstance().get_global_policy(spd_direction_t.SPD_DIR_IN ).toString()));
		PolicyOutTextView.setText(String.format("Policy OUT: "+SPD.getInstance().get_global_policy(spd_direction_t.SPD_DIR_OUT ).toString()));
	}

	/**
	 * Initialize text view components
	 */
	private void init_text_views() {
		DTNServiceTextView = (TextView) this
				.findViewById(R.id.DTNManager_DTNServiceTextView);
		StatusHeaderTextView = (TextView) this
				.findViewById(R.id.DTNManager_StatusHeaderTextView);
		StatusHeaderTextView.setEnabled(true);
		StorageConsumptionTextView = (TextView) this
				.findViewById(R.id.DTNManager_StorageConsumptionTextView);
		NoBundlesRegistrationsTextView = (TextView) this
				.findViewById(R.id.DTNManager_NoBundlesRegistrationsTextView);
		NoBundlesUploadingDownloadingTextView = (TextView) this.findViewById(R.id.DTNManager_NoBundlesUploadingDownloadingTextView);
		NoActiveInterfacesLinksTextView = (TextView) this
				.findViewById(R.id.DTNManager_NoActiveInterfacesLinksTextView);
		DTNConfigurationTextView = (TextView) this
				.findViewById(R.id.DTNManager_DTNConfigurationTextView);
		DTNConfigurationTextView.setEnabled(true);
		StorageTypeQuotaTextView = (TextView) this
				.findViewById(R.id.DTNManager_StorageTypeQuotaTextView);
		StoragePathTextView = (TextView) this
				.findViewById(R.id.DTNManager_StoragePathTextView);
		InterfacesCaptionTextView = (TextView) this
				.findViewById(R.id.DTNManager_InterfacesCaptionTextView);
		InterfacesCaptionTextView.setEnabled(true);
		InterfacesListTextView = (TextView) this
				.findViewById(R.id.DTNManager_InterfacesListTextView);
		LinksCaptionTextView = (TextView) this
				.findViewById(R.id.DTNManager_LinksCaptionTextView);
		LinksCaptionTextView.setEnabled(true);
		LinksListTextView = (TextView) this
				.findViewById(R.id.DTNManager_LinksListTextView);
		RoutesCaptionTextView = (TextView) this
				.findViewById(R.id.DTNManager_RoutesCaptionTextView);
		RoutesCaptionTextView.setEnabled(true);
		RouteLocalEIDTextView = (TextView) this
				.findViewById(R.id.DTNManager_RouteLocalEIDTextView);
		RouterTypeTextView = (TextView) this
				.findViewById(R.id.DTNManager_RouterTypeTextView);
		RoutesListTextView = (TextView) this
				.findViewById(R.id.DTNManager_RoutesListTextView);
		DiscoveriesCaptionTextView = (TextView) this
				.findViewById(R.id.DTNManager_DiscoveriesCaptionTextView);
		DiscoveriesCaptionTextView.setEnabled(true);
		DiscoveriesListTextView = (TextView) this
				.findViewById(R.id.DTNManager_DiscoveriesListTextView);
		AnnouncesCaptionTextView = (TextView) this
				.findViewById(R.id.DTNManager_AnnouncesCaptionTextView);
		AnnouncesCaptionTextView.setEnabled(true);
		AnnouncesListTextView = (TextView) this
				.findViewById(R.id.DTNManager_AnnouncesListTextView);
		BatteryStatusTextView = (TextView) this.findViewById(R.id.DTNManager_BatteryStatusTextView);
		
		PolicyCaptionTextView = (TextView) this.findViewById(R.id.DTNManager_PolicyCaptionTextView);
		PolicyInTextView = (TextView) this.findViewById(R.id.DTNManager_PolicyInTextView);
		PolicyOutTextView = (TextView) this.findViewById(R.id.DTNManager_PolicyOutTextView);
		
	}

	/**
	 * Initialize Button and set onclick listeners
	 */
	private void init_buttons() {
		configurationButton = (Button) this	.findViewById(R.id.DTNManager_ConfigurationButton);
		configurationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(DTNManager.this, DTNConfigScreen.class);
				// i = new Intent(DTNManager.this, DTNManager.class);
				startActivity(i);
		/*		
				
				SPD.spd_direction_t direction;
		   
		        	direction = SPD.spd_direction_t.SPD_DIR_OUT;
		        	
		        	spd_policy_t policy = SPD.spd_policy_t.SPD_USE_NONE;
		         
		        	int policy_temp= SPD.spd_policy_t.SPD_USE_CB.getCode();       
		        	policy = SPD.spd_policy_t.get((SPD.spd_policy_t.SPD_USE_CB.getCode()|SPD.spd_policy_t.SPD_USE_NONE.getCode()));
		         

		            SPD.set_global_policy(direction, (SPD.spd_policy_t)policy);
		            
		    */        
	       
			}
		});

		startStopButton = (Button) this
				.findViewById(R.id.DTNManager_StartStopButton);

		startStopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (DTNService.is_running()) {

					// show progress dialog
					LoadingDialog = ProgressDialog.show(self_, "Processing", "Stopping DTNService...", true,
			                false);
		    		 new Thread(new Runnable() {
		    			public void run() {
		    				delay_short_period();
							
		    				stop_DTN_service();
		    				
		    				Message message = Message.obtain();
		    				message.what = APPLICATION_STOP_HANDLER_MESSAGE_ID;
		    				loading_handler.sendMessage(message);
	    	            	
		    			    }
		    			  }).start();
				} else {
					// show progress dialog
						LoadingDialog = ProgressDialog.show(self_, "Processing", "Starting DTNService...", true,
				                false);
	

			    		 new Thread(new Runnable() {
			    			public void run() {
			    				Message message = Message.obtain();
			    				message.what = APPLICATION_START_HANDLER_MESSAGE_ID;
			    				Bundle message_bundle = new Bundle();
			    				
			    				delay_short_period();

								try {
								
									start_DTN_service();
									
								} catch (FileNotFoundException e) {
									message_bundle.putString("value", e.getMessage());
									message.arg1 = 1;
									Log.e(TAG,
											"start_DTN_service: Config File Not Found Error : "
													+ e.getMessage());
								} catch (NotFoundException e) {
									message_bundle.putString("value", e.getMessage());
									message.arg1 = 1;

									Log.e(TAG, "start_DTN_service: Not Found Error : "
											+ e.getMessage());
								} catch (InvalidDTNConfigurationException e) {
									message_bundle.putString("value", e.getMessage());
									message.arg1 = 1;
											
									Log.e(TAG,
											"start_DTN_service: Invalid DTN Configuration : "
													+ e.getMessage());
								}
								
								if(message.arg1!=1){
									message_bundle.putString("value", "DTN Service is successfully started");
								}
								message.setData(message_bundle);
			    				loading_handler.sendMessage(message);
		    	            	
			    			    }
			    			  }).start();




				}

			}
		});

		restartButton = (Button) this
				.findViewById(R.id.DTNManager_RestartButton);
		restartButton.setEnabled(false);
		restartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// show progress dialog
				LoadingDialog = ProgressDialog.show(self_, "Processing", "Restarting DTN Service...", true,
		                false);
	    		 new Thread(new Runnable() {
	    			public void run() {

	    				Message message = Message.obtain();
	    				message.what = APPLICATION_RESTART_HANDLER_MESSAGE_ID;
	    				Bundle message_bundle = new Bundle();

	    				delay_short_period();

						try{
							restart_DTN_service();

						} catch (FileNotFoundException e) {

							message_bundle.putString("value", e.getMessage());
							message.arg1 = 1;

							Log.e(TAG,
									"restart_DTN_service: Config File Not Found Error : "
											+ e.getMessage());
						} catch (NotFoundException e) {

							message_bundle.putString("value", e.getMessage());
							message.arg1 = 1;

							Log.e(TAG, "restart_DTN_service: Not Found Error : "
									+ e.getMessage());
						} catch (InvalidDTNConfigurationException e) {
							
							message_bundle.putString("value", e.getMessage());
							message.arg1 = 1;

							Log.e(TAG,
									"restart_DTN_service: Invalid DTN Configuration : "
											+ e.getMessage());
						}
	    				
						if(message.arg1!=1){
							message_bundle.putString("value", "DTN Service is successfully restarted");
						}
						message.setData(message_bundle);
	    				loading_handler.sendMessage(message);
    	            	
	    			    }
	    			  }).start();



			}
		});

		appsButton = (Button) this.findViewById(R.id.DTNManager_AppsButton);
		appsButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(DTNManager.this, DTNApps.class);
				startActivity(i);
				
			}} );
		
		quitButton = (Button) this.findViewById(R.id.DTNManager_QuitButton);

		quitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (DTNService.is_running()) {
					new AlertDialog.Builder(self_)
							.setMessage(
									"The DTN Service is running. This will terminate the service. Proceed?")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											LoadingDialog = ProgressDialog.show(self_, "Processing", "Saving data...", true,
									                false);
								    		 new Thread(new Runnable() {
								    			public void run() {
								    				try {
														Thread.sleep(100);
													} catch (InterruptedException e) {
														Log.e(TAG, e.toString());
													}
													
													stop_DTN_service();
								    				
								    				Message message = Message.obtain();
								    				message.what = APPLICATION_QUIT_HANDLER_MESSAGE_ID;
								    				loading_handler.sendMessage(message);
							    	            	
								    			    }
								    			  }).start();

											self_.finish();
											
											
										}
									}).setNegativeButton("Cancel", null).show()

					;

				} else
					self_.finish();

			}
		});
		
		View debug_panel = findViewById(R.id.DTNManager_DebugLinearLayout);
		if (getResources().getString(R.string.DTNManagerShowDebugPanel).equals("true"))
		{
			debug_panel.setVisibility(View.VISIBLE);
		}
		else
		{
			debug_panel.setVisibility(View.GONE);
		}

		resetStorageButton  = (Button)findViewById(R.id.DTNManager_ResetStorageButton);
		
		resetStorageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BundleStore.getInstance().reset_storage();
				RegistrationStore.getInstance().reset_storage();
			}
		});

	}

	/**
	 * Initialize loading_handler
	 */
    private Handler loading_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	
        	Bundle message_bundle = msg.getData();
            switch (msg.what) {
            
            	case APPLICATION_STOP_HANDLER_MESSAGE_ID:
            		set_disabled_UI();
            		set_stop_configuration_text();  
            		stop_status_update_handler();
            		
    				stop_DTN_service_UI_update();
    				LoadingDialog.dismiss();
            		new AlertDialog.Builder(self_).setMessage(
            			"DTN Service is successfully stopped").setPositiveButton("OK",
						null).show();
            		break;

            	case APPLICATION_START_HANDLER_MESSAGE_ID:
            		set_running_user_interface(dtn_config_);
            		
    				LoadingDialog.dismiss();
            		
					// Give a notification Dialog that it's successfully
					// started
					new AlertDialog.Builder(self_).setMessage(message_bundle.getString("value")							)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {

									start_DTN_service_UI_update();
								}
							}).show();
					break;
            	
            	case APPLICATION_RESTART_HANDLER_MESSAGE_ID:
            		
    				LoadingDialog.dismiss();
    				
					set_disabled_UI();
					
					// Give a notification Dialog that it's successfully started
					new AlertDialog.Builder(self_).setMessage(
							message_bundle.getString("value"))
							.setPositiveButton("OK",  new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {

									start_DTN_service_UI_update();
								}
							}).show();            		
					// Give a notification Dialog that it's successfully
					// started
				break;	
				
            	case APPLICATION_QUIT_HANDLER_MESSAGE_ID:
            		LoadingDialog.dismiss();
            		break;
            		
            }
            
        }
    };

    /**
     * Routine for restarting DTN Service
     * @throws FileNotFoundException
     * @throws NotFoundException
     * @throws InvalidDTNConfigurationException
     */
	private void restart_DTN_service() throws FileNotFoundException,
			NotFoundException, InvalidDTNConfigurationException {
		
		
		stop_DTN_service();
		start_DTN_service();
	}

	/**
	 * Setting display for disable user interface by simply disabling all other buttons.
	 * This is done to prevent user to accidentally click the button while the system is processing
	 */
	private void set_disabled_UI()
	{
		startStopButton.setEnabled(false);
		restartButton.setEnabled(false);
		appsButton.setEnabled(false);
		configurationButton.setEnabled(false);
		resetStorageButton.setEnabled(false);
		quitButton.setEnabled(false);
		
	}
	
	/**
	 * Update the user interface for the starting DTN Service function
	 */
	private void start_DTN_service_UI_update()
	{
		startStopButton.setEnabled(true);
		restartButton.setEnabled(true);
		appsButton.setEnabled(true);
		configurationButton.setEnabled(true);
		resetStorageButton.setEnabled(true);
		quitButton.setEnabled(true);
		startStopButton.setText("Stop");
		
	}
	
	/**
	 * The routine for start DTN Service 
	 * @throws FileNotFoundException
	 * @throws NotFoundException
	 * @throws InvalidDTNConfigurationException
	 */
	private void start_DTN_service() throws FileNotFoundException,
			NotFoundException, InvalidDTNConfigurationException {
		// read and parse configuration file

		
		InputStream in = openFileInput(getResources().getString(R.string.DTNConfigFilePath));
		//File file = new File(getResources().getString(R.string.DTNConfigFilePath));
		//InputStream in = new FileInputStream(file);
		
		dtn_config_ = DTNConfigurationParser.parse_config_file(in);

		startService(DTNServer_intent_);

	}

	/**
	 * set the user interface in the running state
	 * @param dtn_config
	 */
	private void set_running_user_interface(DTNConfiguration dtn_config)
	{
		
		start_status_update_handler();

		status_update();
		
		// set configuration text
		set_start_configuration_text(dtn_config);
		
	}
	
	/**
	 * Actual call for initialization of init status update
	 */
	private void init_status_update_handler() {
		status_update_handler_ = new StatusUpdateHandler();
	}

	/**
	 * Delay for very short time 
	 */
	private void delay_short_period()
	{
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Log.e(TAG, e.toString());
		}
	}
	/**
	 * Begin status update by send message
	 */
	private void start_status_update_handler() {
		status_update_handler_.sendMessageDelayed(status_update_handler_
				.obtainMessage(STATUS_UPDATE_HANDLER_MESSAGE_ID), status_update_delay_);
	}
	
	

	/**
	 * Stop status update by removing the message
	 */
	private void stop_status_update_handler() {
		if (status_update_handler_.hasMessages(STATUS_UPDATE_HANDLER_MESSAGE_ID))
			status_update_handler_
					.removeMessages(STATUS_UPDATE_HANDLER_MESSAGE_ID);
	}
	
	/**
	 * Set the user interface in the stop state
	 */
	private void stop_DTN_service_UI_update()
	{
		
		startStopButton.setEnabled(true);
		restartButton.setEnabled(false);
		appsButton.setEnabled(false);
		configurationButton.setEnabled(true);
		startStopButton.setText("Start");
		resetStorageButton.setEnabled(false);
		quitButton.setEnabled(true);
		
	}
	
	/**
	 * stop DTN Service routine
	 */
	private void stop_DTN_service() {
		stopService(DTNServer_intent_);
	}

	/**
	 * Set the initailization configuration text when the system is not started
	 * @param dtn_configuration
	 */
	private void set_start_configuration_text(DTNConfiguration dtn_configuration) {
		DTNServiceTextView.setText("DTN Service : Running");
		StorageConsumptionTextView.setText(String.format(
				"Storage Consumption : %s MB", "0"));

		NoBundlesRegistrationsTextView.setText(String.format(
				"No. Bundles / Registrations : %s / %s", "0", "0"));
		
		NoBundlesUploadingDownloadingTextView.setText(String.format(
				"No. Bundles Uploading / Downloading : %s / %s", "0", "0"));
		
		
		NoActiveInterfacesLinksTextView.setText(String.format(
				"No. Active Interfaces / Links  : %s / %s", "0", "0"));

		StorageTypeQuotaTextView.setText(String.format(
				"Storage Type / Quota : %s / %s", dtn_configuration
						.storage_setting().storage_type().getCaption(),
				dtn_configuration.storage_setting().quota() + " MB"));
		StoragePathTextView.setText(String.format("Storage Path : %s",
				dtn_configuration.storage_setting().storage_path()));
		String buf = "";
		Iterator<InterfaceEntry> interface_itr = dtn_configuration
				.interfaces_setting().interface_entries().iterator();
		while (interface_itr.hasNext()) {
			InterfaceEntry entry = interface_itr.next();
			buf += String.format("Id / Conv / Port : %s / %s / %s", entry.id(),
					entry.conv_layer_type().getCaption(), entry
							.fixed_local_port() ? entry.local_port() : "N/A"

			)
					+ "\n";

		}
		InterfacesListTextView.setText(buf);

		//we update the policy with the value saved in the preferences
		prefs = getSharedPreferences(USER_PREFERENCE, Activity.MODE_PRIVATE);
		boolean useBAB_IN_checked = prefs.getBoolean(useBAB_IN, false);
		boolean useCB_IN_checked = prefs.getBoolean(useCB_IN, false);
		boolean usePSB_IN_checked = prefs.getBoolean(usePSB_IN, false); 
		boolean useBAB_OUT_checked = prefs.getBoolean(useBAB_OUT, false);
		boolean useCB_OUT_checked = prefs.getBoolean(useCB_OUT, false);
		boolean usePSB_OUT_checked = prefs.getBoolean(usePSB_OUT, false);

		int policyCode=0;
		if (useBAB_IN_checked)
			policyCode=policyCode|SPD.spd_policy_t.SPD_USE_BAB.getCode();
		if (useCB_IN_checked)
			policyCode=policyCode|SPD.spd_policy_t.SPD_USE_CB.getCode();
		if (usePSB_IN_checked)
			policyCode=policyCode|SPD.spd_policy_t.SPD_USE_PSB.getCode();
		SPD.getInstance().set_global_policy(SPD.spd_direction_t.SPD_DIR_IN, SPD.spd_policy_t.get(policyCode));

		policyCode=0;
		if (useBAB_OUT_checked)
			policyCode=policyCode|SPD.spd_policy_t.SPD_USE_BAB.getCode();
		if (useCB_OUT_checked)
			policyCode=policyCode|SPD.spd_policy_t.SPD_USE_CB.getCode();
		if (usePSB_OUT_checked)
			policyCode=policyCode|SPD.spd_policy_t.SPD_USE_PSB.getCode();
		SPD.getInstance().set_global_policy(SPD.spd_direction_t.SPD_DIR_OUT, SPD.spd_policy_t.get(policyCode));
		
		PolicyInTextView.setText(String.format("Policy IN: "+SPD.getInstance().get_global_policy(spd_direction_t.SPD_DIR_IN ).toString()));
		PolicyOutTextView.setText(String.format("Policy OUT: "+SPD.getInstance().get_global_policy(spd_direction_t.SPD_DIR_OUT ).toString()));
		
		
		buf = "";
		Iterator<LinkEntry> link_entry_itr = dtn_configuration.links_setting()
				.link_entries().iterator();
		while (link_entry_itr.hasNext()) {
			LinkEntry entry = link_entry_itr.next();
			buf += String.format(
					"Id / Conv / Dest / Type : %s / %s / %s / %s ", entry.id(),
					entry.conv_layer_type().getCaption(), entry.des(), entry
							.type().toString())
					+ "\n";

		}
		LinksListTextView.setText(buf);

		RouteLocalEIDTextView.setText(String.format("Local EID: %s",

		dtn_configuration.routes_setting().local_eid()

		)

		);

		RouterTypeTextView.setText(String.format("Router Type: %s",
				dtn_configuration.routes_setting().router_type().getCaption()));

		buf = "";
		Iterator<RouteEntry> route_entry_itr = dtn_configuration
				.routes_setting().route_entries().iterator();
		while (route_entry_itr.hasNext()) {
			RouteEntry entry = route_entry_itr.next();
			buf += String.format("Dest / Link_Id  : %s / %s ", entry.dest(),
					entry.link_id())
					+ "\n";

		}
		RoutesListTextView.setText(buf);

		buf = "";
		Iterator<DiscoveryEntry> discovery_entry_itr = dtn_configuration
				.discoveries_setting().discovery_entries().iterator();
		while (discovery_entry_itr.hasNext()) {
			DiscoveryEntry entry = discovery_entry_itr.next();
			buf += String.format("Id / Addr_Family / Port : %s / %s / %s",
					entry.id(), entry.address_family().getCaption(), entry
							.port())
					+ "\n";

		}
		DiscoveriesListTextView.setText(buf);

		buf = "";
		Iterator<AnnounceEntry> announce_entry_itr = dtn_configuration
				.discoveries_setting().announce_entries().iterator();
		while (announce_entry_itr.hasNext()) {
			AnnounceEntry entry = announce_entry_itr.next();
			buf += String
					.format(
							"Interface_Id / Discovery_Id / Conv / Interval  : %s / %s / %s / %s",
							entry.interface_id(), entry.discovery_id(), entry
									.conv_layer_type().getCaption(), entry
									.interval())
					+ "\n";

		}
		AnnouncesListTextView.setText(buf);

		Editor editor = prefs.edit();
		String        local_eid = dtn_configuration.routes_setting().local_eid()+"/test";
		editor.putString(PREF_MY_ENDPOINT, local_eid);
		editor.commit();
	}

	/**
	 * Set the stop configuration text when the system is stopped
	 */
	private void set_stop_configuration_text() {
		DTNServiceTextView.setText("DTN Service : Stopped");
		StorageConsumptionTextView.setText("Storage Consumption : N/A");
		NoBundlesRegistrationsTextView
				.setText("No. Bundles / Registrations : N/A");
		
		NoBundlesUploadingDownloadingTextView.setText("No. Bundles Uploading / Downloading : N/A");
		BatteryStatusTextView.setText("Battery Raw / Scale / Level / Status : N/A / N/A / N/A / N/A");
		NoActiveInterfacesLinksTextView.setText("No. Active Interfaces / Links  : N/A");
		StorageTypeQuotaTextView.setText("Storage Type / Quota : N/A");
		StoragePathTextView.setText("Storage Path : N/A ");
		InterfacesListTextView.setText(" N/A ");
		PolicyInTextView.setText("Policy IN: N/A");
		PolicyOutTextView.setText("Policy OUT: N/A");
		LinksListTextView.setText(" N/A ");
		RouteLocalEIDTextView.setText("Local EID: N/A");
		RouterTypeTextView.setText("Router Type: N/A");
		RoutesListTextView.setText(" N/A ");
		DiscoveriesListTextView.setText("N/A");
		AnnouncesListTextView.setText("N/A");

	}

	/**
	 * This method is for cleaning all data left over from previous ran
	 */
	private void clean_up_initialize()
	{
		try {
			copy_config_from_assets();
			
		} catch (IOException e) {
			new AlertDialog.Builder(self_).setMessage(
					"Config Inialization Error").setPositiveButton("OK",
					null).show();
			Log.e(TAG, "Config Initialization Error : " + e.getMessage());
		}
	}
	
	/**
	 * Initialization of the config file
	 */
	private void init_config_file() {

	if (!is_config_file_exist())
			try {
				copy_config_from_assets();
			} catch (IOException e) {
				new AlertDialog.Builder(self_).setMessage(
						"Config Inialization Error").setPositiveButton("OK",
						null).show();
				Log.e(TAG, "Config Initialization Error : " + e.getMessage());
			}

	}
	
	/**
	 * Initialization of the keystore_file
	 */ /*
	private void init_keystore_file() {

	if (!is_keystore_file_exist())
			try {
				copy_keystore_from_assets();
			} catch (IOException e) {
				new AlertDialog.Builder(self_).setMessage(
						"Config Inialization Error").setPositiveButton("OK",
						null).show();
				Log.e(TAG, "Config Initialization Error : " + e.getMessage());
			}

			catch (Exception e) {
				new AlertDialog.Builder(self_).setMessage(
						"Config Inialization Error").setPositiveButton("OK",
						null).show();
				Log.e(TAG, "Config Initialization Error : " + e.getMessage());
			}
			
	}*/
	
	
	/**
	 * Replace configuration in Android from the assets folder. This is useful in the development process
	 * @throws IOException
	 */ /*
	private void copy_keystore_from_assets() throws IOException 
	{
		InputStream in = null;
		OutputStream out = null;
		//String DTNConfigFilePath_bckp = getResources().getString((R.string.keyStoreFilePath_backup));
		File file = new File(getResources().getString(R.string.keyStoreFilePath));
		try {
			out = new FileOutputStream(file);
			
			Resources myCntx = DTNManager.me.getResources();
			 in = myCntx.openRawResource(R.raw.keystore);
			
			//in = getAssets().open(DTNConfigFilePath_bckp);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) 
			{
				out.write(buf, 0, len);
			}
		} 
		finally 
		{
			in.close();
			out.close();
		}

		//We show a notificatin stating that ww created a new file
		Log.e(TAG, "showing notification");
		
		new AlertDialog.Builder(DTNManager.this).setMessage(
				"keystore.bks has been created in: /sdcard/dtn/")
				.setPositiveButton("OK", null).show();
    } */
	
	/**
	 * Create application path. 
	 * If current path doesn't exist then make directories.
	 * @param path Create the current file.
	 * @return True if path successfully created
	 */ /*

	public boolean create_dir(String path){
		try{
			File dir = new File(path);
			if(!dir.isDirectory()){
				if(dir.mkdirs()){
					Log.e(TAG, "Dir Created:"+dir.getAbsolutePath());
					return true;
				}	
				Log.e(TAG, "Dir Not Created:"+dir.getAbsolutePath());
				return false;
			}
			
			return true;
		}catch (Throwable t) {
			Log.e(TAG, "Unable to Create DIR");
		}	
		return false;
	} */
	
	/**
	 * Replace configuration in Android from the assets folder. This is useful in the development process
	 * @throws IOException
	 */
	private void copy_config_from_assets() throws IOException 
	{
		
			InputStream in = null;
			OutputStream out = null;
			String DTNConfigFilePath = getResources().getString(
					(R.string.DTNConfigFilePath));
			try {
				in = getAssets().open(DTNConfigFilePath);
				out = openFileOutput(DTNConfigFilePath, MODE_PRIVATE);
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				in.close();
				out.close();
			}

		
		
		/*
		InputStream in = null;
		OutputStream out = null;
		String DTNConfigFilePath_bckp = getResources().getString((R.string.DTNConfigFilePath_backup));
		File file = new File(getResources().getString(R.string.DTNConfigFilePath));
		try {
			
			
			out = new FileOutputStream(file);
			in = getAssets().open(DTNConfigFilePath_bckp);
			//out = openFileOutput(DTNConfigFilePath_bckp, MODE_PRIVATE);
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
			out.close();
		}

		//We show a notificatin stating that ww created a new file
		Log.e(TAG, "showing notification");
		
		new AlertDialog.Builder(DTNManager.this).setMessage(
				"dtn.config.xml has been created in: /sdcard/dtn")
				.setPositiveButton("OK", null).show(); */
     
	}
	
	/**
	 * Routine to check whether the configuration file is existed
	 * @return whether the configuration file is existed 
	 */
	private boolean is_config_file_exist() {

		try {
			InputStream config_stream = openFileInput(getResources().getString(R.string.DTNConfigFilePath));
			
			//File file = new File(getResources().getString(R.string.DTNConfigFilePath));
			//InputStream config_stream = new FileInputStream(file);
			
			config_stream.close();
			return true;
		} catch (FileNotFoundException e) {

			return false;
		} catch (NotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}
	
	/**
	 * Routine to check whether the keystore file is existed
	 * @return whether the configuration file is existed 
	 */ /*
	private boolean is_keystore_file_exist() 
	{

		try 
		{
			//InputStream config_stream = openFileInput(getResources().getString(R.string.DTNConfigFilePath));
			
			File file = new File(getResources().getString(R.string.keyStoreFilePath));
			InputStream config_stream = new FileInputStream(file);
			
			config_stream.close();
			return true;
		} catch (FileNotFoundException e) {

			return false;
		} catch (NotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	} */
	
	

}