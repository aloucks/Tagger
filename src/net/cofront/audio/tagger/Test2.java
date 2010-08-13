package net.cofront.audio.tagger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.cofront.audio.tagger.CloserThread;
import net.cofront.audio.tagger.mp3.ID3v1;
import net.cofront.audio.tagger.mp3.ID3v2Exception;

public class Test2 {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ID3v2Exception 
	 */
	public static void main(String[] args) throws IOException, ID3v2Exception {
		System.out.print("Copying files to working dir.. ");
		CloserThread cThread = new CloserThread();
		cThread.start();
		
		File workingdir = new File("workingdir");
		File testdir = new File("testfiles/mp3");
		Test.copyFiles(testdir, workingdir);
		
		File file[] = workingdir.listFiles();
		
		cThread.isRunning = false;
		
		System.out.println("done.");
		
		long start = System.currentTimeMillis();
		for (int i=0; i<file.length; i++) {
			RandomAccessFile raf = new RandomAccessFile(file[i], "rw");
			ID3v2 v2tag = ID3v2.read(raf);
			v2tag.write(raf);
			v2tag = ID3v2.read(raf);
			ID3v1 v1tag = ID3v1.read(raf);
			System.out.println(v2tag.getFrames());
			cThread.push(raf);
		}
		long stop = System.currentTimeMillis();
		System.out.println(stop - start + " ms");
		

	}

}
