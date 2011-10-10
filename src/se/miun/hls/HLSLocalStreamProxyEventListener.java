package se.miun.hls;

public interface HLSLocalStreamProxyEventListener {

	void errorNetwork(String msg);
	void errorOther(Exception ex);
	
}
