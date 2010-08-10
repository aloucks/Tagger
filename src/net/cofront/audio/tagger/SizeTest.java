package net.cofront.audio.tagger;

public class SizeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//byte[] size1 = { 0x00, 0x00, 0x02, 0x01 };
		
		byte[] size1 = { 0x23, 0x03, 0x5f, 0x04 };
		
		int s3 = size1[0] << 24 | size1[1] << 16 | size1[2] << 8 | size1[3];
		//int s1 = size1[0] << 21 | size1[1] << 14 | size1[2] << 7 | size1[3];
		//int s1 = size1[0] << 24 | size1[1] << 16 | size1[2] << 8 | size1[3];
		//printHexBytes(size1);
		//System.out.println("s1 = " + s1 + ", hex = " + Integer.toHexString(s1));
		
		byte[] size2 = new byte[4];
		size2[0] = 0;
		size2[1] = 0;
		size2[2] = 0;
		size2[3] = 0;
	
		size2[0] = (byte) (s3 >> 24);
		size2[1] = (byte) (s3 >> 16); 
		size2[2] = (byte) (s3 >> 8);
		size2[3] = (byte) (s3);

		//System.out.println("---");
		/*
		System.out.println(Integer.toBinaryString(size1[0] << 21));
		System.out.println(Integer.toBinaryString(size1[1] << 14));
		System.out.println(Integer.toBinaryString(size1[2] << 7));
		System.out.println(Integer.toBinaryString(size1[3]));
		*/
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[0])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[1])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[2])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[3])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(s1)));
		//System.out.println("---");
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s3)));
		
		int s2 = size2[0] << 24 | size2[1] << 16 | size2[2] << 8 | size2[3];
		//int s2 = size2[0] << 21 | size2[1] << 14 | size2[2] << 7 | size2[3];
		//printHexBytes(size2);
		//System.out.println("s2 = " + s2 + ", hex = " + Integer.toHexString(s2));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s2)));
		
		
		System.out.println("---");
// --------------
		
		s3 = size1[0] << 21 | size1[1] << 14 | size1[2] << 7 | size1[3];
		//int s1 = size1[0] << 21 | size1[1] << 14 | size1[2] << 7 | size1[3];
		//int s1 = size1[0] << 24 | size1[1] << 16 | size1[2] << 8 | size1[3];
		//printHexBytes(size1);
		//System.out.println("s1 = " + s1 + ", hex = " + Integer.toHexString(s1));
		
		size2 = new byte[4];
		size2[0] = 0;
		size2[1] = 0;
		size2[2] = 0;
		size2[3] = 0;
	
		//size2[0] = (byte) (s3 >> 21);
		//size2[1] = (byte)(s3 >> 14); 
		size2[2] = (byte)(s3 >> 7);
		//size2[3] = (byte) (s3);

		//System.out.println("---");
		/*
		System.out.println(Integer.toBinaryString(size1[0] << 21));
		System.out.println(Integer.toBinaryString(size1[1] << 14));
		System.out.println(Integer.toBinaryString(size1[2] << 7));
		System.out.println(Integer.toBinaryString(size1[3]));
		*/
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[0])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[1])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[2])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(size2[3])));
		//System.out.println(String.format("%1$32s", Integer.toBinaryString(s1)));
		//System.out.println("---");
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s3)));
		
		s2 = size2[0] << 21 | size2[1] << 14 | size2[2] << 7 | size2[3];
		//int s2 = size2[0] << 21 | size2[1] << 14 | size2[2] << 7 | size2[3];
		//printHexBytes(size2);
		//System.out.println("s2 = " + s2 + ", hex = " + Integer.toHexString(s2));
		System.out.println(String.format("%1$32s", Integer.toBinaryString(s2)));
		
		

	}
	
	public static void printHexBytes(byte[] bytes) {
		for(int i=0; i<bytes.length; i++) {
			System.out.print(Integer.toHexString(bytes[i]) + " ");
		}
		System.out.println();
	}
}
