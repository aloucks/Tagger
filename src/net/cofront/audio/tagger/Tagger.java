package net.cofront.audio.tagger;

import java.io.Closeable;

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
}
