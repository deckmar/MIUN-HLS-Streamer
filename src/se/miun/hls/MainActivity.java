	package se.miun.hls;
	
	import java.util.Vector;
	
	import android.app.Activity;
	import android.app.AlertDialog;
	import android.content.Context;
	import android.content.DialogInterface;
	import android.media.AudioManager;
	import android.media.MediaPlayer;
	import android.media.MediaPlayer.OnCompletionListener;
	import android.net.Uri;
	import android.os.Bundle;
	import android.util.Log;
	import android.view.Menu;
	import android.view.MenuItem;
	import android.view.MotionEvent;
	import android.view.View;
	import android.view.View.OnClickListener;
	import android.widget.Button;
	import android.widget.EditText;
	import android.widget.LinearLayout;
	import android.widget.SeekBar;
	import android.widget.TextView;
	import android.widget.Toast;
	import android.widget.VideoView;
	import android.widget.SeekBar.OnSeekBarChangeListener;
	
	public class MainActivity extends Activity implements OnCompletionListener, HLSLocalStreamProxyEventListener {
	
		public static final int CONF_SERVER_LISTEN_PORT = 31337;
	
		private final String TAG = this.getClass().getSimpleName();
		private String DEFAULT_URL = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
		private VideoView video;
		private HLSLocalStreamProxy hlsProxy;
		private Button playOrPauseButton;
		private Button muteButton;
		private TextView menuBackground;
		private SeekBar seekBar;
		private LinearLayout videoLayout;
		private boolean menuToggle;
		private AudioManager mAm;
		private boolean mIsMute;
	
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
			
			// Audio mgr
			mAm = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			mIsMute = false;
			
			menuToggle = false;
			
			parseAndRun(DEFAULT_URL);
			
	/*		try {
				hlsProxy.parseAndAddToList(Uri
						.parse(DEFAULT_URL));
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
	*/
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
						video.resume();
					}
				}
			});
			
			
			//Mutebutton
			this.muteButton.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					switchMuteButtonState( mIsMute );
					isMute();
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
	
		public void parseAndRun(String url){
			hlsProxy = new HLSLocalStreamProxy(this, CONF_SERVER_LISTEN_PORT);
			try {
				//hlsProxy.setUrl("http://devimages.apple.com/iphone/samples/bipbop/gear4/prog_index.m3u8");
				hlsProxy.setUrl("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8");
	
				Log.d(TAG, "List of qualities: ");
				for (Float q : this.hlsProxy.getAvailableQualities()) {
					Log.d(TAG, q.toString());
				}
	
				video.setOnCompletionListener(this);
	
				/*if (video_uri_list.size() > 0) {
					// Start playing all the video files described by the HLS link
					onCompletion(null);
				}*/
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	//	@Override
	//	/**
	//	 * When tapping (or dragging) screen - play next movie
	//	 */
	//	public boolean onTouchEvent(MotionEvent event) {
	//		this.playNextVideo();
	//		return super.onTouchEvent(event);
	//	}
	
		@Override
		public void onCompletion(MediaPlayer mp) {
			playNextVideo();
		}
	
		private void playNextVideo() {
			/*if (this.video_uri_list.size() > ++this.video_uri_iteration_index) {
	
				this.video.setVideoURI(this.video_uri_list
						.get(this.video_uri_iteration_index));
				this.video.start();
	
				Log.d(TAG,
						"Now playling: "
								+ this.video_uri_list.get(
										this.video_uri_iteration_index).toString());
			}*/
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			menu.add(0, 1, 1, "Quality").setIcon(android.R.drawable.ic_menu_view);
			menu.add(0, 2, 2, "URL").setIcon(android.R.drawable.ic_menu_search);
			//menu.add(0, 1, 1, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
			menu.add(0, 3, 3, "Exit").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			return true;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch(item.getItemId()){
			case 1:
				final String[] qualities = {"Quality1", "Quality2", "Quality3", "Quality4"};
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("Select a quality");
				dialog.setItems(qualities, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface diaInt, int quality) {
						Toast.makeText(getApplicationContext(), qualities[quality] + " selected!", Toast.LENGTH_SHORT).show();
					}
				}).show();
				break;
			case 2:
				final EditText ed = new EditText(this);
				ed.setText("http://devimages.apple.com/iphone/samples/bipbop/gear2/prog_index.m3u8");
				ed.setSelection(ed.getText().length());
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(ed)
				.setTitle("Open URL")
				.setPositiveButton("Open", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInt, int whichButton) {
						video.stopPlayback();
						parseAndRun(ed.getText().toString());
					}
				}).show();
				break;
			case 3:
				final AlertDialog.Builder exit = new AlertDialog.Builder(this);
				exit.setTitle("Do you really want to exit?")
				.setPositiveButton("Yes, quit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						video.stopPlayback();
						finish();
					}
				})
				.setNegativeButton("No, not yet", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface diaInt, int arg1) {
						diaInt.cancel();
					}
				}).show();
				break;
			}
			return true;
		}
	
		@Override
		public void errorNetwork(String msg) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void errorOther(Exception ex) {
			// TODO Auto-generated method stub
			
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
		 * Switch background icon for the mute/unmute button depending on current state (audioOn)
		 */
		public void switchMuteButtonState( boolean audioOn ) {
			if( audioOn ) {
				this.muteButton.setBackgroundResource( R.drawable.ic_lock_ringer_off );
			} else {
				this.muteButton.setBackgroundResource( R.drawable.ic_lock_ringer_on );
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
		
		public void isMute() {
	
		      if( mIsMute ) {    
		          mAm.setStreamMute(AudioManager.STREAM_MUSIC, false);
		          mIsMute = false;
	
		      } else {
		          mAm.setStreamMute(AudioManager.STREAM_MUSIC, true);
		          mIsMute = true;
		      }
	  	}
	}