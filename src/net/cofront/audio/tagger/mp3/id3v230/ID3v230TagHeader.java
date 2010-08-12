package net.cofront.audio.tagger.mp3.id3v230;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.cofront.audio.tagger.Util;
import net.cofront.audio.tagger.mp3.ID3v2;

public class ID3v230TagHeader {
	
	final private ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
	
	protected byte[] version; // = new byte[2];
	protected byte   flags;	// = (1 byte)
	protected byte[] size;	// = new byte[4];
	
	protected ID3v230TagHeader() {
		version = new byte[] { 0x03, 0x00 };
		flags = 0x00;
		size = new byte[] { 0x00, 0x00, 0x00, 0x00 };
	}
	
	protected ID3v230TagHeader(byte[] rawheader) {
		this();
		Util.byteCopy(rawheader, 3, 2, version, 0);
		flags = rawheader[5];
		Util.byteCopy(rawheader, 6, 4, size, 0);
	}
	
	protected ID3v230TagHeader(byte[] version, byte flags, int size) {
		this(version, flags, Util.intToTwentyEightBitByteArray(size));
	}
	
	protected ID3v230TagHeader(byte[] version, byte flags, byte[] size) {
		this.version = version;
		this.size = size;
		this.flags = flags;
	}
	
	/**
	 * 
	 * @return byte[] { major, minor }
	 */
	public byte[] getVersion() {
		return version;
	}
	
	/**
	 * ID3v2.major.minor
	 * @param major
	 * @param minor
	 */
	protected void setVersion(byte major, byte minor) {
		version[0] = major;
		version[1] = minor;
	}
	
	public byte[] getTagSizeBytes() {
		return size;
	}
	
	/**
	 * Does not include the size of the header (10 bytes).
	 * Total tag size: getTagSize() + 10 + padding
	 * @return
	 */
	public int getTagSize() {
		return Util.twentyEightBitByteArrayToInt(size);
	}
	
	//public int getTagSizeTotal() {
	//	return getTagSize() + 10;
	//}
	
	protected void setTagSize(int size) {
		this.size = Util.intToTwentyEightBitByteArray(size);
	}
	
	/**
	 * Should be encoded with Util.intToTwentyEightBitByteArray().
	 * @param size
	 */
	protected void setTagSize(byte[] size) {
		this.size = size;
	}

	public byte getFlags() {
		return flags;
	}
	
	protected void setFlags(byte flags) {
		this.flags = flags;
	}
	
	public boolean getFlag(byte flag) {
		return (flags & flag) > 0 ? true : false;
	}
	
	protected void setFlag(byte flag, boolean b) {
		if (b) {
			flags = (byte)(flags | flag);
		}
		else {
			flags = (byte)(flags ^ flag);
		}
	}
	
	public synchronized byte[] getBytes() throws IOException {
		bos.reset();
		bos.write(ID3v2.TAG_IDENTIFIER);
		bos.write(version);
		bos.write(flags);
		bos.write(size);
		return bos.toByteArray();
	}
}
