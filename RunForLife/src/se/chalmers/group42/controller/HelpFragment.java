package se.chalmers.group42.controller;

import java.util.Timer;
import java.util.TimerTask;

import se.chalmers.group42.runforlife.Constants;
import se.chalmers.group42.runforlife.FXHandler;
import se.chalmers.group42.runforlife.R;
import se.chalmers.group42.runforlife.Speech;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class HelpFragment extends Fragment{

	private FXHandler fxHandler, fxHandlerNormal, fxHandlerFrequency;
	private Button correctDirectionButton, hundredMetersButton, frequencyButton, 
	leftButton, leftForwButton, rightForwButton, rightButton, wrongDirectionButton, coinPickButton, newCoinButton, technoSoundButton, runFinishedButton;
	private SeekBar frequencySeekbar;
	private TextView distanceText;
	private Timer timer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("Fragment", "Help Fragment created");
		View rootView = inflater.inflate(R.layout.fragment_help,
				container, false);

		// Initialize audio
		(fxHandler = new FXHandler()).initSound(getActivity());
		(fxHandlerFrequency = new FXHandler()).initSound(getActivity());
		(fxHandlerNormal = new FXHandler()).initSound(getActivity());

		//Setting up
		correctDirectionButton = (Button) rootView.findViewById(R.id.button1);
		hundredMetersButton = (Button) rootView.findViewById(R.id.button2);
		frequencySeekbar = (SeekBar) rootView.findViewById(R.id.seekBar1);
		frequencySeekbar.setProgress(100);
		frequencyButton = (Button) rootView.findViewById(R.id.button3);
		distanceText = (TextView) rootView.findViewById(R.id.textView_score);
		leftButton = (Button) rootView.findViewById(R.id.buttonLeft);
		leftForwButton = (Button) rootView.findViewById(R.id.buttonLeftForw);
		rightForwButton = (Button) rootView.findViewById(R.id.buttonRightForw);
		rightButton = (Button) rootView.findViewById(R.id.buttonRight);
		wrongDirectionButton = (Button) rootView.findViewById(R.id.buttonBehind);
		coinPickButton = (Button) rootView.findViewById(R.id.button4);
		newCoinButton = (Button) rootView.findViewById(R.id.button5);
		technoSoundButton = (Button) rootView.findViewById(R.id.button6);
		runFinishedButton = (Button) rootView.findViewById(R.id.button7);
		timer = new Timer();

		//Button actions
		//Correct direction sound
		correctDirectionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Re-initiation needed to reset the angle to 0 degrees
				(fxHandlerNormal = new FXHandler()).initSound(getActivity());
				fxHandlerNormal.updateDelay(500);
				fxHandlerNormal.loop(fxHandlerNormal.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandlerNormal.stopLoop();
					}
				}, 2000);
			}
		});

		//Left button sound
		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.update(fxHandler.getNavigationFX(), 90);
				fxHandler.updateDelay(500);
				fxHandler.loop(fxHandler.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.stopLoop();
					}
				}, 2000);
			}
		});
		//Left forward button sound
		leftForwButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.update(fxHandler.getNavigationFX(), 45);
				fxHandler.updateDelay(500);
				fxHandler.loop(fxHandler.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.stopLoop();
					}
				}, 2000);
			}
		});
		//Right forward button sound
		rightForwButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.update(fxHandler.getNavigationFX(), -45);
				fxHandler.updateDelay(500);
				fxHandler.loop(fxHandler.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.stopLoop();
					}
				}, 2000);
			}
		});
		//Right button sound
		rightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.update(fxHandler.getNavigationFX(), -90);
				fxHandler.updateDelay(500);
				fxHandler.loop(fxHandler.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.stopLoop();
					}
				}, 2000);
			}
		});

		//Wrong direction sound
		wrongDirectionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.update(fxHandler.getNavigationFX(), 180);
				fxHandler.updateDelay(500);
				fxHandler.loop(fxHandler.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.stopLoop();
					}
				}, 2000);
			}
		});

		//Frequency seekbar
		frequencySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				final int MAX_PROGRESS = 100;
				int invertedDistance = MAX_PROGRESS-progress;
				fxHandlerFrequency.updateDelay((invertedDistance*Constants.MAX_DELAY)/MAX_PROGRESS);
				distanceText.setText("" + invertedDistance + " m");
				//				fxHandler.update(fxHandler.getNavigationFX(), 0, progress);
				//				fxHandler.getNavigationFX().setDistance(progress*5);
				//				fxHandler.loop(fxHandler.getNavigationFX());
				//				timer.schedule(new TimerTask() {
				//					@Override
				//					public void run() {
				//						fxHandler.stopLoop();
				//					}
				//				}, 1000);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		//Frequency sound
		frequencyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandlerFrequency.loop(fxHandlerFrequency.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandlerFrequency.stopLoop();
					}
				}, 2000);
			}
		});
		
		//Techno sound
		technoSoundButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.update(fxHandler.getNavigationFX(), (float) 0.1);
				fxHandler.loop(fxHandler.getNavigationFX());
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.stopLoop();
					}
				}, Constants.DELAY_OF_TO_SOURCE);
			}
		});

		//Hundred meters sound
		hundredMetersButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.sayDistance(new Speech(2));
			}
		});
		
		//Pick up coin sound
		coinPickButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.playCoin();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						fxHandler.sayGoodJob();
					}
				}, 1000);
			}
		});

		//New coin generated sound
		newCoinButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.sayNewCoin();
			}
		});
		
		//Run finished
		runFinishedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fxHandler.sayRunFinished();
			}
		});
		
		return rootView;
	}
}
