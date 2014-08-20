package de.indiajuliet.audiopong;

import de.indiajuliet.audiopong.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.media.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.hardware.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements SensorEventListener{
	
    Thread t;
    int sr = 11025;
    int amplitudeAlert = 0;
   
    int scorePlayer=0, scoreComputer=0;
    
    
    boolean isRunning = true;
    boolean useSlider = false;
    SeekBar fSlider;
    SeekBar fSlider2;
//    SeekBar fSlider3;
    
    final double twopi = 8.*Math.atan(1.);
    
    
    double sliderval;
    double sliderval2;
    double sliderval3;
    double ballX = 0.75;
    double ballY = 0.5;
    double opponentY = 0.5;
    
   
    
    
    TextView textBar1, textBar2, textBar3, textBar4, textBar5, textBar6, textBar7, textBar8, textBar9;
    
	
    private SensorManager sensorManager;
	
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
    
    public void switchInput (View view) {
    	useSlider=!useSlider;
    }
    
    public void giveUp (View view) {
    	finish();
    }
    
    
    private Handler handler = new Handler();
    private Handler handlerAlert = new Handler();
    private Handler handlerCapture = new Handler();
    
    private boolean upX=false, upY;
    private double angle = Math.random()/100;
    
    private Runnable runnable = new Runnable() {
    	   @Override
    	   public void run() {
    	      /* do what you need to do */
    		   if (ballY<=0)
    			   upY = true;
    		   if (ballY>=1)
    			   upY = false;
    		   
    		   if (ballX<=0)
    			   upX = true;
    		   if (ballX>=1)
    			   upX = false; 		   
    		   
    		   if (upY) 
    			   ballY+=Math.abs(angle);
    		   else 
    			   ballY-=Math.abs(angle);
    		   
    		   if (upX) 
    			   ballX+=0.013;
    		   else 
    			   ballX-=0.013;   
    		   
    		   if (opponentY<ballY)
    			   opponentY+=0.0051;
    		   else
    			   opponentY-=0.0051;
    		   
     	      

    		   if (ballX>=1){
    			   checkHit();    		   
    		   }
    		   if (ballX<=0)
    			   checkOpponentHit();
    		   
    		   
    	      /* and here comes the "trick" */
    	      handler.postDelayed(this, 100);
    	     // textBar4.setText("paddle_y="+String.valueOf((int)(sliderval*100)));
    	      //textBar4.setText("angle="+String.valueOf((angle)));
    	      textBar4.setText("delta_opponent="+String.valueOf((Math.abs(opponentY-ballY))));
    	      textBar5.setText("ball_y="+String.valueOf((int)(ballY*100)));
    	      textBar6.setText("ball_x="+String.valueOf((int)(ballX*100)));
    	      textBar7.setText("delta_y_ball-paddle="+String.valueOf(ballY-sliderval));
    	      textBar8.setText("Score Player="+String.valueOf(scorePlayer));
    	      textBar9.setText("Score Computer="+String.valueOf(scoreComputer));
    	      fSlider2.setProgress((int)(ballY*100));
    	   }
    	};
    	
    	
    	private Runnable runnableAlert = new Runnable() {
    		public void run() {
    			if (amplitudeAlert==0 && ballX>.7 && upX)
    				amplitudeAlert=4000;
    			else
    				amplitudeAlert=0;
    			handlerAlert.postDelayed(this, (int)(400-ballX*ballX*350));
    		}
    	};

    	private Runnable runnableCapture = new Runnable() {
    		public void run () {
    			Log.i("paddle_y=",String.valueOf(sliderval));
    			Log.i("ball_x=",String.valueOf(ballX));
    			Log.i("ball_y=",String.valueOf(ballY));
    			//handlerCapture.postDelayed(this, 1000);
    		}
    	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

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
        findViewById(R.id.switchInput).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.giveUp).setOnTouchListener(mDelayHideTouchListener);
        
        
        
        
        
        
        
        
        textBar1 = (TextView) findViewById(R.id.textView1);
        textBar2 = (TextView) findViewById(R.id.textView2);
        textBar3 = (TextView) findViewById(R.id.textView3);
        textBar4 = (TextView) findViewById(R.id.textView4);
        textBar5 = (TextView) findViewById(R.id.textView5);
        textBar6 = (TextView) findViewById(R.id.textView6);
        textBar7 = (TextView) findViewById(R.id.textView7);
        textBar8 = (TextView) findViewById(R.id.textView8);
        textBar9 = (TextView) findViewById(R.id.textView9);
        
        
        
        
        
        
        // point the slider to thwe GUI widget
        fSlider = (SeekBar) findViewById(R.id.fSlider);

        // create a listener for the slider bar;
        OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
          public void onStopTrackingTouch(SeekBar seekBar) { }
          public void onStartTrackingTouch(SeekBar seekBar) { }
          public void onProgressChanged(SeekBar seekBar, 
                                          int progress,
                                           boolean fromUser) {
              if(fromUser) sliderval = progress / (double)seekBar.getMax();
           }
        };

        // set the listener on the slider
        fSlider.setOnSeekBarChangeListener(listener);
        

        // point the slider to thwe GUI widget
        fSlider2 = (SeekBar) findViewById(R.id.fSlider2);

        // create a listener for the slider bar;
        OnSeekBarChangeListener listener2 = new OnSeekBarChangeListener() {
          public void onStopTrackingTouch(SeekBar seekBar) { }
          public void onStartTrackingTouch(SeekBar seekBar) { }
          public void onProgressChanged(SeekBar seekBar, 
                                          int progress,
                                           boolean fromUser) {
              if(fromUser) sliderval2 = progress / (double)seekBar.getMax();
           }
        };

        // set the listener on the slider
        fSlider2.setOnSeekBarChangeListener(listener2);
   

        
        
        
        
        
        
        
        
        handler.postDelayed(runnable, 100);
        handler.postDelayed(runnableAlert, 1000);
        handler.postDelayed(runnableCapture, 1000);
        

        // start a new thread to synthesise audio
        t = new Thread() {
         public void run() {
         // set process priority
         setPriority(Thread.MAX_PRIORITY);
         // set the buffer size
        int buffsize = AudioTrack.getMinBufferSize(sr,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
 
        
        // create an audiotrack object
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                          sr, AudioFormat.CHANNEL_OUT_MONO,
                                  AudioFormat.ENCODING_PCM_16BIT, buffsize,
                                  AudioTrack.MODE_STREAM);

        short samples[] = new short[buffsize];
        int amplitudePaddle = 10000;
        int amplitudeBall = 0;
       
        double frequencyPaddle = 440.f;
        double frequencyBall = 440.f;
        double frequencyAlert = 220.f;
        double phasePaddle = 0.0;
        double phaseBall = 0.0;
        double phaseAlert = 0.0;
        

        // start audio
       audioTrack.play();

       // synthesis loop
       while(isRunning){
        frequencyPaddle =  440 + 440*sliderval;
        frequencyBall =  440 + 440*ballY;
        for(int i=0; i < buffsize; i++){
        	amplitudeBall=(int)(ballX*10000);
        	samples[i] = (short) (amplitudePaddle*triangle(phasePaddle)+(amplitudeBall*Math.sin(phaseBall))+(amplitudeAlert*square(phaseAlert)));
			phasePaddle += twopi*frequencyPaddle/sr;
			phaseBall += twopi*frequencyBall/sr;
			phaseAlert += twopi*frequencyAlert/sr;
        }
       audioTrack.write(samples, 0, buffsize);;
      }
      audioTrack.stop();
      audioTrack.release();
    }
   };

   
   t.start(); 
   
   sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
     
        
    }

    
    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        getAccelerometer(event);
      }

    }

    private void getAccelerometer(SensorEvent event) {
      float[] values = event.values;
      // Movement
      float x = values[0];
      float y = (values[1]/SensorManager.GRAVITY_EARTH+1)/2;
      float z = values[2];

      
      textBar1.setText("x="+String.valueOf(((int)(x*100))/100.0));
      textBar2.setText("y="+String.valueOf(((int)(y*100))/100.0));
      textBar3.setText("z="+String.valueOf(((int)(z*100))/100.0));
      if (useSlider){
    	  fSlider.setProgress((int)(y*100));
    	  sliderval=y;
      }
    //  fSlider2.setProgress((int)((z+SensorManager.GRAVITY_EARTH)*5));
     // sliderval2=(z+SensorManager.GRAVITY_EARTH)/20;
      
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
    
    public void onDestroy(){
        super.onDestroy();
        isRunning = false;
        try {
          t.join();
         } catch (InterruptedException e) {
           e.printStackTrace();
         }
         t = null;
   }
    @Override
    protected void onResume() {
      super.onResume();
      // register this class as a listener for the orientation and
      // accelerometer sensors
      sensorManager.registerListener(this,
          sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
          SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
      // unregister listener
      super.onPause();
      sensorManager.unregisterListener(this);
    }
    
    public double triangle(double input) {
    	double modInput = input % twopi;
    	if (modInput <= Math.PI)
    		return 2/Math.PI*modInput-1;
    	else
    		return -2/Math.PI*modInput+3;
    		
    }
    
    public double square(double input) {
    	double modInput = input % twopi;
    	if (modInput <= Math.PI)
    		return 1;
    	else
    		return -1;
    		
    }
    
    public void checkHit() {
    	double delta = sliderval-ballY;
    	if (Math.abs(delta)>.05)
    		scoreComputer++;
    	else {
    		angle=delta/4;
    		if (delta<0)
    			upY=true;
    		else
    			upY=false;
    	}
    }
    
    public void checkOpponentHit() {
    	double delta = opponentY-ballY;
    	if (Math.abs(delta)>.05)
    		scorePlayer++;
    	else {
    		angle=delta/4;
    		if (delta<0)
    			upY=true;
    		else
    			upY=false;
    	}
    }
    
    
    
    public void checkHitSinglemode() {
    	double delta = sliderval-ballY;
    	if (Math.abs(delta)>.05)
    		scoreComputer++;
    	else {
    		scorePlayer++;
    		angle=delta/4;
    		if (delta<0)
    			upY=true;
    		else
    			upY=false;
    	}
    }
    
    
}
