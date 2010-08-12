package net.cofront.audio.tagger;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cofront.audio.tagger.mp3.ID3v1;
import net.cofront.audio.tagger.mp3.ID3v2;

public class Tagger {

	protected static CloserThread closer = new CloserThread();

	public void startCloser() {
		closer.start();
		closer.setDaemon(true);
	}
	
	public boolean isCloserRunning() {
		return closer.isAlive();
	}
	
	public static void close(Closeable c) {
		if (! closer.isAlive()) {
			closer.setDaemon(true);
			closer.start();
		}
	}
	
	public Tag[] read(File f) throws FileNotFoundException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		List<Class<Tag>> tclasses = getTagClasses(f);
		List<Tag> tags = new ArrayList<Tag>();
		Iterator<Class<Tag>> i = tclasses.iterator();
		Method m = null;
		while (i.hasNext()) {
			try {
				raf.seek(0);
				m = i.next().getDeclaredMethod("read", RandomAccessFile.class);
				tags.add((Tag)m.invoke(null, raf));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return (Tag[])tags.toArray();
	}
	
	public List<Class<Tag>> getTagClasses(File f) {
		List clist = new ArrayList();
		String ext = f.getName()
			.toLowerCase()
			.substring(f.getName().length()-3);
		if (ext.equals("mp3")) {
			clist.add(ID3v2.class);
			clist.add(ID3v1.class);
		}
				
		return clist;
	}
}
