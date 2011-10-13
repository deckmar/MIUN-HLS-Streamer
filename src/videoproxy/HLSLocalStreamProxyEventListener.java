package videoproxy;

public interface HLSLocalStreamProxyEventListener {

	void errorNetwork(String msg);
	void errorOther(Exception ex);
	
	void readyForPlaybackNow();
	
}
