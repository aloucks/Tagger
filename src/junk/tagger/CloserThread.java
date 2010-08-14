package junk.tagger;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

public class CloserThread extends Thread {
	protected boolean isRunning = true;
	protected boolean lowPriority = false;
	private Stack<Closeable> stack = new Stack(); 
	public void run() {
		Closeable c = null;
		while (isRunning) {
			c = this.pop();
			try {
				if (c != null) {
					if (lowPriority == true) {
						//System.out.println("Sleeping..");
						Thread.sleep(50);
					}
					//System.out.println("Closing..");
					c.close();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void push(Closeable c) {
		synchronized (stack) {
			stack.push(c);
		}
	}
	public Closeable pop() {
		Closeable c = null;
		synchronized (stack) {
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (! stack.isEmpty()) {
				c = stack.pop();
			}
		}
		return c;
	}
}