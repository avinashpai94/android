package com.example.amine.learn2sign;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.example.amine.learn2sign.LoginActivity.INTENT_ID;
import static com.example.amine.learn2sign.LoginActivity.INTENT_TIME_WATCHED;
import static com.example.amine.learn2sign.LoginActivity.INTENT_TIME_WATCHED_VIDEO;
import static com.example.amine.learn2sign.LoginActivity.INTENT_URI;
import static com.example.amine.learn2sign.LoginActivity.INTENT_WORD;

public class PracticeActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private TextView mTextMessage;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Button mToggleButton;
    private TextView tv_timer;
    private TextView tv_time;
    Intent returnIntent;
    String returnfile;
    VideoActivity activity;
    String word;
    String words[];
    static Random rnd = new Random();
    private boolean mInitSuccesful;
    SharedPreferences sharedPreferences;
    CountDownTimer timer;
    CountDownTimer time;
    long time_watched;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        if(getIntent().hasExtra(INTENT_WORD)) {
            words = getResources().getStringArray(R.array.spinner_words);
            word = words[rnd.nextInt(26)];


        }
        if(getIntent().hasExtra(INTENT_TIME_WATCHED)) {
            time_watched = getIntent().getLongExtra(INTENT_TIME_WATCHED,0);
        }
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_camera);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        tv_timer = (TextView) findViewById(R.id.tv_timer);
        tv_time = (TextView) findViewById(R.id.tv_time);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sharedPreferences =  this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        mToggleButton = (Button) findViewById(R.id.bt_start);
        time = new CountDownTimer(4000,1000) {
            @Override
            public void onTick(long l) {
                int a = (int) (l / 1000);
                tv_time.setText(a + " ");
            }

            @Override
            public void onFinish() {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                if(time!=null) {
                    time.cancel();
                }
                returnIntent.putExtra(INTENT_URI,returnfile);
                returnIntent.putExtra(INTENT_TIME_WATCHED_VIDEO , time_watched);
                activity.setResult(8888,returnIntent);
                activity.finish();
            }
        };
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(final View v) {
                timer = new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        int a = (int) (millisUntilFinished / 1000);
                        tv_timer.setText(a + " ");
                        ((Button) v).setEnabled(false);
                    }
                    public void onFinish() {
                        tv_timer.setVisibility(View.GONE);
                        ((Button) v).setText("Stop Recording");
                        ((Button) v).setEnabled(true);
                        mMediaRecorder.start();
                        time.start();
                    }
                };
                if (((Button) v).getText().toString().equals("Start Recording")) {
                    timer.start();
                }
                else if (((Button) v).getText().toString().equals("Stop Recording")) {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    ((Button) v).setText("Start Recording");
                    if(time!=null) {
                        time.cancel();
                    }
                    returnIntent.putExtra(INTENT_URI,returnfile);
                    returnIntent.putExtra(INTENT_TIME_WATCHED_VIDEO , time_watched);

                    activity.setResult(8888,returnIntent);
                    activity.finish();

                }
            }
        });
    }

    boolean fileCreated = false;
    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview

        if(mCamera == null) {
            mCamera = Camera.open(1);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.unlock();

        }

        if(mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        int i=0;
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
        String format = s.format(new Date());
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Learn2Sign/Practice/"
                + sharedPreferences.getString(INTENT_ID,"0000")+"_"+word+"_0_"+format  + ".mp4");
        //just to be safe
        while(file.exists()) {
            i++;
            file = new File(Environment.getExternalStorageDirectory().getPath() + "/Learn2Sign/Practice/"
                    + sharedPreferences.getString(INTENT_ID,"0000")+"_"+word+"_"+i +"_"+format+ ".mp4");
        }

        if(file.createNewFile()) {
            fileCreated = true;
            Log.e("file path",file.getPath());
            returnfile = file.getPath();
        }


        mMediaRecorder.setOutputFile(file.getPath());
        // No limit. Check the space on disk!
        mMediaRecorder.setMaxDuration(5000);
        mMediaRecorder.setVideoSize(320,240);
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {    //finish after max duration has been reached
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    if(time!=null) {
                        time.cancel();
                    }
                    returnIntent.putExtra(INTENT_URI,returnfile);
                    returnIntent.putExtra(INTENT_TIME_WATCHED_VIDEO , time_watched);
                    activity.setResult(8888,returnIntent);
                    activity.finish();
                }

            }
        });

        //mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setOrientationHint(270);
        //mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setVideoFrameRate(30); //might be auto-determined due to lighting
        mMediaRecorder.setVideoEncodingBitRate(3000000);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// MPEG_4_SP
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        try {

            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
    }


    @Override
    public void onBackPressed() {
        if(timer!=null)
            timer.cancel();

        returnIntent.putExtra(INTENT_URI,returnfile);
        returnIntent.putExtra(INTENT_TIME_WATCHED_VIDEO , time_watched);
        activity.setResult(7777,returnIntent);
        activity.finish();

        super.onBackPressed();
    }


    public void surfaceCreated(SurfaceHolder holder) {

        try {
            if(!mInitSuccesful)
                initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        //shutdown();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {}

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();
        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        returnIntent.putExtra(INTENT_URI,returnfile);
        returnIntent.putExtra(INTENT_TIME_WATCHED_VIDEO , time_watched);
        activity.setResult(7777,returnIntent);
        mCamera = null;
        timer.cancel();
        finish();
    }

}
