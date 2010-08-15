package net.cofront.audio.tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(Integer.toBinaryString(ID3v230.EH_FLAG_CRC));
		System.out.println(Integer.toBinaryString(0xffffffff & 0xff));
		for (byte i=Byte.MIN_VALUE; i<Byte.MAX_VALUE; i++) {
			String b = Integer.toBinaryString(i);
			if (b.equals("10000000")) {
				System.out.println(i);
				System.out.println(Integer.toHexString(i));
				System.out.println(b);
			
			}
		}
		File f = new File("workingdir/test.txt");
		FileOutputStream out = new FileOutputStream(f);
		//out.write( (1 << 7) );
		out.write( Integer.MAX_VALUE );
		out.close();
		
		FileInputStream in = new FileInputStream(f);
		byte[] b = new byte[1];
		in.read(b);
		System.out.println(Integer.toHexString(b[0]));
		System.out.println(Integer.toBinaryString(b[0]));
		System.out.println(b[0]);
		//b[0] = -128;
		//System.out.println(Byte.MIN_VALUE);
		
	}

}
