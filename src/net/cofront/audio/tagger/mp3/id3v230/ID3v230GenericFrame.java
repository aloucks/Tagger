package net.cofront.audio.tagger.mp3.id3v230;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.cofront.audio.tagger.Util;

public class ID3v230GenericFrame implements ID3v230Frame {
	private byte[] id = new byte[4];
	private byte[] size = new byte[4];
	private byte[] flags = new byte[2];
	private byte[] data = null;
	
	public static boolean isValidFrameId(byte[] b) {
		// Frame IDs are A-Z0-9 only
		//System.out.println("b[0]="+ ((b[0] > 47 && b[0] < 58) || (b[0] > 64 && b[0] < 91)) );
		return (
			((b[0] > 47 && b[0] < 58) || (b[0] > 64 && b[0] < 91)) &&
			((b[1] > 47 && b[1] < 58) || (b[1] > 64 && b[1] < 91)) &&
			((b[2] > 47 && b[2] < 58) || (b[2] > 64 && b[2] < 91)) &&
			((b[3] > 47 && b[3] < 58) || (b[3] > 64 && b[3] < 91))
		);
	}
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream(id.length + size.length + flags.length + data.length);
		b.write(id);
		b.write(size);
		b.write(flags);
		b.write(data);
		return b.toByteArray();
	}
	/*
	protected ID3v230GenericFrame(byte[] id, byte[] data, byte[] flags) {
		Util.byteCopy(id, 0, 4, this.id, 0);
		//Util.byteCopy(size, 0, 4, this.size, 0);
		this.setFrameData(data);
		Util.byteCopy(flags, 0, 2, this.flags, 0);
	}
	*/
	
	protected ID3v230GenericFrame(byte[] fheader) {
		Util.byteCopy(fheader, 0, 4, this.id, 0);
		Util.byteCopy(fheader, 4, 4, this.size, 0);
		Util.byteCopy(fheader, 8, 2, this.flags, 0);
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
	
	public synchronized void setFrameDataSize(int s) {
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
