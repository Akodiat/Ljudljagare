package se.chalmers.group42.gameModes;

import java.util.ArrayList;
import java.util.Collections;

import se.chalmers.group42.controller.MapFragment;
import se.chalmers.group42.controller.RunActivity;
import se.chalmers.group42.controller.RunFragment;
import se.chalmers.group42.runforlife.Constants;
import se.chalmers.group42.runforlife.FXHandler;
import se.chalmers.group42.runforlife.GMapV2Direction;
import se.chalmers.group42.runforlife.Human;
import se.chalmers.group42.runforlife.R;
import se.chalmers.group42.utils.LocationHelper;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * A game of collecting coins.
 * 
 * @author Joakim Johansson
 * 
 */
public class CoinCollectorActivity extends RunActivity {
	public static LatLng DEFAULT_POSITION = new LatLng(58.705477, 11.990884);
	public static int GAME_MODE_ID = 0;
    public CheckBox dontShowAgain;
    public static final String PREFS_NAME = "PREFERENCE";
	//private static final int DIALOG_REALLY_EXIT_ID = 0;

	private Human human; // Containing the player position and score
	private float orientation; // Compass angle
	private Location coinLocation; // Location of the sound source / Location of
	// current coin

	private ArrayList<Location> finalRoute = new ArrayList<Location>();
	// Handles the sound
	private FXHandler fx;
	private boolean generateRoute = true;

	private int curr100;

	private int checkpoints;

	private SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		human = new Human();
		//curr100 = Constants.RUN_DISTANCE;
		coinLocation = LocationHelper.locationFromLatlng(DEFAULT_POSITION);

		//Retrieving distancevalue from preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		// Initialize audio
		(fx = new FXHandler()).initSound(this);
		
		// Showing disclaimer!
		
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        adb.setView(eulaLayout);
        adb.setTitle("Attention!");
        adb.setMessage(Html.fromHtml("Please run with your own and others safety in mind, " +
        		"stay aware and give way to moving vehicles at all time. " +
        		"YOU ARE USING THIS APPLICATION AT YOUR OWN RISK."));
        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";
                if (dontShowAgain.isChecked())
                    checkBoxResult = "checked";
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("skipMessage", checkBoxResult);
                // Commit the edits!
                editor.commit();
                return;
            }
        });
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");
        if (!skipMessage.equals("checked"))
            adb.show();
	}

	// Get the finalRoute from mapFragment when the route is calculated.
	@Override
	public void sendFinalRoute(ArrayList<Location> finalRoute, float distance) {
		this.finalRoute = finalRoute;
		coinLocation = finalRoute.get(0);
		MapFragment mapFrag = (MapFragment) getSupportFragmentManager()
				.findFragmentByTag("android:switcher:" + R.id.pager + ":1");

		mapFrag.handleNewCoin(coinLocation);

		final TextView textViewToChange = (TextView) findViewById(R.id.textView_distance);
		textViewToChange.setText(distance + " m");
	}

	@Override
	public void onBackPressed() {
		quitRunActivity();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			quitRunActivity();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		// Update human location
		this.human.setLocation(location);

		if (generateRoute) {
			generateRoute = false;

			//Setting value to 700 meters if no values have been set
			String distance = sharedPref.getString("distance", "700");

			String nrPoints = sharedPref.getString("nrPoints", "3");

			checkpoints = Integer.parseInt(nrPoints);

			generateRandomRoute(Integer.parseInt(distance));


			float tempDistance = human.getLocation().distanceTo(coinLocation) 
					- Constants.MIN_DISTANCE;
			curr100 = ((int) (tempDistance / 100)) * 100;

			MapFragment mapFrag = (MapFragment) getSupportFragmentManager()
					.findFragmentByTag("android:switcher:" + R.id.pager + ":1");
			mapFrag.zoomToPosition(location);
			mapFrag.setCheckpoints(checkpoints);

			RunFragment runFrag = (RunFragment) getSupportFragmentManager()
					.findFragmentByTag("android:switcher:" + R.id.pager + ":0");
			runFrag.setMax(checkpoints);
		}

		// If a coin is found..
		if (isAtCoin() && dataHandler.isRunning()) {
			dataHandler.onAquiredCoin(human.getLocation());
			// Increase the player score by one
			this.human.modScore(1);

			// And generate a new coin to search for
			generateNewCoin();
		}

		// If a current coin is set
		if (this.coinLocation != null && !usingGyro())
			adjustPanoration();
	}

	private void pointArrowToSource_Compass() {
		ImageView arrow = (ImageView) this
				.findViewById(R.id.imageView_arrow);
		if(arrow != null)
			arrow.setRotation(getRotation());
	}

	@Override
	public void onOrientationChanged(float orientation) {
		super.onOrientationChanged(orientation);
		//human.getLocation().setBearing(orientation);
		this.orientation = orientation;
		if(this.coinLocation != null){
			adjustPanoration();
			pointArrowToSource_Compass();
		}
		//		// Update compass value
		//		this.compassFromNorth = headingAngleOrientation;
		//
		//		// If a current coin is set
		//		if (usingCompass() && this.coinLocation != null) {
		//			adjustPanoration();
		//			pointArrowToSource_Compass(headingAngleOrientation);
		//		}
	}

	private boolean isAtCoin() {
		return (// If closer than minimum distance
				human.getLocation().distanceTo(coinLocation) < Constants.MIN_DISTANCE
				// Or the accuracy is less than 50 meters but still larger
				// than the distance to the sound source.
				|| (human.getLocation().getAccuracy() < 50 ? human.getLocation()
						.distanceTo(coinLocation) < human.getLocation().getAccuracy()
						: false));
	}

	private void generateNewCoin() {
		if (human.getScore() < checkpoints) {
			// Play sound of a coin
			fx.foundCoin();

			coinLocation = this.finalRoute.get(human.getScore());

			float distance = human.getLocation().distanceTo(coinLocation) 
					- Constants.MIN_DISTANCE;
			curr100 = ((int) (distance / 100)) * 100;

			MapFragment mapFrag = (MapFragment) getSupportFragmentManager()
					.findFragmentByTag("android:switcher:" + R.id.pager + ":1");
			mapFrag.handleNewCoin(coinLocation);
			// Show collected coin on the map
			mapFrag.showCollectedCoin(human.getLocation());

		} else {
			fx.playRouteFinished();
			stop();
		}
	}

	private void adjustPanoration() {
		// negate to invert angle
		float angle = -getRotation();

		float distance = human.getLocation().distanceTo(coinLocation) 
				- Constants.MIN_DISTANCE; //Subtracting the distance that a coin can be picked up from

		if (fx.getNavigationFX().isPlaying())
			fx.update(fx.getNavigationFX(), (angle));

		// if user has moved forward to new 100s
		if (distance - curr100 <= 0) {
			int currHundred = ((int) (distance / 100));
			if(currHundred<fx.getSpeechLimit())
				fx.sayDistance(fx.getSpeech(currHundred));
			curr100 = currHundred * 100;
		}
		else if (!(distance - curr100 > 100)) {
			float delayRatio, newDist = distance % 100;
			delayRatio = (float) Math.pow(newDist / 100, 2);
			fx.updateDelay((Constants.MAX_DELAY - Constants.MIN_DELAY)
					* delayRatio + Constants.MIN_DELAY);
		}
		else {
			fx.updateDelay(Constants.MAX_DELAY);
		}
	}

	@Override
	protected void playSound() {
		super.playSound();

		if (!fx.getNavigationFX().isPlaying())
			fx.loop(fx.getNavigationFX());

	}

	@Override
	protected void stopSound() {
		super.stopSound();
		fx.stopLoop();
	}

	/**
	 * Gets the rotation according to the GPS bearing or the GyroGPSFusion, depending on the value of usingGyro(). 
	 * Rotating an upwards pointing arrow with this value will make the arrow point in the direction
	 * of the source
	 */
	public float getRotation() {
		float bearingTo = human.getLocation().bearingTo(coinLocation);
		if (bearingTo < 0) {
			bearingTo += 360;
		}
		float tempAngle = bearingTo - 
				(usingGyro() ? 
						this.orientation : 
							human.getLocation().getBearing());
		// Marcus added this, was problem before
		if(tempAngle > 180){
			tempAngle = tempAngle - 360;
		}else if(tempAngle < -180){
			tempAngle = tempAngle + 360;
		}
		return tempAngle;
	}

	private ArrayList<Location> generateRoute(int checkpoints, double radius, double angle){

		Location origo = new Location("MidLocation");
		origo.setLongitude(human.getLocation().getLongitude());
		origo.setLatitude(human.getLocation().getLatitude() + radius);

		double radiansPerCheckpoint = 2 * Math.PI / checkpoints;
		ArrayList<Location> route = new ArrayList<Location>();
		double currentCheckpoint = 2 * Math.PI * 3/4;

		for(int i = 0; i <= checkpoints; i++){

			double addLat = Math.sin(currentCheckpoint) * radius;
			double addLong = Math.cos(currentCheckpoint) * radius
					/ Math.cos(Math.toRadians(human.getLocation().getLatitude()));

			currentCheckpoint -= radiansPerCheckpoint;

			Location routeLocation = new Location("new Location");
			routeLocation.setLongitude(origo.getLongitude() + addLong);
			routeLocation.setLatitude(origo.getLatitude() + addLat);

			double diffLong = -(human.getLocation().getLongitude()
					- routeLocation.getLongitude()) 
					* Math.cos(Math.toRadians(human.getLocation().getLatitude()));
			double diffLat = -(human.getLocation().getLatitude() 
					- routeLocation.getLatitude());

			// Rotation around human location, rotation matrix
			double newLong = Math.cos(angle) * diffLong + Math.sin(angle) * diffLat;
			double newLat =  -Math.sin(angle) * diffLong + Math.cos(angle) * diffLat;

			routeLocation.setLongitude(human.getLocation().getLongitude() 
					+ newLong
					/ Math.cos(Math.toRadians(human.getLocation().getLatitude())));
			routeLocation.setLatitude(human.getLocation().getLatitude() + newLat);

			route.add(routeLocation);
		}

		return route;

	}

	private void generateRandomRoute(double distance) {

		//		//Is used to be able to set the random values in the settings-menu in the app
		Boolean random = sharedPref.getBoolean("route", false);
		String random2 = sharedPref.getString("random2", "0");

		double randomB = Double.parseDouble(random2);

		finalRoute.clear();

		double b;

		if(random){
			b = randomB;
		} else{
			b = Math.random();
		}

		double distanceFromLocation = distance / Constants.LAT_LNG_TO_METER;

		double angle = 2 * Math.PI * b; 

		ArrayList<Location> routeTest = generateRoute(checkpoints, distanceFromLocation / 2, angle);

		// Deciding which way of the route you are going to run
		if (Math.random() > 0.5 && !random){
			Collections.reverse(routeTest);
		}
		
		for (int i = 0; i < routeTest.size() - 1; i++) {
			findDirections(routeTest.get(i).getLatitude(), routeTest.get(i)
					.getLongitude(), routeTest.get(i + 1).getLatitude(),
					routeTest.get(i + 1).getLongitude(),
					GMapV2Direction.MODE_WALKING);
		}
	}
}
