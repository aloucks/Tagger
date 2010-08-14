package junk.tagger.mp3.id3v230;

import java.io.IOException;

public interface ID3v230Frame {
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
	
	public int getFrameDataSize();
	
	public void setFrameDataSize(int size);
	
	public byte[] getFrameId();
	
	public String getFrameIdAsString();
	
	public void setFrameId(String frameId);
	
	public byte[] getFlags();
	
	public void setFlags(byte[] flags);	
}
