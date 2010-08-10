package net.cofront.audio.tagger;

public class SizeTest2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] size1 = { 0x09, 0x03, 0x5f, 0x04 };
		
		int s01 = Util.byteArrayToInt(size1);
		int s1 = byteArrayTo28bitInt(size1);
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size1[0])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size1[1])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size1[2])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size1[3])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s1)));
		System.out.println(s1);
		System.out.println(s01);
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s01)));
		
		byte[] size02 = Util.intToByteArray(s01);
		int s02 = Util.byteArrayToInt(size02);
		
		byte[] size2 = x28bitIntToByteArray(s1);
		int s2 = byteArrayTo28bitInt(size2);
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[0])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[1])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[2])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[3])));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s2)));
		
		System.out.println(s2);
		System.out.println(s02);
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s02)));
		

	}
	
	public static final byte[] x28bitIntToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 21),
                (byte)(value >>> 14),
                (byte)(value >>> 7),
                (byte)value};
	}
	
	public static final int byteArrayTo28bitInt(byte [] b) {
        return     (b[0] << 21)
                | ((b[1] & 0xFF) << 14)
                | ((b[2] & 0xFF) << 7)
                | ( b[3] & 0xFF);
	}

}
