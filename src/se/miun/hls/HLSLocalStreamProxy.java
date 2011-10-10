package se.miun.hls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.util.Log;

public class HLSLocalStreamProxy {

	private static final String FILETYPE_PLAYLIST = "m3u8";

	private String TAG = this.getClass().getSimpleName();
	private Vector<Uri> streamUris = new Vector<Uri>();
	private ArrayList<Uri> uris = new ArrayList<Uri>();

	/**
	 * List of all Uri's described by the playlist
	 * 
	 * @return Returns a list of all Uri's described by the playlist
	 */
	public Vector<Uri> getStreamUris() {
		return streamUris;
	}

	public HLSLocalStreamProxy() {
	}

	/**
	 * If the resource is a m3u8 playlist it will be fetched and parsed, if it
	 * is a videofile it will be added in the list. In effect this fetches all
	 * the leaf nodes (actual media http-links) in the playlist-tree.
	 * 
	 * @param resourceUri
	 * @throws Exception
	 */
	public void parseAndAddToList(Uri resourceUri) throws Exception {
		String filename = resourceUri.getLastPathSegment().toString();
		String extention = "";
		String split[] = filename.split("\\.");
		if (split.length >= 2) {
			extention = split[split.length - 1];
		}

		// Log.d(TAG, filename);
		// Log.d(TAG, extention);

		if (extention.equals(FILETYPE_PLAYLIST)) {
			for (Uri uri : parseList(resourceUri)) {
				this.parseAndAddToList(uri);
			}
		} else {
			this.streamUris.add(resourceUri);
		}
	}

	private Vector<Uri> parseList(Uri listUri) throws Exception {
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
			uris.add(child);
			ret.add(child);
		}

		return ret;
	}
	
	int test = 0;
	
	public HashMap<String, Uri> parseQuality(Uri listUri) throws Exception {
		HashMap<String, Uri> qualities = new HashMap<String, Uri>();
		
		test++;
		
		Log.i("QUALITY", "it works, "+test);
		
		String content = downloadContents(listUri);
		
		for(String line : content.split("\n")){
			
			if(!line.trim().contains("BANDWIDTH")){
				continue;
			}
			
			Log.i("QUALITY", "Line: " + line);
			String quality = line.substring(line.lastIndexOf("BANDWIDTH=")+"BANDWIDTH=".length());
			
			Log.i("QUALITY", "Quality: " + quality);
			
			qualities.put(quality, uris.get(test));
			test++;
		}
		
		return qualities;
	}

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

	/*public void startLocalMediaProxy(int listenPort) throws IOException {

		ServerSocket listen = new ServerSocket(listenPort);
		
	}*/
}
