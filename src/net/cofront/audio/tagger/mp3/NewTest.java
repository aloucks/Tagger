package net.cofront.audio.tagger.mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.cofront.audio.tagger.Test;
import net.cofront.audio.tagger.mp3.id3v230.ID3v230;
import net.cofront.audio.tagger.mp3.id3v230.ID3v230Frame;

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
		File dir = new File("workingdir");
		File[] files =  dir.listFiles();
		//File f = new File("workingdir/source5.mp3");
		
		long start = System.currentTimeMillis();
		for (int i=0; i<files.length; i++) {
			System.out.println(files[i].getName());
			ID3v230 tag = ID3v230.read(files[i]);
			//ID3v230.removeTag(files[i]);
			//ID3v1.removeTag(files[i]);
			ArrayList<ID3v230Frame> frames = (ArrayList<ID3v230Frame>) tag.getFrames("TIT2");
			frames.get(0).setFrameData("\0WTF".getBytes());
			tag.setFrames("TIT2", frames);
			tag.write(files[i], true);
			
			break;
		}
		long stop = System.currentTimeMillis();
		System.out.println( (stop - start) );
		/*
		if ( (((b[0] >>> 5) & 0x07) == 0x07) && lastbyte == 0xffffffff ) {
			//System.out.println(Integer.toHexString(lastbyte));
			if (read >= tsize) {
				System.out.println( Integer.toBinaryString(lastbyte & 0xff) + " " + Integer.toBinaryString(b[0] & 0xff) );
				System.out.println( read ); 
				System.exit(1);
			}
			
		}
		*/	
	}
}
