package net.cofront.audio.tagger.mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.cofront.audio.tagger.mp3.id3v230.ID3v230;

public class NewTest {

	/**
	 * @param args
	 * @throws ID3v2Exception 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ID3v2Exception {
		File f = new File("testfiles/mp3/source1.mp3");
		long start = System.currentTimeMillis();
		ID3v2 tag = ID3v230.read(f);
		tag = ID3v230.read(f);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		System.out.println("tag="+tag);
		System.out.println("Wtf");
	}

}
