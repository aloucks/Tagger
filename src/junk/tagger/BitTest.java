package junk.tagger;

import java.util.BitSet;

public class BitTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		char c = 'A';
		System.out.println( (byte)c );
		
		byte b = 64;
		System.out.println( (char)b );
		
		//b = 0;
		System.out.println( b );
		//b = (1 << 8);
		System.out.println( b );
		System.out.println( Integer.toBinaryString(b) );
		*/
		
		for (byte b=64; b<70; b++) {
			System.out.println( b + ": " + Integer.toBinaryString(b) );
		}
		System.out.println();
		byte b = 0;
		byte mask = 32;
		//b = (byte) (b ^ 1);
		//b = (byte) (b ^ 8);
		b = (byte) (b ^ 64);
		//b = (byte) (b ^ 32);
		//b = (byte) (b ^ 32);
		b = (byte) (b ^ 32 );
		//int x = b & 8;
		b = 0;
		//b ^= ( 1 << (7 - (8 - 7) ) );
		//b ^= ( 1 << (7 - (8 - 6) ) );
		int bit = 7;
		b = (byte) (8 * (bit+1));
		System.out.println( b + ": " + Integer.toBinaryString(b) );
		//System.out.println("x="+x);
		
		System.out.println(Integer.toHexString(257));
		
		//int size = 257;
		byte[] size = new byte[] { 0x00, 0x00, 0x02, 0x01 };
		b = 0;
		int z = 0;
		
		/*
		for (int i=0; i<size.length; i++) {
			byte s = size[i];
			for (int x=1; x<=4; x++) {
				z |= (s * (int)Math.pow(x, 2));
				System.out.println( z + ": " + Integer.toBinaryString(z) );
			}
			
			//System.out.println( "size[" + i + "] = " + Integer.toBinaryString(size[i]) );
		}
		*/
		
		z = ( size[0] << 21 | size[1] << 14 | size[2] << 7 | size[3] );
		
		System.out.println("z = " + z);
		System.out.println("zhex = " + Integer.toHexString(z));
		
		byte test = (byte)(8 * (1 + 7));
		System.out.println("test = " + test + ", hex = " + Integer.toHexString(test));
		test = 0;
		// 87654321
		test = (1 << (7 - 1) );
		System.out.println("test = " + test + ", hex = " + Integer.toHexString(test));
		byte b1 = ( 1 << 6 | 1 << 5 | 1 << 4 | 1 << 3 | 1 << 2 | 1 << 1 | 1 << 0);
		b1 = 1 << 6 | 1 << 5 | 1 << 4;
		System.out.println("b1 = " + Integer.toBinaryString(b1));
		System.out.println("b2 = " + Integer.toBinaryString(Byte.MAX_VALUE));
	}

}
