package se.miun.hls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.R.drawable;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements OnCompletionListener {

	public static final int CONF_SERVER_LISTEN_PORT = 31337;

	private final String TAG = this.getClass().getSimpleName();
	private VideoView video;
	private Vector<Uri> video_uri_list;
	private int video_uri_iteration_index = 0;
	private HLSLocalStreamProxy hlsProxy;
	private Button playOrPauseButton;
	private Button muteButton;
	private TextView menuBackground;
	private SeekBar seekBar;
	private LinearLayout videoLayout;
	private boolean menuToggle;
	private boolean playOrPauseButtonToggle;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		this.video = (VideoView) findViewById(R.id.videoView);
		this.menuBackground = (TextView) this.findViewById(R.id.floating_background);
		this.playOrPauseButton = (Button) this.findViewById( R.id.floating_play_pause );
		this.muteButton = (Button) this.findViewById(R.id.floating_mute);
		this.seekBar = (SeekBar) this.findViewById(R.id.floating_seekbar);
		this.videoLayout = (LinearLayout) this.findViewById(R.id.videoLayout);
		
		this.menuBackground.setVisibility((int)View.INVISIBLE);
		this.playOrPauseButton.setVisibility(View.INVISIBLE); 
		this.muteButton.setVisibility(View.INVISIBLE); 
		this.seekBar.setVisibility(View.INVISIBLE);
		
		menuToggle = false;
		
		hlsProxy = new HLSLocalStreamProxy();
		try {
			hlsProxy.parseAndAddToList(Uri
					.parse("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8"));
			// .parse("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"));

			Log.d(TAG, "List of file uri:");
			this.video_uri_list = this.hlsProxy.getStreamUris();
			for (Uri u : this.video_uri_list) {
				Log.d(TAG, u.toString());
			}

			video.setOnCompletionListener(this);

			if (video_uri_list.size() > 0) {
				// Start playing all the video files described by the HLS link
				onCompletion(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		this.video.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(menuToggle) {
//					menuBackground.setVisibility(View.INVISIBLE);
//					playOrPauseButton.setVisibility(View.INVISIBLE); 
//					forwardButton.setVisibility(View.INVISIBLE); 
//					backwardsButton.setVisibility(View.INVISIBLE); 
//					seekBar.setVisibility(View.INVISIBLE); 
//					
//					menuToggle = false;
//				}
//				else {
//					menuBackground.setVisibility(View.VISIBLE);
//					playOrPauseButton.setVisibility(View.VISIBLE); 
//					forwardButton.setVisibility(View.VISIBLE); 
//					backwardsButton.setVisibility(View.VISIBLE); 
//					seekBar.setVisibility(View.VISIBLE);
//					
//					menuToggle = true;
//				}
//				return true;
//			}
//		});
		
		
//		this.video.setOnTouchListener(new OnTouchListener() {
//		
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			return true;
//		}
//	});
		
		
		//Videoview
		this.video.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(menuToggle) {
					menuBackground.setVisibility(View.INVISIBLE);
					playOrPauseButton.setVisibility(View.INVISIBLE); 
					muteButton.setVisibility(View.INVISIBLE); 
					seekBar.setVisibility(View.INVISIBLE); 
					
					menuToggle = false;
				}
				else {
					menuBackground.setVisibility(View.VISIBLE);
					playOrPauseButton.setVisibility(View.VISIBLE); 
					muteButton.setVisibility(View.VISIBLE); 
					seekBar.setVisibility(View.VISIBLE);
					
					menuToggle = true;
				}
			}
		});
		
		
		//Playbutton
		this.playOrPauseButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( video.isPlaying() ) {
					switchPlayOrPausButtonState( video.isPlaying() ); //Switch button icon
					video.pause();
				} else {
					switchPlayOrPausButtonState( video.isPlaying() ); //Switch button icon
					video.start();
				}
			}
		});
		
		
		//Mutebutton
		this.muteButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		
		
		//Seekbar
		this.seekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Do nothing?
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Stop buffering to prepare for change of sequence number?
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Calculate which *.ts sequence to start buffer.
			}
		});
		
	}

	
//	@Override
//	/**
//	 * When tapping (or dragging) screen - play next movie
//	 */
//	public boolean onTouchEvent(MotionEvent event) {
//		this.playNextVideo();
//		return super.onTouchEvent(event);
//	}

	/**
	 * When current clip is completed call function to start the next in buffer
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		playNextVideo();
	}
	
	/**
	 * Play next video
	 */
	private void playNextVideo() {
		if (this.video_uri_list.size() > ++this.video_uri_iteration_index) {

			this.video.setVideoURI(this.video_uri_list
					.get(this.video_uri_iteration_index));
			this.video.start();
			
			Log.d(TAG,
					"Now playling: "
							+ this.video_uri_list.get(
									this.video_uri_iteration_index).toString());
		}
	}
	
	
	/**
	 * Switch background icon for the play/pause button depending on current state (isPlaying)
	 */
	public void switchPlayOrPausButtonState( boolean isPlaying ) {
		if (isPlaying) {
			this.playOrPauseButton.setBackgroundResource( R.drawable.ic_media_pause );
		} else {
			this.playOrPauseButton.setBackgroundResource( R.drawable.ic_media_play );
		}
	}

	
	/**
	 * When Screen is touched and menu is untoggled, present view, otherwise hide it.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(menuToggle) {
				menuBackground.setVisibility(View.INVISIBLE);
				playOrPauseButton.setVisibility(View.INVISIBLE); 
				muteButton.setVisibility(View.INVISIBLE); 
				seekBar.setVisibility(View.INVISIBLE); 
				
				menuToggle = false;
			}
			else {
				//TODO: Bug on startup when screen is tapped for the first time (popup menu stays behind the videoView)
				
				this.videoLayout.invalidate();
				this.menuBackground.bringToFront();
				this.playOrPauseButton.bringToFront();
				this.muteButton.bringToFront();
				this.seekBar.bringToFront();
				
				menuBackground.setVisibility(View.VISIBLE);
				playOrPauseButton.setVisibility(View.VISIBLE); 
				muteButton.setVisibility(View.VISIBLE); 
				seekBar.setVisibility(View.VISIBLE);
				
				menuToggle = true;
			}
			return true; 
		}
		return false;
	}
}