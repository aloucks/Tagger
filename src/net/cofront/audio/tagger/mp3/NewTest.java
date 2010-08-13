package net.cofront.audio.tagger.mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.cofront.audio.tagger.Test;
import net.cofront.audio.tagger.mp3.id3v230.ID3v230;

public class NewTest {

	/**
	 * @param args
	 * @throws ID3v2Exception 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ID3v2Exception, InterruptedException {
		Test.copyFiles(new File("testfiles/mp3"), new File("workingdir"));
		File f = new File("workingdir/source5.mp3");
		
		ID3v230.removeTag(f);
		ID3v1.removeTag(f);
		System.exit(1);
		long start = System.currentTimeMillis();
		ID3v230 tag = ID3v230.read(f);
		//tag = ID3v230.read(f);
		//ID3v1.removeTag(f);
		//ID3v230.removeTag(f);
		//long end = System.currentTimeMillis();
		//System.out.println(end - start);
		//System.out.println("tag="+tag);
		int tsize = tag.getTagSize() + 10;
		System.out.println("tsize=" + tsize );
		int b1 = ( 1 << 7 | 1 << 6 | 1 << 5 | 1 << 4 | 1 << 3 | 1 << 2 | 1 << 1 | 1 << 0 );// & 0xff ;
		byte b2 = ( 1 << 6 | 1 << 5 | 1 << 4 ) ;
		//System.out.println(b1 + " " + Integer.toBinaryString(b1));
		//System.exit(1);
		//System.out.println(b2 + " " + Integer.toBinaryString(b2)); System.exit(1);
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		byte[] b = new byte[1];
		byte lastbyte = 0;
		int read = 0;
		//System.out.println(Integer.toBinaryString(((byte)-32))); System.exit(1);
		int r =0;
		//System.out.println( (b2 >> 5) == 0x07 );
		//System.out.println( (b1 ^ 0xff) );
		//System.out.println(Integer.toHexString(-1));
		//System.out.println((0xff ^ b1) + " " + (0xff & 0x00) ); System.exit(1);
		//System.out.println( Integer.toHexString(0xffffffff) + " " + Integer.toHexString(b2 >> 5) ); System.exit(1);
		
		while ( (r = raf.read(b)) > -1) {
			//System.out.println(Integer.toBinaryString(lastbyte) + " " + Integer.toBinaryString(b[0]));
			
			if ( (lastbyte ^ b1) == 0 && (b2 >> 5) == 0x07 ) {
				//System.out.println("sync=" + read);
				//System.exit(1);
			}
			
			if ( ( b[0] & (1 << 7) ) != 0 ) {
				//System.out.println(Integer.toBinaryString(b[0] & 0xff));
			}
			int t = (b[0] >> 5);
			if ( t == 7 ){
				//System.out.println(t + " " + Integer.toBinaryString( (b[0] ) ));
			}
			if (lastbyte == 0xffffffff && (b[0] >> 5) == 0x07) {
				System.out.println("sync_start="+(read-1));
				if (tsize < read-1) {
					System.exit(1);
				}
			}
			/*
			if (( b[0] & (1 << 7)) > 0 ) {
				if (( b[0] & (1 << 6)) > 0 ) {
					if (( b[0] & (1 << 5)) > 0 ) {
						//System.out.println("7 and 6 and 5 are set");
						//System.out.println(Integer.toBinaryString(b[0]));
						byte foo = (byte)( b[0] >>> 5 );
						System.out.println( Integer.toBinaryString( (foo & 0x07) ) ); 
						boolean match = (((b[0] >>> 5) & 0x07) == 0x07);
						System.out.println( match );
						//System.out.println( Integer.toBinaryString( bar )); System.exit(1);
						System.out.println( foo + " " + Integer.toBinaryString( foo ) ); System.exit(1);
						if ( lastbyte == 0xff ) {
							System.out.println( "sync!" );
						}
					}
				}
				
			}
			*/
			if ( (((b[0] >>> 5) & 0x07) == 0x07) && lastbyte == 0xffffffff ) {
				//System.out.println(Integer.toHexString(lastbyte));
				if (read >= tsize) {
					System.out.println( Integer.toBinaryString(lastbyte & 0xff) + " " + Integer.toBinaryString(b[0] & 0xff) );
					System.out.println( read ); 
					System.exit(1);
				}
				
			}
			//System.out.println(Integer.toBinaryString(b[0]));
			lastbyte = b[0];
			read++;
			
		}
		
	}

}
