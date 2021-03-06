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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.media.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.hardware.*;


public class GameActivity extends Activity implements SensorEventListener{

	//DESIGN
	
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
    
    
    
    
    
    
	// Audio Synthesis block
    Thread audioThread;
    int sr = 44100;
    int amplitudeAlert = 0;
    final double twopi = 8.*Math.atan(1.);
  
    
    // Game variables
    double playerY = 0.5;
    double ballX = 0.75;
    double ballY = 0.5;
    double opponentY = 0.5;
    int matchLength;
   	int scorePlayer=0, scoreComputer=0;
    boolean useSlider ;
    private boolean upX=false, upY;
    private double angle = Math.random()/100;
    double opponentSpeed;
    
	// Needed Objects
    private SensorManager sensorManager;
    SeekBar paddleSlider, ballSlider, opponentPaddleSlider;
    TextView tbScorePlayer, tbScoreComputer;
    
   // Handler to send delayed messages for recursive call
    private Handler gameHandler = new Handler();
    private Handler alertHandler = new Handler();
   // private Handler handlerCapture = new Handler();
    boolean isRunning = true;  

    /**
     * Returns an triangular signal
     */
    public double triangle(double input) {
    	double modInput = input % twopi;
    	if (modInput <= Math.PI)
    		return 2/Math.PI*modInput-1;
    	else
    		return -2/Math.PI*modInput+3;
    }

    /**
     * Returns an square signal
     */
    public double square(double input) {
    	double modInput = input % twopi;
    	if (modInput <= Math.PI)
    		return 1;
    	else
    		return -1;
    }

    /**
     * Increments the score of opposing player if ball is missed or sets new angle if hit
     */
    public void checkPlayerHit() {
    	double delta = playerY-ballY;
    	if (Math.abs(delta)>.05){
    		scoreComputer++;
    		if (scoreComputer>=matchLength){
    			goGameOver();
    			finish();
    		}
    	}
    	else {
    		angle=delta/4;
    		if (delta<0)
    			upY=true;
    		else
    			upY=false;
    	}
    }
    
    /**
     * Increments the score of opposing player if ball is missed or sets new angle if hit
     */
    public void checkOpponentHit() {
    	double delta = opponentY-ballY;
    	if (Math.abs(delta)>.05){
    		scorePlayer++;
			if (scorePlayer>=matchLength){
				goGameOver();
				finish();
			}
		}
    	else {
    		angle=delta/4;
    		if (delta<0)
    			upY=true;
    		else
    			upY=false;
    	}
    }
    
    /**
     * Starts the GameoverActivity
     */
    public void goGameOver() {
    	Intent intent = new Intent(GameActivity.this,GameoverActivity.class);
    	intent.putExtra("scorePlayer",scorePlayer);
    	intent.putExtra("scoreComputer", scoreComputer);
    	startActivity(intent);
    }
    
    /**
     * Sets useSlider to the opposite 
     */
    public void switchInput (View view) {
    	useSlider=!useSlider;
    }
    
    public void giveUp (View view) {
    	finish();
    }
    
    // the game itself
    private Runnable gameRunnable = new Runnable() {
    	   @Override
    	   public void run() {
    	   // upper and lower borders
    		   if (ballY<=0)
    			   upY = true;
    		   if (ballY>=1)
    			   upY = false;
    		  
    	   // left and right borders
    		   if (ballX<=0){
    			   upX = true;
    			   checkOpponentHit();
    		   }	   
    		   if (ballX>=1){
    			   upX = false;
    			   checkPlayerHit();  
    		   }
    		   
    	   // Ball y-movement
    		   if (upY) 
    			   ballY+=Math.abs(angle);
    		   else 
    			   ballY-=Math.abs(angle);
    		   
    	   // Ball x-movement
    		   if (upX) 
    			   ballX+=0.013;
    		   else 
    			   ballX-=0.013;   
    		
    	   //  opponent paddle movement towards ball
    		   if (opponentY<ballY)
    			   opponentY+=opponentSpeed;
    		   else
    			   opponentY-=opponentSpeed;
    	   
    	   // rerun every 1/10 s  
    		   if (isRunning)
    			   gameHandler.postDelayed(this, 100);

    	   // update screen
    	      tbScorePlayer.setText("PLAYER="+String.valueOf(scorePlayer));
    	      tbScoreComputer.setText("COMPUTER="+String.valueOf(scoreComputer));
    	      ballSlider.setProgress((int)(ballY*100));
    	      opponentPaddleSlider.setProgress((int)(opponentY*100));
    	   }
    	};
    	
    	// Alternates the amplitude of the alert signal when ball comes near player 
    	private Runnable alertRunnable = new Runnable() {
    		public void run() {
    			if (amplitudeAlert==0 && ballX>.7 && upX)
    				amplitudeAlert=4000;
    			else
    				amplitudeAlert=0;
    			if (isRunning)
    				alertHandler.postDelayed(this, (int)(400-ballX*ballX*350));
    		}
    	};

    	
    	
//    	private Runnable runnableCapture = new Runnable() {
//    		public void run () {
//    			Log.i("paddle_y=",String.valueOf(playerY));
//    			Log.i("ball_x=",String.valueOf(ballX));
//    			Log.i("ball_y=",String.valueOf(ballY));
//    			handlerCapture.postDelayed(this, 1000);
//    		}
//    	};
    	
    	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

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
        
        
        
        
        // find the layout components from their ID
        tbScorePlayer = (TextView) findViewById(R.id.scorePlayer);
        tbScoreComputer = (TextView) findViewById(R.id.scoreComputer);
        paddleSlider = (SeekBar) findViewById(R.id.paddleSlider);
        opponentPaddleSlider = (SeekBar) findViewById(R.id.opponentPaddleSlider);
        ballSlider = (SeekBar) findViewById(R.id.ballSlider);
        
        
        
        //Read settings and set variables to make them active
        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_WORLD_WRITEABLE);
        int inputModeID = settings.getInt("input", R.id.rbIMmove);
        switch (inputModeID){
        	case R.id.rbIMmove 	: 	useSlider=false;
        							break;
        	case R.id.rbIMtouch	:	useSlider=true;
        							break;
        }
        int matchLengthID = settings.getInt("length", R.id.rbML6);
        switch (matchLengthID){
    	case R.id.rbML3 		: 	matchLength=3;
									break;
    	case R.id.rbML6 		: 	matchLength=6;
									break;		
    	case R.id.rbML10 		: 	matchLength=10;
									break;
    	case R.id.rbMLtrainer 	: 	matchLength=10000;
									break;
        }
        int computerSpeedID = settings.getInt("speed", R.id.rbCSmiddle);
        switch (computerSpeedID){
    	case R.id.rbCSslow 		: 	opponentSpeed=0.0041;
									break;
    	case R.id.rbCSmiddle	: 	opponentSpeed=0.0051;
									break;		
    	case R.id.rbCSfast 		: 	opponentSpeed=0.008;
									break;
        }


        // create a listener for the players paddle slider bar
          OnSeekBarChangeListener playerPaddleListener = new OnSeekBarChangeListener() {
          public void onStopTrackingTouch(SeekBar seekBar) { }
          public void onStartTrackingTouch(SeekBar seekBar) { }
          public void onProgressChanged(SeekBar seekBar, 
                                          int progress,
                                           boolean fromUser) {
              if(fromUser) playerY = progress / (double)seekBar.getMax();
           }
        };

        // set the listener on the slider
        paddleSlider.setOnSeekBarChangeListener(playerPaddleListener);
        
        
        // Start the game by sending a message to the Runnables for the first time
        gameHandler.postDelayed(gameRunnable, 100);
        gameHandler.postDelayed(alertRunnable, 1000);
        //gameHandler.postDelayed(runnableCapture, 1000);
        

        // start a new thread to synthesize audio
        audioThread = new Thread() {
         public void run() {
         // set process priority
        	setPriority(Thread.MAX_PRIORITY);
         // set the buffer size
	        int buffsize = AudioTrack.getMinBufferSize(sr,
	                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	        Log.i("buffsize=",String.valueOf(buffsize));
	        
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
		        frequencyPaddle =  440 + 440*playerY;
		        frequencyBall =  440 + 440*ballY;
		        amplitudeBall=(int)(ballX*10000);
		        for(int i=0; i < buffsize; i++){
		        	samples[i] = (short) (amplitudePaddle*Math.sin(phasePaddle)+(amplitudeBall*triangle(phaseBall))+(amplitudeAlert/2*square(phaseAlert))+(amplitudeAlert*Math.random()));
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
	  
	   audioThread.start(); 
	   sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    
    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
    	  if (!useSlider)
    		  getAccelerometer(event);
      }

    }

    // only run when useSlider is false
    private void getAccelerometer(SensorEvent event) {
      float[] values = event.values;
      // Movement
      // float x = values[0];
      float y = (values[1]/SensorManager.GRAVITY_EARTH+1)/2;
      // float z = values[2];
      paddleSlider.setProgress((int)(y*100));
      playerY=y;      
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
        delayedHide(100000);
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
          audioThread.join();
         } catch (InterruptedException e) {
           e.printStackTrace();
         }
         audioThread = null;
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
}
