package se.chalmers.group42.runforlife;

import sensors.GPSInputHandler;

public interface StatusIconEventListener {
	
	/**
	 * Called when the GPS has connected and has an
	 * accuracy less than {@link GPSInputHandler.MAXIMAL_ACCEPTABLE_ACCURACY}
	 */
	public void onGPSConnect();
	
	/**
	 * Called when the GPS has diconnected or has an
	 * accuracy larger than {@link GPSInputHandler.MAXIMAL_ACCEPTABLE_ACCURACY}
	 */
	public void onGPSDisconnect();
	
	public void onSoundOn();
	
	public void onSoundOff();
	
	/**
	 * Called when headphones are plugged in
	 */
	public void onHeadphonesIn();
	
	/**
	 * Called when headphones are unplugged
	 */
	public void onHeadphonesOut();
	
}
