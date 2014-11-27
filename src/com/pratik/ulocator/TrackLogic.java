package com.pratik.ulocator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

public class TrackLogic extends SherlockActivity implements OnClickListener,
		LocationListener {

	// variable definitions
	private SharedPreferences mPreferences;
	private SharedPreferences prefs;
	private static final String TAG = "OnTrackLogic";
	private String result;
	private String number;
	private String contact_name;
	private Button AddNumber;
	private Address address;
	private TextView numberList;
	private LocationManager locationManager;
	LocationListener locationListener;
	String strZipcode;
	String strAddressLine;
	String strLocality;
	String strAdminArea;
	ArrayList<String> phone1 = new ArrayList<String>();
	Location locationGPS;
	String text;
	TextView help;

	// Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// setting the content,initializing the variables,widgets

		super.onCreate(savedInstanceState);
		setContentView(R.layout.homepage);
		AddNumber = (Button) findViewById(R.id.AddNumbers);
		numberList = (TextView) findViewById(R.id.numberList);
		AddNumber.setOnClickListener(this);
		help = (TextView) findViewById(R.id.textView1);

		help.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), About.class);
				startActivity(intent);
			}
		});

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Getting the Prefs that are set in contacts.java
		mPreferences = getSharedPreferences("com.pratik.safetytrack",
				MODE_PRIVATE);
		if (mPreferences.getBoolean("firstrun", true)) {
			mPreferences.edit().putBoolean("firstrun", false).commit();
		} else {
			prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
			contact_name = prefs.getString("contact_name", null);
			number = prefs.getString("phoneNumber", null);
			if (number == null) {
				return;
			}

			numberList.append(contact_name);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	// creating options menu
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intentContact = new Intent(TrackLogic.this, contacts.class);
		Intent intentHelp = new Intent(TrackLogic.this, About.class);
		switch (item.getItemId()) {
		case R.id.item_add_contacts:
			startActivity(intentContact);

			return true;

		case R.id.item_feedback:

			try {
				Intent localIntent = new Intent("android.intent.action.SEND");
				localIntent.putExtra("android.intent.extra.EMAIL",
						new String[] { "pkhivesara25@gmail.com" });
				localIntent.putExtra("android.intent.extra.SUBJECT",
						"SendLocation - Feedback");
				localIntent.setType("text/plain");
				startActivity(Intent.createChooser(localIntent,
						"Choose your client"));
				return false;
			} catch (Exception e) {
				Toast.makeText(
						this,
						"Sorry, there was some problem. Please try again or send a mail at pkhivesara25@gmail.com",
						Toast.LENGTH_LONG).show();
			}
			return false;
		case R.id.item_rate:

			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri
						.parse("market://details?id=com.pratik.ulocator"));
				startActivity(intent);
				return false;
			} catch (ActivityNotFoundException localActivityNotFoundException) {
				Toast.makeText(getBaseContext(),
						"Sorry, there was some problem, Please try again",
						Toast.LENGTH_LONG).show();
				return false;
			}
		default:
			return false;

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, this);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 20000, 0, this);

	}

	@Override
	public void onClick(View v) {

		MyClass myclass = new MyClass();
		myclass.execute();

	}

	public void sendLocations() {

	}

	class MyClass extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// connectivity manager to get the wifi and data objects
			final ConnectivityManager connMgr = (ConnectivityManager) getBaseContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final android.net.NetworkInfo wifi = connMgr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			final android.net.NetworkInfo mobile = connMgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			boolean mobileDataEnabled = false; // Assume disabled

			// IMPORTANT METHOD FOR DATA CHECK
			try {
				Class cmClass = Class.forName(connMgr.getClass().getName());
				Method method = cmClass
						.getDeclaredMethod("getMobileDataEnabled");
				method.setAccessible(true); // Make the method callable
				// get the setting for "mobile data"
				mobileDataEnabled = (Boolean) method.invoke(connMgr);

			} catch (Exception e) {
				Toast.makeText(
						TrackLogic.this,
						"The application encountered a prolem!Please try again",
						Toast.LENGTH_LONG).show();
			}

			// the loop runs only if Wifi or data is available and none of the
			// objects are null
			if ((wifi != null && wifi.isAvailable())
					|| (mobileDataEnabled != false && mobile != null && mobile
							.isAvailable())) {

				try {

					// Retrieving the last known location using
					// getLastKnownLocation
					Geocoder geocoder = new Geocoder(
							TrackLogic.this.getApplicationContext(),
							Locale.getDefault());
					locationGPS = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					Location locationNetwork = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

					// Using geocoder to reverse the lat/long to real time
					// address

					List<Address> list;

					if (locationGPS != null) {
						list = geocoder.getFromLocation(
								locationGPS.getLatitude(),
								locationGPS.getLongitude(), 3);

						if (list != null) {
							if (list.size() > 0) {
								strZipcode = list.get(0).getPostalCode();
								strAdminArea = list.get(0).getAdminArea();
								strLocality = list.get(0).getLocality();
								strAddressLine = list.get(0).getAddressLine(0);

								// if 1st provider does not have data, loop
								// through
								// other providers to find it.
								int count = 0;
								while (count < list.size()) {
									if ((strZipcode == null
											|| strAdminArea == null
											|| strLocality == null || strAddressLine == null)
											&& (strZipcode.equals("null")
													|| strAdminArea
															.equals("null")
													|| strLocality
															.equals("null") || strAddressLine
														.equals("null"))) {
										strZipcode = list.get(count)
												.getPostalCode();
										strAdminArea = list.get(count)
												.getAdminArea();
										strLocality = list.get(count)
												.getLocality();
										strAddressLine = list.get(count)
												.getAddressLine(count);
									}
									count++;
								}

								result = strAddressLine + ", " + strLocality
										+ ", " + strAdminArea + "- "
										+ strZipcode;
								// sendmessage();
							}
						} else {
							Toast.makeText(
									getBaseContext(),
									"The server didn't respond. Please try again.",
									Toast.LENGTH_LONG).show();

						}

					} else if (locationNetwork != null) {
						list = geocoder.getFromLocation(
								locationNetwork.getLatitude(),
								locationNetwork.getLongitude(), 3);

						if (list != null) {
							if (list.size() > 0) {
								strZipcode = list.get(0).getPostalCode();
								strAdminArea = list.get(0).getAdminArea();
								strLocality = list.get(0).getLocality();
								strAddressLine = list.get(0).getAddressLine(0);

								// if 1st provider does not have data, loop
								// through
								// other providers to find it.
								int count = 0;
								while (count < list.size()) {
									if ((strZipcode == null
											|| strAdminArea == null
											|| strLocality == null || strAddressLine == null)
											&& (strZipcode.equals("null")
													|| strAdminArea
															.equals("null")
													|| strLocality
															.equals("null") || strAddressLine
														.equals("null"))) {
										strZipcode = list.get(count)
												.getPostalCode();
										strAdminArea = list.get(count)
												.getAdminArea();
										strLocality = list.get(count)
												.getLocality();
										strAddressLine = list.get(count)
												.getAddressLine(count);
									}
									count++;
								}

								result = strAddressLine + ", " + strLocality
										+ ", " + strAdminArea + "- "
										+ strZipcode;
								// sendmessage();
							}
						} else {
							Toast.makeText(
									getBaseContext(),
									"The server didn't respond. Please try again.",
									Toast.LENGTH_LONG).show();
						}

					} else {

						Toast.makeText(
								TrackLogic.this,
								"Unable to locate your location. Please try again",
								Toast.LENGTH_LONG).show();
					}

				}

				catch (IOException e1) {

					e1.printStackTrace();
					Toast.makeText(getBaseContext(),
							"No response from the server. Please try again",
							Toast.LENGTH_LONG).show();
				}

				// sending text

			} else {
				Toast.makeText(TrackLogic.this, "No network detected!",
						Toast.LENGTH_LONG).show();

			}
			return null;

		}

		@Override
		protected void onPostExecute(String result) {
			sendmessage();
			super.onPostExecute(result);
		}

	}

	public void sendmessage() {

		text = "Hey, I am currently at " + result;

		try {
			int i;
			SmsManager smsManager = SmsManager.getDefault();
			//

			String[] finals = number.split(",");

			for (i = 0; finals.length > i; i++) {
				smsManager.sendTextMessage(finals[i], null, text, null, null);

				Toast.makeText(TrackLogic.this, "Message send successfully!",
						Toast.LENGTH_LONG).show();
				Toast.makeText(TrackLogic.this, "Your address is: " + result,
						Toast.LENGTH_LONG).show();

			}

		} catch (Exception e) {
			Toast.makeText(
					TrackLogic.this,
					"Unable to send message. Please choose a contact and try again.",
					Toast.LENGTH_LONG).show();

			e.printStackTrace();
		}

	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
