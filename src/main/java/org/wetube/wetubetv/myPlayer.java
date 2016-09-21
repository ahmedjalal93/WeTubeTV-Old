package org.wetube.wetubetv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;

import net.butterflytv.rtmp_client.RtmpClient;

public class myPlayer extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaController.MediaPlayerControl {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private Handler handler;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
    private ProgressDialog BufferBox;
    private int repeat = 1;
    public RtmpClient rtmpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myplayer);
        StrictMode.setThreadPolicy(policy);
        MediaGrabber.grabChannel();
        handler = new Handler();
        vidSurface = (SurfaceView) findViewById(R.id.surfView);
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaController = new MediaController(this);
            mediaPlayer.setDisplay(vidHolder);
            mediaPlayer.setAudioStreamType(3);
            mediaPlayer.setDataSource(MediaGrabber.ChannelPath);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync();
            BufferBox = ProgressDialog.show(this, MediaGrabber.ChannelTitle, "Buffering...", true, true);
            BufferBox.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface ProgressDialog) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (MediaGrabber.MediaType != "org.wetube.wetubetv.Channels") {
            mediaController.setMediaPlayer(this);
            mediaController.setAnchorView(findViewById(R.id.surfView));
            handler.post(new Runnable() {
                public void run() {
                    mediaController.setEnabled(true);
                    mediaController.show();
                }
            });
        }
        mediaPlayer.start();
        BufferBox.dismiss();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 19:
                if (MediaGrabber.ChannelId > 0) {
                    MediaGrabber.ChannelId = --MediaGrabber.ChannelId;
                    launchChannel();
                }
                return true;
            case 20:
                if (MediaGrabber.ChannelId >= 0 && MediaGrabber.ChannelId < MediaGrabber.ChannelsCount - 1) {
                    MediaGrabber.ChannelId = ++MediaGrabber.ChannelId;
                    launchChannel();
                }
                return true;
            case 4:
                finish();
                return true;
            case 23:
                finish();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void launchChannel() {
        Intent myIntent = new Intent(this, myPlayer.class);
        finish();
        startActivity(myIntent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Intent mpdIntent = new Intent(this, PlayerActivity.class)
                .setData(Uri.parse(MediaGrabber.ChannelPath+" live=1"));
        mpdIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mpdIntent);
        return false;
    }


    public void start() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (MediaGrabber.MediaType != "org.wetube.wetubetv.Channels") {
            mediaController.show();
        }
        return false;
    }
}
