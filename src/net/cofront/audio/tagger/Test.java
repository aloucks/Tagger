package net.cofront.audio.tagger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import net.cofront.audio.tagger.mp3.ID3v1;
import net.cofront.audio.tagger.mp3.ID3v2Exception;

public class Test {
	public static String NL = System.getProperty("line.separator");
	public static byte[] id3v1 = ("TAG").getBytes();
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ID3v2Exception 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ID3v2Exception {
		
		byte[] source = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		byte[] dest = new byte[3];
		
		//Util.byteCopy(source, 2, 3, dest, 0);
		//System.out.println(new String(dest)); System.exit(1);
		
		CloserThread cThread = new CloserThread();
		cThread.start();
		
		File workingdir = new File("workingdir");
		File testdir = new File("testfiles/mp3");
		copyFiles(testdir, workingdir);
		
		File file[] = workingdir.listFiles();
		
		RandomAccessFile[] rafArray = new RandomAccessFile[file.length];
		cThread.lowPriority = true;
		long start = System.currentTimeMillis();
		for (int i=0; i<file.length; i++) {
			File f = file[i];
			//System.out.println(f.getCanonicalPath());
			rafArray[i] = new RandomAccessFile(f, "rw");
		}
		
		ID3v1[] id3v1 = ID3v1.read(rafArray);
		for(int i=0; i<id3v1.length; i++) {
			//System.out.println(file[i].getName() + ": " + id3v1[i]);
			if (id3v1[i] != null) {
				id3v1[i].write(rafArray[i]);
			}
			else {
				id3v1[i] = new ID3v1();
				id3v1[i].title = "Unknown Title";
				id3v1[i].artist = "Unknown Artist";
				id3v1[i].write(rafArray[i]);
				
			}
			id3v1[i] = ID3v1.read(rafArray[i]);
			//System.out.println(file[i].getName() + ": " + id3v1[i]);
			ID3v2 id3v2 = null;
			try {
				System.out.println( "\n" + file[i].getName());
				id3v2 = ID3v2.read(rafArray[i]);
			} catch (ID3v2Exception e) {
				e.printStackTrace();
				cThread.isRunning = false;
			}
			List frames = id3v2.getFrames();
			Iterator fi = frames.iterator();
			while (fi.hasNext()) {
				ID3v2Frame f = (ID3v2Frame) fi.next();
				String fdata = new String(f.getFrameData());
				//System.out.println("fdata[" + f.getFrameIdAsString() + "] = " + fdata);
			}
			cThread.push(rafArray[i]);
		}

		
		
		//raf.close();

		rafArray = null;
		
		long end = System.currentTimeMillis();
		cThread.lowPriority = false;
		System.out.println(end - start + " ms");
		//new Thread(cThread).start();
		Thread.currentThread().sleep(10000);
		System.out.println("Ending");
		cThread.isRunning = false;
		
		
		

	}
	
	public static void copyFiles(File srcdir, File destdir) throws IOException {
		File src[] = srcdir.listFiles();
		FileInputStream fis = null;
		FileOutputStream fos = null;
		for (int i=0; i<src.length; i++) {
			fis = new FileInputStream(src[i]);
			fos = new FileOutputStream(destdir.getAbsolutePath() + "/" + src[i].getName());
			byte[] b = new byte[4096*128];
			int r = -1;
			while ( (r = fis.read(b)) != -1 ) {
				fos.write(b, 0, r);
			}
			fis.close();
			fos.close();
		}
		
	}
	
}


