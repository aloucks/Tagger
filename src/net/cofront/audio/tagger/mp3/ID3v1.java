package net.cofront.audio.tagger.mp3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import net.cofront.audio.tagger.Util;

public class ID3v1 {
	public static byte[] TAG_IDENTIFIER = ("TAG").getBytes();
	
	public String title;
	public String artist;
	public String album;
	public String year;
	public String comment;
	public int track;
	public int genre;
	
	
	public ID3v1() {
		this.title = "";
		this.artist = "";
		this.album = "";
		this.year = "";
		this.comment = "";
		this.track = 0;
		this.genre = 0;
	}
	
	public ID3v1(String title, String artist, String album, String year, 
			String comment, int track, int genre) {
		this(title.getBytes(), artist.getBytes(), album.getBytes(), year.getBytes(), 
			comment.getBytes(), (byte)track, (byte)genre);
	}
	
	public ID3v1(byte[] title, byte[] artist, byte[] album, byte[] year, 
			byte[] comment, byte track, byte genre) {
		
		this.title = new String(title).trim();//.substring(0, 30);
		this.artist = new String(artist).trim();//.substring(0, 30);
		this.album = new String(album).trim();//.substring(0, 30);
		this.year = new String(year).trim();//.substring(0, 4);
		this.comment = new String(comment).trim();//.substring(0, 30);
		this.track = track;
		this.genre = genre;
	}
	
	public static ID3v1 read(RandomAccessFile raf) throws IOException {
		return read(new RandomAccessFile[] { raf })[0];
	}
	
	public static ID3v1[] read(RandomAccessFile[] rafArray) throws IOException {
		
		byte[] title = new byte[30];
		byte[] artist = new byte[30];
		byte[] album = new byte[30];
		byte[] year = new byte[4];
		byte[] comment = new byte[30];
		byte[] genre = new byte[1];
		
		int rLen = rafArray.length;
		ID3v1[] id3v1 = new ID3v1[rLen];

		for (int i=0; i<rLen; i++) {
			RandomAccessFile raf = rafArray[i];
			long fsize = raf.length();
			long tagstart = fsize - 128;
			
			raf.seek(tagstart);
			byte[] tag = new byte[TAG_IDENTIFIER.length];
			raf.read(tag);
			
			id3v1[i] = null;
			if (Arrays.equals(tag, TAG_IDENTIFIER)) {
				raf.read(title);
				raf.read(artist);
				raf.read(album);
				raf.read(year);
				raf.read(comment);
				raf.read(genre);
				id3v1[i] = new ID3v1(title, artist, album, year, comment, comment[29], genre[0]);
			}
		}
		
		return id3v1;
	}
	
	public void write(RandomAccessFile raf) throws IOException {
		long fsize = raf.length();
		long tagstart = fsize - 128;
		
		raf.seek(tagstart);
		byte[] tag = new byte[TAG_IDENTIFIER.length];
		raf.read(tag);
		
		if (! Arrays.equals(tag, TAG_IDENTIFIER)) {
			raf.setLength(fsize + 128);
			raf.seek(fsize);
			raf.write(TAG_IDENTIFIER);
		}
		
		byte[] title = new byte[30];
		//Arrays.fill(title, this.title.trim().getBytes(), 30);
		raf.write(Util.fillCopy(this.title.getBytes(), 30, (byte)0));
		raf.write(Util.fillCopy(this.artist.getBytes(), 30, (byte)0));
		raf.write(Util.fillCopy(this.album.getBytes(), 30, (byte)0));
		raf.write(Util.fillCopy(this.year.getBytes(), 4, (byte)0));
		raf.write(Util.fillCopy(this.comment.getBytes(), 29, (byte)0));
		raf.write((byte)this.track);
		raf.write((byte)this.genre);
		
	}
	
	
	
	public String toString() {
		return "[title=" + title + ",artist=" + artist + ",album=" + album + ",year=" + year + ",comment=" + comment + ",track=" + track + ",genre=" + genre + "]";
	}
	
}
