package net.cofront.audio.tagger.mp3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.cofront.audio.tagger.Util;

public class GenericFrame implements Frame {
	private byte[] id = new byte[4];
	private byte[] size = new byte[4];
	private byte[] flags = new byte[2];
	private byte[] data = null;
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream(id.length + size.length + flags.length + data.length);
		b.write(id);
		b.write(size);
		b.write(flags);
		b.write(data);
		return b.toByteArray();
	}
	
	protected GenericFrame(byte[] id, byte[] data, byte[] flags) {
		Util.byteCopy(id, 0, 4, this.id, 0);
		//Util.byteCopy(size, 0, 4, this.size, 0);
		this.setFrameData(data);
		Util.byteCopy(flags, 0, 2, this.flags, 0);
	}

	public synchronized void setFrameData(byte[] data) {
		this.data = data;
		this.setFrameDataSize(this.data.length);
	}
	
	public byte[] getFrameId() {
		return this.id;
	}
	
	public String getFrameIdAsString() {
		return String.valueOf(id);
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

	public void setFrameId(String frameId) {
		this.id = frameId.getBytes();
	}

	public byte[] getFlags() {
		return this.flags;
	}

	public void setFlags(byte[] flags) {
		this.flags = flags;
	}
}
