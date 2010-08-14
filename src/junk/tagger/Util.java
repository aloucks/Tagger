package junk.tagger;

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
				System.err.println("slen="+source.length+", soff="+i + ", dlen="+dest.length + ", doff="+doffset);
				e.printStackTrace();
			}
			doffset++;
			
		}
	}
	
	public static byte[] fillCopy(byte[] data, int len, byte filler) {
		byte[] tmp = new byte[len];
		int dlen = data.length;
		for(int i=0; i<len; i++) {
			if (i < dlen) {
				tmp[i] = data[i];
			}
			else {
				tmp[i] = filler;
			}
		}
		return tmp;
	}
	
	/**
	 * Pushes a byte onto the ride side (block[block.length - 1])
	 * of a byte array. The first byte of the array (block[0]) 
	 * is discarded.
	 * @param b
	 * @param block
	 * @return
	 */
	public static byte[] rpush(byte b, byte[] block) {
		int len = block.length;
		for (int i=1; i<len; i++) {
			block[i-1] = block[i];
		}
		block[len - 1] = b;
		return block;
	}
	
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
	        (byte)(value >>> 24),
	        (byte)(value >>> 16),
	        (byte)(value >>> 8),
	        (byte) value
	    };
	}
	
	public static final int byteArrayToInt(byte [] b) {
        return (
        	   (b[0] << 24)
	        | ((b[1] & 0xFF) << 16)
	        | ((b[2] & 0xFF) << 8)
	        | ( b[3] & 0xFF)
	    );
	}
	
	/**
	 * The ID3v2 tag size is encoded with four bytes where the most
	 * significant bit (bit 7) is set to zero in every byte, making a total
	 * of 28 bits. The zeroed bits are ignored, so a 257 bytes long tag is
	 * represented as $00 00 02 01.
	 * @param value integer less than 28 bits
	 * @return
	 */
	public static final byte[] intToTwentyEightBitByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 21),
            (byte)(value >>> 14),
            (byte)(value >>> 7),
            (byte)value
        };
	}
	
	/**
	 * The ID3v2 tag size is encoded with four bytes where the most
	 * significant bit (bit 7) is set to zero in every byte, making a total
	 * of 28 bits. The zeroed bits are ignored, so a 257 bytes long tag is
	 * represented as $00 00 02 01.
	 * @param b byte[2]
	 * @return
	 */
	public static final int twentyEightBitByteArrayToInt(byte [] b) {
        return (
        	   (b[0] << 21)
        	| ((b[1] & 0xFF) << 14)
        	| ((b[2] & 0xFF) << 7)
        	| ( b[3] & 0xFF)
        );
	}
}
