package net.cofront.audio.tagger.mp3;

import java.io.IOException;

public interface Frame {
	/**
	 * Return the entire frame as raw bytes.
	 * @return
	 */
	public byte[] getBytes() throws IOException;
	
	/**
	 * Return the frame payload as raw bytes.
	 * @return
	 */
	public byte[] getFrameData();
	
	public void setFrameData(byte[] data);
	
	public byte[] getFrameId();
	
	public String getFrameIdAsString();
	
	public void setFrameId(String frameId);
	
	public byte[] getFlags();
	
	public void setFlags(byte[] flags);
	
}
