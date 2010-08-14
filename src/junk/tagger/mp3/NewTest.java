package junk.tagger.mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import junk.tagger.Test;
import junk.tagger.mp3.id3v230.ID3v230;
import junk.tagger.mp3.id3v230.ID3v230Frame;


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
			ID3v230.removeTag(files[i]);
			ID3v1.removeTag(files[i]);
			/*
			ArrayList<ID3v230Frame> frames1 = (ArrayList<ID3v230Frame>) tag.getFrames("TALB");
			frames1.get(0).setFrameData("\0Album Name".getBytes());
			ArrayList<ID3v230Frame> frames2 = (ArrayList<ID3v230Frame>) tag.getFrames("TPE1");
			frames2.get(0).setFrameData("\0ArtistName".getBytes());
			ArrayList<ID3v230Frame> frames3 = (ArrayList<ID3v230Frame>) tag.getFrames("TIT2");
			frames3.get(0).setFrameData("\0Track name".getBytes());
			ArrayList<ID3v230Frame> frames4 = (ArrayList<ID3v230Frame>) tag.getFrames("XSOP");
			frames4.get(0).setFrameData("\0ArtistName".getBytes());
			//tag.setFrames(, frames)
			
			tag.write(files[i], true);
			*/
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
