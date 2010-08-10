package net.cofront.audio.tagger;

public class Util {
	public static byte setBit(byte b, int bit) {
		b ^= ( 1 << (7 - (8 - bit) ) );
		return b;
	}
	
	public static void byteCopy(byte[] source, int soffset, int slength, byte[] dest, int doffset) {
		for (int i=soffset; i<(soffset+slength); i++) {
			//System.out.println("doff="+doffset + ", soff="+i);
			try {
				dest[doffset] = source[i];
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("doff="+doffset + ", soff="+i);
				e.printStackTrace();
			}
			doffset++;
			
		}
	}
	
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	
	public static final int byteArrayToInt(byte [] b) {
        return     (b[0] << 24)
                | ((b[1] & 0xFF) << 16)
                | ((b[2] & 0xFF) << 8)
                | ( b[3] & 0xFF);
	}
	
	public static final byte[] x28bitIntToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 21),
                (byte)(value >>> 14),
                (byte)(value >>> 7),
                (byte)value};
	}
	
	public static final int x28bitByteArrayToInt(byte [] b) {
        return     (b[0] << 21)
                | ((b[1] & 0xFF) << 14)
                | ((b[2] & 0xFF) << 7)
                | ( b[3] & 0xFF);
	}


}
