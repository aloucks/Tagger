package net.cofront.audio.tag;

public class ID3v230 {
	public final static byte[] TAG_IDENTIFIER = ("ID3").getBytes();
	final public static byte FLAG_UNSYNCHRONIZED = (1 << (7 - 1)); // bit7 (1st bit)
	final public static byte FLAG_EXTENDEDHEADER = (1 << (6 - 1)); // bit6 (2nd bit)
	final public static byte FLAG_EXPERIMENTAL   = (1 << (5 - 1)); // bit5 (3rd bit)
	
	byte[] header = new byte[10];
	byte[] eheader = new byte[10];
	
	public int getMajorVersion() {
		return header[3];
	}
	
	public void setMajorVersion(int version) {
		header[3] = (byte)version;
	}
	
	public int getMinorVersion() {
		return header[4];
	}
	
	public void setMinorVersion(int version) {
		header[4] = (byte)version;
	}
	
	public boolean getHeaderFlag(byte flag) {
		return (header[5] | ~flag) == 0xffffffff ? true : false;
	}
	
	public void setHeaderFlag(byte flag, boolean on) {
		if (on) {
			header[5] = (byte)(header[5] | flag);
		}
		else {
			header[5] = (byte)(header[5] & ~flag);
		}
	}
	
	public int getTagSize() {
		return Util.twentyEightBitByteArrayToInt(new byte[] { header[6], header[7], header[8], header[9] });
	}
	
	public void setTagSize(int size) {
		byte[] tsize = Util.intToTwentyEightBitByteArray(size);
		Util.byteCopy(tsize, 0, 4, header, 6);
	}
	
	public int getExtendedHeaderSize() {
		return Util.byteArrayToInt(new byte[] { eheader[0], eheader[1], eheader[2], eheader[3] });
	}
	
	public void setExtendedHeaderSize(int size) {
		byte[] tsize = Util.intToByteArray(size);
		Util.byteCopy(tsize, 0, 4, eheader, 0);
	}
}
