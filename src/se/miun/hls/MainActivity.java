package se.miun.hls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends Activity implements OnCompletionListener {

	public static final int CONF_SERVER_LISTEN_PORT = 31337;

	private final String TAG = this.getClass().getSimpleName();
	private VideoView video;
	private Vector<Uri> video_uri_list;
	private int video_uri_iteration_index = 0;
	private HLSLocalStreamProxy hlsProxy;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		video = (VideoView) findViewById(R.id.videoView);

		hlsProxy = new HLSLocalStreamProxy();
		try {
			hlsProxy.parseAndAddToList(Uri
					.parse("http://devimages.apple.com/iphone/samples/bipbop/gear4/prog_index.m3u8"));
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
	}

	@Override
	/**
	 * When tapping (or dragging) screen - play next movie
	 */
	public boolean onTouchEvent(MotionEvent event) {
		this.playNextVideo();
		return super.onTouchEvent(event);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		playNextVideo();
	}

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
			final String[] qualities = {"Quality1, Quality2, Quality3, Quality4"};
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Select a quality");
			dialog.setItems(qualities, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface diaInt, int quality) {
					Toast.makeText(getApplicationContext(), qualities[quality], Toast.LENGTH_SHORT).show();
				}
			}).show();
			break;
		case 2:
			final EditText ed = new EditText(this);
			ed.setText("Open URL");
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(ed)
			.setPositiveButton("Open", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInt, int whichButton) {
					
				}
			}).show();
			break;
		case 3:
			break;
		}
		return true;
	}
}