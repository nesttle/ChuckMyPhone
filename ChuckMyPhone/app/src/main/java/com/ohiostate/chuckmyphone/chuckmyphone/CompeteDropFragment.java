package com.ohiostate.chuckmyphone.chuckmyphone;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Locale;

public class CompeteDropFragment extends CompeteFragment {
    private final float FALLING_MAX_ACCELERATION = 4.0f;

    private final String TUTORIAL_TEXT = "Click the arrow, then drop your phone!";

    private double acceleration;
    private boolean isFalling;

    private long fallingStartTime;
    private long fallingEndTime;

    private Sensor linearAccelerometer;

    private MediaPlayer dropSound;

    public static CompeteDropFragment newInstance() {
        return new CompeteDropFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        score = 0;
        acceleration = 0;
        isFalling = false;
        if (CurrentUser.getInstance().getGoofySoundEnabled()) {
            dropSound = MediaPlayer.create(getActivity(), R.raw.grenade_sound);
        } else {
            dropSound = MediaPlayer.create(getActivity(), R.raw.splat_sound);
        }

        //max falling speed is set when the scores are grabbed, no need to initialize here
        //maxTimeFalling = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

        //make the sensor start listening again
        initializeSensors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initializeViews(view);

        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //need to re-instantiate this at least once between each button press so a thread isn't run
        //while it is already running. This is a convenient spot that happens at least once
        //between button presses. May need to put in a delay if performance suffers (note, thread isn't run until later)
        updateViewRunnableThread = new Thread(updateViewRunnable);

        Sensor sensor = event.sensor;

        if (isRecording && sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 10) {
                lastUpdate = curTime;

                float ax = event.values[0];
                float ay = event.values[1];
                float az = event.values[2];
                acceleration = Math.sqrt(ax*ax + ay*ay + az*az);

                //if phone starts falling
                if (!isFalling && acceleration < FALLING_MAX_ACCELERATION) {
                    fallingStartTime = System.currentTimeMillis();
                    isFalling = true;
                }

                //if phone stops falling
                if (isFalling && acceleration > FALLING_MAX_ACCELERATION) {
                    fallingEndTime = System.currentTimeMillis();
                    score = (fallingEndTime-fallingStartTime);
                    isFalling = false;
                    if (CurrentUser.getInstance().getSoundEnabled()) {
                        if (dropSound != null) {
                            dropSound.start();
                        }
                    }
                }

                if (score > runHighScore) {
                    runHighScore = score;
                }

                //if new high score
                if (score > currentUser.getDropScore()) {

                    //a weird bug sometimes has the run score being higher than the score saved in current user, this removes that possibility
                    if (runHighScore > currentUser.getDropScore()) {
                        currentUser.updateDropScore(runHighScore);
                        score = runHighScore;
                    }

                    if (!badgeUnlockNames.contains(getString(R.string.badge_drop_level_one)) && !FirebaseHelper.getInstance().hasBadge(getString(R.string.badge_drop_level_one)) && score >= Badge.BADGE_DROP_LEVEL_1_SCORE()) {
                        badgeUnlockNames.add(getString(R.string.badge_drop_level_one));
                    }
                    if (!badgeUnlockNames.contains(getString(R.string.badge_drop_level_two)) && !FirebaseHelper.getInstance().hasBadge(getString(R.string.badge_drop_level_two)) && score >= Badge.BADGE_DROP_LEVEL_2_SCORE()) {
                        badgeUnlockNames.add(getString(R.string.badge_drop_level_two));
                    }
                    if (!badgeUnlockNames.contains(getString(R.string.badge_drop_level_three)) && !FirebaseHelper.getInstance().hasBadge(getString(R.string.badge_drop_level_three)) && score >= Badge.BADGE_DROP_LEVEL_3_SCORE()) {
                        badgeUnlockNames.add(getString(R.string.badge_drop_level_three));
                    }
                }
            }
        }
    }

    private void initializeSensors() {
        //set up sensor overhead
        sensManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        linearAccelerometer = sensManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //make the sensor start listening
        userHasSensor = sensManager.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        if (!userHasSensor) {
            Toast.makeText(getActivity().getApplicationContext(), "Your phone does not have the necessary sensors for this activity", Toast.LENGTH_LONG).show();
        }
    }

    void initializeViews(View view) {
        super.initializeViews(view);

        yourBestScoreTextView.setText(TUTORIAL_TEXT);

        updateViewSubRunnableScore = new Runnable() {
            @Override
            public void run() {
                if(isRecording)
                    currentScoreTextView.setText(String.format(Locale.ENGLISH, "%d", score));
                else
                    currentScoreTextView.setText(String.format(Locale.ENGLISH, "%d", runHighScore));

                if (currentUser.getDropScore() == 0) {
                    yourBestScoreTextView.setText(TUTORIAL_TEXT);
                } else{
                    yourBestScoreTextView.setText(String.format(Locale.ENGLISH, "Your best: %d", currentUser.getDropScore()));
                }
            }
        };

        showTutorialToastRunnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), "Drop your phone now! \n(disable this message in settings menu)", Toast.LENGTH_LONG).show();
            }
        };
    }
}