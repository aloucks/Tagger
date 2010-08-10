package net.cofront.audio.tagger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 
 */
public class ID3v2Header {
	final public static byte[] ID3 = ("ID3").getBytes();

	final public static byte MASK_UNSYNC = (1 << (7 - 1)); // bit7 (1st bit)
	final public static byte MASK_EXTEND = (1 << (6 - 1)); // bit6 (2nd bit)
	final public static byte MASK_EXPERI = (1 << (5 - 1)); // bit5 (3rd bit)
										
	private byte[] identifier = new byte[3];
	private byte[] version = new byte[2];
	private byte[] flags = new byte[1];
	private byte[] size = new byte[4];
	
	public ID3v2Header() {
		this(
			new byte[] {
				'I','D','3',			
				0x03,0x00,				
				0x00,
				0x00,0x00,0x00,0x00,
			}
		);
	}
	
	protected ID3v2Header(byte[] h) {
		Util.byteCopy(h, 0, 3, identifier, 0);
		Util.byteCopy(h, 3, 2, version, 0);
		Util.byteCopy(h, 5, 1, flags, 0);
		Util.byteCopy(h, 6, 4, size, 0);
	}
	
	public byte[] getVersionBytes() {
		return new byte[] { version[0], version[1] };
	}
	
	public String getVersion() {
		return new String((int)version[0] + "." + (int)version[1]);
	}
	
	public String getFullVersion() {
		return "2." + getVersion();
	}
	
	
	public byte getFlags() {
		return flags[0];
	}
	
	protected void setFlags(byte b) {
		flags[0] = b;
	}
	
	public boolean hasFlagUnsyncronized() {
		return (flags[0] & MASK_UNSYNC) > 0 ? true : false;
	}
	
	public boolean hasFlagExtendedHeader() {
		return (flags[0] & MASK_EXTEND) > 0 ? true : false;
	}
	
	public boolean hasFlagExperimental() {
		return (flags[0] & MASK_EXPERI) > 0 ? true : false;
	}
	
	/**
	 * The ID3v2 tag size is the size of the complete tag after unsychronisation, 
	 * including padding, excluding the header but not excluding the extended 
	 * header (total tag size - 10). Only 28 bits (representing up to 256MB) are used 
	 * in the size description to avoid the introducuction of 'false syncsignals'. 
	 * @return
	 */
	public int getTagSize() {
		//return ( size[0] << 21 | size[1] << 14 | size[2] << 7 | size[3] );
		return Util.twentyEightBitByteArrayToInt(size);
	}
	
	public int getTotalTagSize() {
		return Util.twentyEightBitByteArrayToInt(size) + 10;
	}
	
	protected void setTagSize(int s) {
		/*
		size[0] = (byte)(s >> 21);
		size[1] = (byte)(s >> 14);
		size[2] = (byte)(s >> 7);
		size[3] = (byte)(s);
		*/
		size = Util.intToTwentyEightBitByteArray(s);
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			b.write(identifier);
			b.write(version);
			b.write(flags);
			b.write(size);
			return b.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
