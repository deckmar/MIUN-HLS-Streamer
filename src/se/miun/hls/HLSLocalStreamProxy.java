package se.miun.hls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.util.Log;

public class HLSLocalStreamProxy implements HLSLocalStreamProxyInterface {

	private class BufferedVideoFile {
		final String bufferBase = "./";
		File videoFile;

		public BufferedVideoFile() {
		}

		void downloadAsync(final String url) {
			Executors.newSingleThreadExecutor().submit(new Runnable() {

				@Override
				public void run() {
					try {
						downloadSync(url);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		void downloadSync(String url) throws Exception {
			String filename = "buffer_video_" + (new Random()).nextInt();
			videoFile = new File(bufferBase + filename);

			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(new URI(url));
			HttpResponse response = httpClient.execute(httpGet, localContext);

			InputStream is = response.getEntity().getContent();
			OutputStream os = new FileOutputStream(this.videoFile);

			byte[] buffer = new byte[2048];
			while (is.available() > 0) {
				int size = is.read(buffer);
				os.write(buffer, 0, size);
			}

			videoFile.deleteOnExit();
		}

		void delete() {
			videoFile.delete();
		}
	}

	private HLSLocalStreamProxyEventListener listener = null;
	private static final String FILETYPE_PLAYLIST = "m3u8";

	private String TAG = this.getClass().getSimpleName();

	private String baseUrl = "";
	private HashMap<Float, String> playlistQualityUrlMap = new HashMap<Float, String>();
	private Vector<String> videoFileNames = new Vector<String>();

	private Vector<BufferedVideoFile> bufferedVideoParts = new Vector<HLSLocalStreamProxy.BufferedVideoFile>();
	private String fullUrl;

	// private ServerSocket listenSock

	public HLSLocalStreamProxy(HLSLocalStreamProxyEventListener listener,
			int listenPort) {
		this.listener = listener;
	}

	/**
	 * If the resource is a m3u8 playlist it will be fetched and parsed, if it
	 * is a videofile it will be added in the list. In effect this fetches all
	 * the leaf nodes (actual media http-links) in the playlist-tree.
	 * 
	 * @param resourceUri
	 * @throws Exception
	 */
	public void parseAndAddToList(Uri resourceUri, boolean root)
			throws Exception {

		if (root) {
			this.baseUrl = "http://" + resourceUri.getHost();
			Log.d(TAG, baseUrl);
		}

		String filename = resourceUri.getLastPathSegment().toString();
		String extention = "";
		String split[] = filename.split("\\.");
		if (split.length >= 2) {
			extention = split[split.length - 1];
		}

		if (root) {
			// The current playlist-files correspond to qualities.

			String currentPath = "";
			Float currentQuality = 0f;
			
			String content = downloadContents(resourceUri);
			for (String line : content.split("\n")) {
				line = line.trim();
				if (line.startsWith("#") && line.contains("BANDWIDTH")) {
					// Get the current bandwidth from the EXT comment
					String[] split2 = line.split("BANDWIDTH=");
					currentQuality = Float.parseFloat(split2[1]);
				} else if (!line.startsWith("#")) {

					String[] split2 = line.split("\\/");
					currentPath = split2[0];
					
					this.playlistQualityUrlMap.put(currentQuality, currentPath);
					
					Log.d(TAG, "Put: " + currentQuality + " : " + currentPath);
					
					/*String uriPathWithoutFilename = resourceUri.toString();
					uriPathWithoutFilename = uriPathWithoutFilename.substring(
							0, uriPathWithoutFilename.indexOf(resourceUri
									.getLastPathSegment()));*/

					/*Uri child = Uri.withAppendedPath(
							Uri.parse(uriPathWithoutFilename), line.trim());*/
				}
			}

			/*
			 * for (Uri uri : parseList(resourceUri)) {
			 * this.parseAndAddToList(uri, false); }
			 */
		} else {
			this.videoFileNames.add(filename);
		}
	}

	/*private Vector<Uri> parseList(Uri listUri) throws Exception {
		Vector<Uri> ret = new Vector<Uri>();

		String content = downloadContents(listUri);

		// Log.d(TAG, content);

		for (String line : content.split("\n")) {

			// Log.d(TAG, "Line: " + line);

			if (line.trim().startsWith("#")) {
				continue;
			}

			String uriPathWithoutFilename = listUri.toString();
			uriPathWithoutFilename = uriPathWithoutFilename
					.substring(0, uriPathWithoutFilename.indexOf(listUri
							.getLastPathSegment()));

			Uri child = Uri.withAppendedPath(Uri.parse(uriPathWithoutFilename),
					line.trim());
			ret.add(child);
		}

		return ret;
	}*/

	private String downloadContents(Uri uri) throws Exception {
		StringBuilder sb = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(new URI(uri.toString()));
		HttpResponse response = httpClient.execute(httpGet, localContext);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));

		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}

		return sb.toString();
	}

	@Override
	public void setUrl(String topPlaylistUrl) throws Exception {
		this.fullUrl = topPlaylistUrl;
		
		parseAndAddToList(Uri.parse(topPlaylistUrl), true);
	}

	@Override
	public String getUrl() {
		return this.fullUrl;
	}

	@Override
	public boolean validateUrl(String topPlaylistUrl) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector<Float> getAvailableQualities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQuality(int qualityIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasNextLocalVideoUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getNextLocalVideoUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * public void startLocalMediaProxy(int listenPort) throws IOException {
	 * 
	 * ServerSocket listen = new ServerSocket(listenPort);
	 * 
	 * }
	 */
}
