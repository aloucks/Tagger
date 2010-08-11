package net.cofront.audio.tagger.mp3;

public class SyncTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		sayHello();
	}
	
	public static synchronized String getHello() {
		return "Hello";
	}
	
	public static synchronized void sayHello() {
		System.out.println(getHello());
	}

}
