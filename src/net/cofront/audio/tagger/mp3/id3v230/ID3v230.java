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
	private HashMap<String, ArrayList<ID3v230Frame>> framemap = new HashMap<String, ArrayList<ID3v230Frame>>();
	
	

	private ID3v230TagHeader header;
	private ID3v230ExtendedHeader eheader;	// ID3v2.3.0
	
	public static void write(File f) {
		ID3v230 oldtag = null;
	}
	
	public static void removeTag(File f) throws FileNotFoundException, IOException, ID3v2Exception {
		//ID3v230 tag = read(f);
		//if (tag != null) {
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			raf.seek(0);
			//tag.getTagSize();
			//System.out.println(tag.getTagSize());
			boolean foundtag = false;
			boolean foundsync = false;
			int r = 0;
			byte[] buff = new byte[2048];
			long tagPosition = 0;
			byte[] header = new byte[10];
			byte[] sbytes = new byte[4];
			while ( !foundtag && !foundsync && (r = raf.read(buff)) > -1 ) {
				for (int i=0; i<r; i++) {
					tagPosition++;
					Util.rpush(buff[i], header);
					if (detectHeader(header)) {
						
						Util.byteCopy(header, 6, 4, sbytes, 0);
						
						tagPosition = tagPosition - header.length;
						foundtag = true;
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
			int tsize = Util.twentyEightBitByteArrayToInt(sbytes) + header.length; //tag.getTagSize() + header.length;
			
			System.out.println("tsize="+tsize);
			System.out.println("tagpos="+tagPosition);
			raf.seek(tagPosition);
			byte[] zero = new byte[tsize];
			raf.write(zero);
			
		//}
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
					tagPosition = tagPosition - header.length;
					tag = new ID3v230();
					tag.header = new ID3v230TagHeader(header);
					if (tag.header.version[0] != 0x03) {
						return null;
					}
					int tsize = tag.header.getTagSize();
					
					int frameoffset = 0;
					buff = new byte[tsize];
					raf.seek(tagPosition);
					r = raf.read(buff);
					
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
							Util.byteCopy(buff, x+1, fdatasize, fdata, 0);
							
							frame.setFrameData(fdata);
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
		return tag;
	}
	
	
	private void checkmodified() throws IOException {
		if (modified) {
			getBytes();
		}
	}
	
	public void setPadding(int psize) {
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
	
	private synchronized void validatekey(String frameId) {
		if (! framemap.containsKey(frameId)) {
			framemap.put(frameId, new ArrayList<ID3v230Frame>());
		}
	}
	
	public synchronized void addFrame(ID3v230Frame frame) {
		modified = true;
		String frameId = frame.getFrameIdAsString();
		validatekey(frameId);
		framemap.get(frameId).add(frame);
	}

	public synchronized void setFrame(ID3v230Frame frame) {
		modified = true;
		String frameId = frame.getFrameIdAsString();
		validatekey(frameId);
		removeFrames(frameId);
		ArrayList<ID3v230Frame> list = new ArrayList<ID3v230Frame>();
		list.add(frame);
		framemap.put(frameId, list);
	}
	
	public synchronized void setFrames(String frameId, ArrayList<ID3v230Frame> frames) {
		modified = true;
		validatekey(frameId);
		removeFrames(frameId);
		framemap.put(frameId, frames);
	}
	
	public synchronized List<ID3v230Frame> getFrames(String frameId) {
		validatekey(frameId);
		return framemap.get(frameId);
	}
	
	public synchronized void removeFrames(String frameId) {
		modified = true;
		framemap.remove(frameId);
	}
	
	public synchronized ArrayList<ID3v230Frame> getAllFrames() {
		ArrayList<ID3v230Frame> a = new ArrayList<ID3v230Frame>();
		Set<String> keys = framemap.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			a.addAll(framemap.get(i.next()));
		}
		return a;
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
		bos.write(ehbytes);
		bos.write(fbytes);
		bos.write(padding);
		
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
