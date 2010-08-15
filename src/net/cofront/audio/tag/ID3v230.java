package net.cofront.audio.tag;

public class ID3v230 {
	public final static byte[] TAG_IDENTIFIER = ("ID3").getBytes();
	
	final public static int FLAG_UNSYNCHRONIZED = (1 << 7); // bit7 (1st bit)
	final public static int FLAG_EXTENDEDHEADER = (1 << 6); // bit6 (2nd bit)
	final public static int FLAG_EXPERIMENTAL   = (1 << 5); // bit5 (3rd bit)
	
	final public static int EH_FLAG_CRC         = (1 << 7); // bit7 (1st bit)
	
	byte[] header = new byte[10];  
	byte[] eheader1 = new byte[6]; // size, flags, padding
	byte[] eheader2 = new byte[4]; // crc
	
	/*
	public int[] getVersion() {
		return new int[] { header[3], header[4] };
	}
	
	public void setVersion(int major, int minor) {
		header[3] = (byte)major;
		header[4] = (byte)minor;
	}
	*/
	
	public boolean getFlag(int flag) {
		return (header[5] & flag) > 0 ? true : false;
	}
	
	public void setFlag(int flag, boolean on) {
		if (on) {
			header[5] |= flag;
		}
		else {
			header[5] &= ~flag;
		}
	}
	
	public int getSize() {
		return Util.twentyEightBitByteArrayToInt(new byte[] { header[6], header[7], header[8], header[9] });
	}
	
	public void setSize(int size) {
		byte[] tsize = Util.intToTwentyEightBitByteArray(size);
		Util.byteCopy(tsize, 0, 4, header, 6);
	}
	/*
	public int getExtendedHeaderSize() {
		return Util.byteArrayToInt(new byte[] { eheader[0], eheader[1], eheader[2], eheader[3] });
	}
	*/
	public int getPaddingSize() {
		byte[] psize = new byte[4];
		Util.byteCopy(eheader1, 0, 4, psize, 0);
		return Util.byteArrayToInt(psize);
	}
	/*
	public void setExtendedHeaderSize(int size) {
		byte[] tsize = Util.intToByteArray(size);
		Util.byteCopy(tsize, 0, 4, eheader, 0);
	}

	public boolean getExtendedHeaderFlag(byte flag) {
		return (eheader[4] & flag) > 0 ? true : false;
	}
	
	public void setExtendedHeaderFlag(byte flag, boolean on) {
		if (on) {
			eheader[4] |= flag;
		}
		else {
			eheader[4] &= ~flag;
		}
	}
	*/
	
	public int getCRC() {
		return eheader2[0] | eheader2[1] | eheader2[2] | eheader2[3];
	}
	
	public void setCRC(int crc) {
		byte[] data = Util.intToByteArray(crc);
		Util.byteCopy(data, 0, 4, eheader2, 0);
	}
	
}
