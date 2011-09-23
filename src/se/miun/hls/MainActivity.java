package se.miun.hls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.URLUtil;
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
}