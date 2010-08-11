package net.cofront.audio.tagger.mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.cofront.audio.tagger.UnexpectedFrameDataException;

public class NewTest {

	/**
	 * @param args
	 * @throws UnexpectedFrameDataException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, UnexpectedFrameDataException {
		File f = new File("workingdir/source1.mp3");
		ID3v2 tag = ID3v230.read(f);
		System.out.println(tag);
	}

}
