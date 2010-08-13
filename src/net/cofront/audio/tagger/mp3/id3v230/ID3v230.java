package net.cofront.audio.tagger.mp3.id3v230;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.cofront.audio.tagger.ID3v2Frame;
import net.cofront.audio.tagger.Tagger;
import net.cofront.audio.tagger.Util;
import net.cofront.audio.tagger.mp3.ID3v2;
import net.cofront.audio.tagger.mp3.ID3v2Exception;
import net.cofront.audio.tagger.mp3.ID3v2.Header;

/**
 * http://id3.org/d3v2.3.0
 * 
 * @author Aaron Loucks
 *
 */
public class ID3v230 extends ID3v2 {
	final private ByteArrayOutputStream bos = new ByteArrayOutputStream();
	
	final public static byte FLAG_UNSYNCHRONIZED = (1 << (7 - 1)); // bit7 (1st bit)
	final public static byte FLAG_EXTENDEDHEADER = (1 << (6 - 1)); // bit6 (2nd bit)
	final public static byte FLAG_EXPERIMENTAL   = (1 << (5 - 1)); // bit5 (3rd bit)
	private boolean modified = false;
	//private HashMap<String, ArrayList<ID3v230Frame>> framemap = new HashMap<String, ArrayList<ID3v230Frame>>();
	private ArrayList<ID3v230Frame> framelist = new ArrayList<ID3v230Frame>();
	private static File ftmp = null;
	private static RandomAccessFile rtmp = null;

	private ID3v230TagHeader header;
	private ID3v230ExtendedHeader eheader;	// ID3v2.3.0
	
	public static void write(File f) {
		ID3v230 oldtag = null;
	}
	
	public static void removeTag(File f) throws FileNotFoundException, IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		removeTag(raf);
		raf.close();
	}
	
	private static synchronized RandomAccessFile getTempFile(boolean reset) throws IOException {
		if (ftmp == null || rtmp == null) {
			ftmp = File.createTempFile("tagger", null);
			ftmp.deleteOnExit();
			rtmp = new RandomAccessFile(ftmp, "rw");
		}
		if (reset) {
			rtmp.setLength(0);
			rtmp.seek(0);
		}
		return rtmp;
	}
	
	public static synchronized void killTempFile() throws IOException {
		rtmp.close();
		ftmp.delete();
	}
	
	public synchronized void write(File f, boolean replace) throws FileNotFoundException, IOException {
		long start = System.currentTimeMillis();
		//File ftmp = File.createTempFile("tagger", null);
		//ftmp.deleteOnExit();
		RandomAccessFile rtmp;
		System.out.println("tmpcreate="+(System.currentTimeMillis() - start));
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		boolean foundtag = false;
		byte[] buff = new byte[4096*32];
		long tagPosition = 0;
		byte[] header = new byte[10];
		byte[] size = new byte[4];
		int tsize;
		int ttsize;
		int r = 0;
		if (! replace) {
			raf.seek(0);
			//System.out.println("writing tag: " + new String(this.getBytes()));
			System.out.println("tagsize="+this.getTagSize());
			rtmp = getTempFile(true);
			rtmp.write(this.getBytes());
			System.out.println("len="+raf.length());
			while ( (r = raf.read(buff)) > -1) {
				rtmp.write(buff, 0, r);
			}
			raf.setLength(0);
			rtmp.seek(0);
			while ( (r = rtmp.read(buff)) > -1) {
				raf.write(buff, 0, r);
			}
			return;
		}
		else {
			while ((r = raf.read(buff)) > -1 ) {
				if (! foundtag) {
					for (int i=0; i<r; i++) {
						tagPosition++;
						Util.rpush(buff[i], header);
						if (detectHeader(header)) {
							
							Util.byteCopy(header, 6, 4, size, 0);
							tsize = Util.twentyEightBitByteArrayToInt(size); 
							ttsize = tsize + header.length;
							tagPosition = tagPosition - header.length;
							foundtag = true;
							// if the old tag is the same size or larger
							// write the new tag and be done
							//System.out.println("tsize="+tsize);
							//System.out.println("tagsize="+this.getTagSize());
							if (tsize >= this.getTagSize()) {
								this.setPadding(tsize - this.getTagSize());
								raf.seek(tagPosition);
								raf.write(this.getBytes());
								raf.close();
								return;
							}
							
							
							// discard the rest of the buffer
							r = 0; 
							
							
							// have the next read start after old tag
							raf.seek(tagPosition + ttsize);
							
							// write the new tag
							rtmp = getTempFile(true);
							rtmp.write(this.getBytes());
							break;
						}
					}
				}
				// if we found the tag, r will be 0 and the rest of 
				// the buffer will be discarded during the current read/write
				rtmp = getTempFile(false);
				rtmp.write(buff, 0, r);
			}	
			
			raf.setLength(0);
			raf.seek(0);
			
			if (! foundtag) {
				//System.out.println("wtf");
				raf.write(this.getBytes());
			}
			rtmp = getTempFile(false);
			rtmp.seek(0);
			
			while ((r = rtmp.read(buff)) > -1) {
				raf.write(buff, 0, r);
			}

			raf.close();
			//killTempFile();
		}
		
		
	}
	
	public static synchronized void removeTag(RandomAccessFile raf) throws IOException {
		raf.seek(0);
		
		boolean foundtag = false;
		int r = 0;
		byte[] buff = new byte[4098*32];
		long tagPosition = 0;
		byte[] header = new byte[10];
		byte[] size = new byte[4];

		RandomAccessFile rtmp = getTempFile(true);
		int tsize;
		int ttsize;
		
		while ((r = raf.read(buff)) > -1 ) {
			if (! foundtag) {
				for (int i=0; i<r; i++) {
					tagPosition++;
					Util.rpush(buff[i], header);
					if (detectHeader(header)) {
						Util.byteCopy(header, 6, 4, size, 0);
						tagPosition = tagPosition - header.length;
						foundtag = true;
						// discard the rest of the buffer
						r = 0; 
						tsize = Util.twentyEightBitByteArrayToInt(size); 
						ttsize = tsize + header.length;
						// have the next read start after the tag
						raf.seek(tagPosition + ttsize);
						break;
					}
				}
			}
			// if we found the tag, r will be 0 and the rest of 
			// the buffer will be discarded during the current read/write
			rtmp.write(buff, 0, r);
		}				

		// truncate the file
		raf.setLength(0);
		
		raf.seek(0);
		rtmp.seek(0);
		
		// copy the temp data back into the file. deleting the original and renaming
		// the temp would be faster, but we would loose the original creation date
		// and any other file properties.
		while ((r = rtmp.read(buff)) > -1) {
			raf.write(buff, 0, r);
		}
		
		//rtmp.close();
		//ftmp.delete();
	}
	
	public static ID3v230 read(File f) throws FileNotFoundException, IOException, ID3v2Exception {
		ID3v230 tag = null;
		int r = 0;
		byte[] buff;
		
		buff = new byte[2048];
		
		// Byte block for detecting the ID3 header 
		// and for detecting MPEG sync.
		byte[] header = new byte[10];
		
		boolean foundtag  = false;
		boolean foundsync = false;
		long tagPosition = 0;

		byte[] fid = new byte[4];
		byte[] fsize = new byte[4];
		byte[] fflags = new byte[2];
		byte[] fheader = new byte[10];
		byte[] fdata = null;
		
		int hbytecount = 0;
		int fdatasize = 0;
		int totalframesize = 0;
		
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		
		while ( !foundtag && !foundsync && (r = raf.read(buff)) > -1 ) {
			
			for (int i=0; i<r; i++) {
				tagPosition++;
				Util.rpush(buff[i], header);
				if (detectHeader(header)) {
					
					//System.out.println("found header");

					tag = new ID3v230();
					tag.header = new ID3v230TagHeader(header);
					if (tag.header.version[0] != 0x03) {
						return null;
					}
					int tsize = tag.header.getTagSize();
					//System.out.println("tsize=" + tsize);
					int frameoffset = 0;
					buff = new byte[tsize];
					raf.seek(tagPosition); // should be the first byte after the header
					r = raf.read(buff);
					
					//System.out.println(new String(buff));
					
					if (r != buff.length) {
						// something bad happened
						return null;
					}
					if (tag.header.getFlag(FLAG_UNSYNCHRONIZED)) {
						byte[] tmp = reverseunsync(buff);
						buff = tmp;
					}
					if (tag.header.getFlag(FLAG_EXTENDEDHEADER)) {
						tag.eheader = new ID3v230ExtendedHeader(buff);
						frameoffset = tag.eheader.getSize();
					}
					
					// x will auto be incremented after finding a frame
					for (int x=frameoffset; x<tsize; x++) {
						hbytecount++;
						Util.rpush(buff[x], fheader);
				
						// we've read a full frame header
						if (hbytecount == 10 && ID3v230GenericFrame.isValidFrameId(fheader)) {

							ID3v230Frame frame = new ID3v230GenericFrame(fheader);
							fdatasize = frame.getFrameDataSize();
							
							totalframesize += fdatasize;
							hbytecount = 0;

							
							if (fdatasize < 1) {
								// Empty frames are invalid. There's padding on this file
								// but it's not declared in the extended header 
								// (or there is no extended header)
								break;
							}
							
							fdata = new byte[fdatasize];
							//System.out.println("fdatasize=" + fdatasize);
							Util.byteCopy(buff, x+1, fdatasize, fdata, 0);
							
							frame.setFrameData(fdata);
							//System.out.println("frame=" + frame.getFrameIdAsString());
							//System.out.println("fdata="+new String(fdata));
							x += fdatasize;
							if (fdatasize == fdata.length) {
								tag.addFrame(frame);
							}
							else {
								throw new ID3v2Exception("Expected Frame Size: " + fdatasize + ", Actual: " + fdata.length);
							}
						}
					}
					foundtag = true; 
					Tagger.close(raf);
					break;
				}
				/*
				if ( (header[0] & SYNCBYTES_MASK[0]) > 0 &&  (header[1] & SYNCBYTES_MASK[1]) > 0 ) {
					System.out.println("Found sync: " + i);
					foundsync = true;
					break;
				}
				*/
			}
		}
		int psize = tag.header.getTagSize() - tag.getFramesBytes().length;
		if (psize != tag.getPaddingSize()) {
			tag.setPadding(psize);
		}
		return tag;
	}
	
	
	private void checkmodified() throws IOException {
		if (modified) {
			getBytes();
		}
	}
	
	public void setPadding(int psize) {
		modified = true;
		if (eheader == null) {
			eheader = new ID3v230ExtendedHeader(Util.intToByteArray(6), new byte[] { 0, 0 }, Util.intToByteArray(psize), new byte[] { 0, 0 } );
		}
		else {
			eheader.setPaddingSize(psize);
		}
	}
	
	public int getPaddingSize() {
		if (eheader == null) {
			return 0;
		}
		else {
			return eheader.getPaddingSize();
		}
	}
	/*
	private synchronized void validatekey(String frameId) {
		if (! framemap.containsKey(frameId)) {
			framemap.put(frameId, new ArrayList<ID3v230Frame>());
		}
	}
	*/
	
	public synchronized void addFrame(ID3v230Frame frame) {
		modified = true;
		/*
		String frameId = frame.getFrameIdAsString();
		validatekey(frameId);
		framemap.get(frameId).add(frame);
		*/
		framelist.add(frame);
	}

	public synchronized void setFrame(ID3v230Frame frame) throws Exception {
		modified = true;
		/*
		String frameId = frame.getFrameIdAsString();
		validatekey(frameId);
		removeFrames(frameId);
		ArrayList<ID3v230Frame> list = new ArrayList<ID3v230Frame>();
		list.add(frame);
		framemap.put(frameId, list);
		*/
		framelist.add(frame);
		throw new Exception("Not implemented");
	}
	
	public synchronized void setFrames(String frameId, ArrayList<ID3v230Frame> frames) {
		modified = true;
		/*
		validatekey(frameId);
		removeFrames(frameId);
		framemap.put(frameId, frames);
		*/
		framelist.addAll(frames);
	}
	
	public synchronized List<ID3v230Frame> getFrames(String frameId) {
		/*
		validatekey(frameId);
		return framemap.get(frameId);
		*/
		ArrayList<ID3v230Frame> tmp = new ArrayList<ID3v230Frame>();
		Iterator<ID3v230Frame> i = framelist.iterator();
		while (i.hasNext()) {
			ID3v230Frame f = i.next();
			if (f.getFrameIdAsString().equals(frameId)) {
				tmp.add(i.next());
			}
		}
		return tmp;
	}
	
	public synchronized void removeFrames(String frameId) {
		modified = true;
		/*
		framemap.remove(frameId);
		*/
		ArrayList<ID3v230Frame> tmp = new ArrayList<ID3v230Frame>();
		Iterator<ID3v230Frame> i = framelist.iterator();
		while (i.hasNext()) {
			ID3v230Frame f = i.next();
			if (! f.getFrameIdAsString().equals(frameId)) {
				tmp.add(i.next());
			}
		}
		framelist = tmp;
		
	}
	
	public synchronized ArrayList<ID3v230Frame> getAllFrames() {
		/*
		ArrayList<ID3v230Frame> a = new ArrayList<ID3v230Frame>();
		Set<String> keys = framemap.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			a.addAll(framemap.get(i.next()));
		}
		return a;
		*/
		return framelist;
	}
	
	public synchronized byte[] getFramesBytes() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ArrayList<ID3v230Frame> a = this.getAllFrames();
		Iterator<ID3v230Frame> i = a.iterator();
		while (i.hasNext()) {
			byte[] bytes = i.next().getBytes();
			b.write(bytes);
		}
		return b.toByteArray();
	}
	
	/**
	 * Get the size of the tag, excluding the header.
	 * @return
	 * @throws IOException 
	 */
	public synchronized int getTagSize() throws IOException {
		checkmodified();
		return header.getTagSize();
	}
	
	/* *
	 * Get the size of the tag, including the header.
	 * @return
	 */
	//public synchronized int getTagSizeTotal() {
	//	return header.getTagSizeTotal();
	//}
	
	public synchronized boolean getFlag(byte flag) throws IOException {
		checkmodified();
		return header.getFlag(flag);
	}
	
	/**
	 * Get the ID3v2 tag version. 
	 * @return major.minor
	 * @throws IOException 
	 */
	public synchronized String getVersion() throws IOException {
		checkmodified();
		byte[] v = header.getVersion();
		String version = new String(v[0] + "." + v[1]);
		return version;
		
	}
	
	public synchronized byte[] getBytes() throws IOException {
		/*
		byte[] ehbytes = new byte[0];
		byte[] fbytes = this.getFramesBytes();
		byte[] padding;
		
		int psize = 0; // default 0 bytes of padding
		if (eheader != null) {
			psize = eheader.getPaddingSize();
			padding = new byte[psize];
			ehbytes = eheader.getBytes();
		}
		else {
			padding = new byte[psize];
		}
		
		bos.reset();
		bos.write(ehbytes);
		bos.write(fbytes);

		byte[] tmp = bos.toByteArray();
		byte[] body = usync(tmp);
		
		if (tmp.length != body.length) {
			header.setFlag(FLAG_UNSYNCHRONIZED, true);
		}
		
		header.setTagSize(body.length + psize);
		
		bos.reset();
		bos.write(header.getBytes());
		//bos.write(ehbytes);
		bos.write(fbytes);
		if (padding.length == 0) {
			//padding = new byte[128];
		}
		bos.write(padding);
		
		modified = false;
		return bos.toByteArray();
		*/
		
		byte[] hbytes = header.getBytes();
		byte[] fbytes = this.getFramesBytes();
		
		bos.reset();
		
		
		bos.write(hbytes);
		bos.write(fbytes);
		bos.write(new byte[this.getPaddingSize()]);
		modified = false;
		return bos.toByteArray();
		
	}
	
	/**
	 * Perform the unsynchronization scheme on the raw tag data. This will insert
	 * a null byte between the MPEG sync bits. 
	 * @param rawtag
	 * @return
	 */
	public static byte[] usync(byte[] rawtag) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		int size = rawtag.length;
		byte lastByte = 0;
		
		for (int i=0; i<size; i++) {
			if ( (lastByte & SYNCBYTES_MASK[0]) > 0 && (rawtag[i] & SYNCBYTES_MASK[1]) > 0 ) {
				b.write(0);
			}
			if (i > 0) {
				lastByte = rawtag[i];
			}
			b.write(rawtag[i]);
		}
		return b.toByteArray();
	}
	
	/**
	 * Reverse the unsynchronization scheme on the raw tag data. This will remove
	 * the null byte between the MPEG sync bits.
	 * @param rawtag
	 * @return
	 */
	public static byte[] reverseunsync(byte[] rawtag) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		int size = rawtag.length;
		byte lastByte = 0;

		for (int i=0; i<size; i++) {
			if ( (lastByte & SYNCBYTES_MASK[0]) > 0 && rawtag[i] == 0 && (i+i < size) && (rawtag[i+1] & SYNCBYTES_MASK[1]) > 0 ) {
				// ignore this byte.
			}
			else {
				b.write(rawtag[i]);
			}
			if (i > 0) {
				lastByte = rawtag[i];
			}
			
		}
		return b.toByteArray();
	}

}
