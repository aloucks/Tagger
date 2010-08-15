package net.cofront.audio.tag;

import java.nio.ByteBuffer;

public class ID3v230Frame {
	
	public static final int FLAG_TAG_ALTER_PRESERVE = (1 << 15);
	public static final int FLAG_FILE_ALTER_PRESERVE = (1 << 14);
	public static final int FLAG_READONLY = (1 << 13);
	public static final int FLAG_COMPRESSION = (1 << 7);
	public static final int FLAG_ENCRYPTION = (1 << 6);
	public static final int FLAG_GROUP = (1 << 5);
	
	private byte[] fheader = new byte[10];
	private byte[] feheader = new byte[6];
	private byte[] fdata = null;
	
	public byte[] getFrameId() {
		/*
		byte[] frameId = new byte[4];
		Util.byteCopy(fheader, 0, 4, frameId, 0);
		return frameId;
		*/
		return ByteBuffer.wrap(fheader, 0 ,4).array();
		
	}
	
	public int getSize() {
		byte[] size = new byte[4];
		Util.byteCopy(fheader, 4, 4, size, 0);
		return Util.byteArrayToInt(size);
	}
	
	public void setSize(int size) {
		byte[] tsize = Util.intToTwentyEightBitByteArray(size);
		Util.byteCopy(tsize, 4, 4, fheader, 4);
	}
	
	public boolean getFlag(int flag) {
		int flags = (fheader[8] << 8) | (fheader[9]);
		return (flags & flag) > 0;
	}
	
	public void setFlag(int flag) {
		int flags = (fheader[8] << 8) | (fheader[9]);
		flags |= flag;
		byte[] tmp = new byte[4];
		Util.intToByteArray(flags);
		fheader[8] = tmp[0];
		fheader[9] = tmp[1];
	}
	
	public int getDataOffset() {
		int offset = 0;
		if (getFlag(FLAG_COMPRESSION)) {
			offset += 4;
		}
		if (getFlag(FLAG_ENCRYPTION)) {
			offset += 1;
		}
		if (getFlag(FLAG_GROUP)) {
			offset += 1;
		}
		return offset;
	}
	
	public byte[] getData() {
		int offset = this.getDataOffset();
		ByteBuffer bb = ByteBuffer.wrap(fdata, offset, fdata.length - offset);
		return bb.array();
	}

	
	public void setData(byte[] data) {
		fdata = data;
		setSize(data.length);
	}
}
