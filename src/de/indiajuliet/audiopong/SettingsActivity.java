package de.indiajuliet.audiopong;

import de.indiajuliet.audiopong.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.media.*;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.hardware.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SettingsActivity extends Activity {
	
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 30000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    // Radio Button Groups
    RadioGroup  rbGroupInput;
    RadioGroup  rbGroupMatchLength;
    RadioGroup  rbGroupComputerSpeed;

    // Editor for storing Preferences
    SharedPreferences.Editor editor;
    
    
    // Button navigation - start GameActivity
    public void playGame (View view) {
    	Intent intent = new Intent(getApplicationContext(),GameActivity.class);
    	startActivity(intent);
    }
    
    /**
     * Set settings file and Radiobutton to im
     * @param im
     */
    private void syncInputMode(int im){
        switch (im){
	        case R.id.rbIMmove:
	        	rbGroupInput.check(R.id.rbIMmove);
	            editor.putInt("input",R.id.rbIMmove);
	            editor.apply();
	            break;
	        case R.id.rbIMtouch:
	        	rbGroupInput.check(R.id.rbIMtouch);
	        	editor.putInt("input",R.id.rbIMtouch);
	        	editor.apply();
	        	break;
        }
    }  
    
    /**
     * Set settings file and Radiobutton to ml
     * @param ml
     */
    private void syncMatchLength(int ml){
        switch (ml){
	        case R.id.rbML3:
	        	rbGroupMatchLength.check(R.id.rbML3);
	            editor.putInt("length",R.id.rbML3);
	            editor.apply();
	            break;
	        case R.id.rbML6:
	        	rbGroupMatchLength.check(R.id.rbML6);
	        	editor.putInt("length",R.id.rbML6);
	        	editor.apply();
	        	break;
	        case R.id.rbML10:
	        	rbGroupMatchLength.check(R.id.rbML10);
	        	editor.putInt("length",R.id.rbML10);
	        	editor.apply();
	        	break;	        	
	        case R.id.rbMLtrainer:
	        	rbGroupMatchLength.check(R.id.rbMLtrainer);
	        	editor.putInt("length",R.id.rbMLtrainer);
	        	editor.apply();
	        	break;	        		        	
        }
    }  
    
    /**
     * Set settings file and Radiobutton to cs
     * @param cs
     */
    private void syncComputerSpeed(int cs){
        switch (cs){
	        case R.id.rbCSfast:
	        	rbGroupComputerSpeed.check(R.id.rbCSfast);
	            editor.putInt("speed",R.id.rbCSfast);
	            editor.apply();
	            break;
	        case R.id.rbCSmiddle:
	        	rbGroupComputerSpeed.check(R.id.rbCSmiddle);
	        	editor.putInt("speed",R.id.rbCSmiddle);
	        	editor.apply();
	        	break;
	        case R.id.rbCSslow:
	        	rbGroupComputerSpeed.check(R.id.rbCSslow);
	        	editor.putInt("speed",R.id.rbCSslow);
	        	editor.apply();
	        	break;	        	
        }
    }  
    
    

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.playGame).setOnTouchListener(mDelayHideTouchListener);

        // find Radio Button Groups by ID
        rbGroupInput = (RadioGroup) findViewById(R.id.rbGroupInput);
        rbGroupMatchLength = (RadioGroup) findViewById(R.id.rbGroupMatchLength);
        rbGroupComputerSpeed = (RadioGroup) findViewById(R.id.rbGroupComputerSpeed);
        
        // Set Listeners to react on inputs
        rbGroupInput.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(RadioGroup group, int checkedId) {
        			syncInputMode(checkedId);
        			}
        	});
        rbGroupMatchLength.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(RadioGroup group, int checkedId) {
        			syncMatchLength(checkedId);
        			}
        	});       
        rbGroupComputerSpeed.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(RadioGroup group, int checkedId) {
        			syncComputerSpeed(checkedId);
        			}
        	});        
          
        
        
       // open Shared preferences file (world writeable for debug)
        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_WORLD_WRITEABLE);
        // Point editor to settings file
        editor = settings.edit();
        
        // Read settings 
        int inputMode = settings.getInt("input", R.id.rbIMmove);
        int matchLength = settings.getInt("length", R.id.rbML6);
        int computerSpeed = settings.getInt("speed", R.id.rbCSmiddle);
        //check radiobuttons according to settings
        syncInputMode(inputMode);
        syncMatchLength(matchLength);
        syncComputerSpeed(computerSpeed);
        editor.apply();
    }

    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
