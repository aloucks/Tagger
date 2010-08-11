package net.cofront.audio.tagger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ID3v2Frame {
	private byte[] id = new byte[4];
	private byte[] size = new byte[4];
	private byte[] flags = new byte[2];
	private byte[] data = null;
	private String idAsString = null;
	
	public byte[] getBytes() {
		ByteArrayOutputStream b = new ByteArrayOutputStream(id.length + size.length + flags.length + data.length);
		try {
			b.write(id);
			b.write(size);
			b.write(flags);
			b.write(data);
			return b.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	protected ID3v2Frame(byte[] id, byte[] size, byte[] flags) {
		Util.byteCopy(id, 0, 4, this.id, 0);
		Util.byteCopy(size, 0, 4, this.size, 0);
		Util.byteCopy(flags, 0, 2, this.flags, 0);
		this.idAsString = new String(this.id);
	}

	protected synchronized void setFrameData(byte[] data) {
		this.data = data;
		this.setFrameDataSize(this.data.length);
	}
	
	public byte[] getFrameId() {
		return this.id;
	}
	
	public String getFrameIdAsString() {
		return this.idAsString;
	}
	
	public synchronized byte[] getFrameData() {
		return this.data;
	}
	
	protected synchronized void setFrameDataSize(int s) {
		size = Util.intToByteArray(s);
	}
	
	public synchronized int getFrameDataSize() {
		return Util.byteArrayToInt(size);
	}
}
