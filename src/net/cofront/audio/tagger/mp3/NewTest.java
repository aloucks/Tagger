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
		File f = new File("workingdir/source3.mp3");
		

		long start = System.currentTimeMillis();
		//ID3v2 tag = ID3v230.read(f);
		//tag = ID3v230.read(f);
		//ID3v1.removeTag(f);
		//ID3v230.removeTag(f);
		//long end = System.currentTimeMillis();
		//System.out.println(end - start);
		//System.out.println("tag="+tag);
		//System.out.println("Wtf");
		int b1 = ( 1 << 7 | 1 << 6 | 1 << 5 | 1 << 4 | 1 << 3 | 1 << 2 | 1 << 1 | 1 << 0 ) & 0xff ;
		int b2 = ( 1 << 7 | 1 << 6 | 1 << 5 ) ;
		//System.out.println(b1 + " " + Integer.toBinaryString(b1));
		//System.out.println(b2 + " " + Integer.toBinaryString(b2));
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		byte[] b = new byte[1];
		byte lastbyte = 0;
		int read = 0;
		System.out.println(Integer.toBinaryString(128));
		System.exit(1);
		while (raf.read(b) > -1) {
			if (read % 2048 == 0) {
				//System.out.println(read);
			}
			//if (lastbyte)
			if ( (lastbyte & b1) > 0 && (b2 & b[0]) > 0 ) {
				System.out.println("sync=" + read);
			}
			read++;
		}
		
	}

}
