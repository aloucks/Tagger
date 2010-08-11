package net.cofront.audio.tagger.mp3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.cofront.audio.tagger.Util;

/**
 * http://id3.org/d3v2.3.0
 * 
 * @author Aaron Loucks
 *
 */
public class ID3v2 {
	final private ByteArrayOutputStream bos = new ByteArrayOutputStream();
	
	public final static byte[] TAG_IDENTIFIER = ("ID3").getBytes();
	
	final public static byte FLAG_UNSYNCHRONIZED = (1 << (7 - 1)); // bit7 (1st bit)
	final public static byte FLAG_EXTENDEDHEADER = (1 << (6 - 1)); // bit6 (2nd bit)
	final public static byte FLAG_EXPERIMENTAL   = (1 << (5 - 1)); // bit5 (3rd bit)
	
	final public static byte[] SYNCBYTES_MASK = new byte[] { 
		// 11111111
		1 << 6 | 1 << 5 | 1 << 4 | 1 << 3 | 1 << 2 | 1 << 1 | 1 << 0,
		// 11100000
		1 << 6 | 1 << 5 | 1 << 4
	};

	private ID3v2.Header header;
	private ID3v2.ExtHeader3 eheader3;	// ID3v2.3.0
	private ID3v2.ExtHeader4 eheader4;	// ID3v2.4.0
	
	private boolean modified = false;
	
	private HashMap<String, ArrayList<Frame>> framemap = new HashMap<String, ArrayList<Frame>>();
	
	private void checkmodified() throws IOException {
		if (modified) {
			getBytes();
		}
	}
	
	public void setPadding(int psize) {
		if (eheader3 == null) {
			eheader3 = new ID3v2.ExtHeader3(Util.intToByteArray(6), new byte[] { 0, 0 }, Util.intToByteArray(psize), new byte[] { 0, 0 } );
		}
		else {
			eheader3.setPaddingSize(psize);
		}
	}
	
	private synchronized void validatekey(String frameId) {
		if (! framemap.containsKey(frameId)) {
			framemap.put(frameId, new ArrayList<Frame>());
		}
	}
	
	public synchronized void addFrame(Frame frame) {
		modified = true;
		String frameId = frame.getFrameIdAsString();
		validatekey(frameId);
		framemap.get(frameId).add(frame);
	}

	public synchronized void setFrame(Frame frame) {
		modified = true;
		String frameId = frame.getFrameIdAsString();
		validatekey(frameId);
		removeFrames(frameId);
		ArrayList<Frame> list = new ArrayList<Frame>();
		list.add(frame);
		framemap.put(frameId, list);
	}
	
	public synchronized void setFrames(String frameId, ArrayList<Frame> frames) {
		modified = true;
		validatekey(frameId);
		removeFrames(frameId);
		framemap.put(frameId, frames);
	}
	
	public synchronized List<Frame> getFrames(String frameId) {
		validatekey(frameId);
		return framemap.get(frameId);
	}
	
	public synchronized void removeFrames(String frameId) {
		modified = true;
		framemap.remove(frameId);
	}
	
	public synchronized ArrayList<Frame> getAllFrames() {
		ArrayList<Frame> a = new ArrayList<Frame>();
		Set<String> keys = framemap.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			a.addAll(framemap.get(i.next()));
		}
		return a;
	}
	
	public synchronized byte[] getFramesBytes() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ArrayList<Frame> a = this.getAllFrames();
		Iterator<Frame> i = a.iterator();
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
		if (eheader3 != null) {
			psize = eheader3.getPaddingSize();
			padding = new byte[psize];
			ehbytes = eheader3.getBytes();
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
	 * 
	 * @param block byte[10]
	 * @return
	 */
	public static boolean detectHeader(byte[] block) {
		if (
			block[0] == 0x49 && // I
			block[1] == 0x44 && // D
			block[2] == 0x33 && // 3
			block[3] <  0xFF && // version major
			block[4] <  0xFF && // version minor
		//  block[5]			// flags
			block[6] <  0x80 && // size
			block[7] <  0x80 && // size
			block[8] <  0x80 && // size
			block[9] <  0x80 	// size
		) return true;
		else return false;
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
	
	// ---------------------------------------------------------------------------------------
	
	public static class Header {
		
		final private ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		
		private byte[] version; // = new byte[2];
		private byte   flags;	// = (1 byte)
		private byte[] size;	// = new byte[4];
		
		protected Header() {
			version = new byte[] { 0x03, 0x00 };
			flags = 0x00;
			size = new byte[] { 0x00, 0x00 };
		}
		
		protected Header(byte[] rawheader) {
			this();
			Util.byteCopy(rawheader, 3, 2, version, 0);
			flags = rawheader[5];
			Util.byteCopy(rawheader, 6, 4, size, 0);
		}
		
		protected Header(byte[] version, byte flags, int size) {
			this(version, flags, Util.intToTwentyEightBitByteArray(size));
		}
		
		protected Header(byte[] version, byte flags, byte[] size) {
			this.version = version;
			this.size = size;
			this.flags = flags;
		}
		
		/**
		 * 
		 * @return byte[] { major, minor }
		 */
		public byte[] getVersion() {
			return version;
		}
		
		/**
		 * ID3v2.major.minor
		 * @param major
		 * @param minor
		 */
		protected void setVersion(byte major, byte minor) {
			version[0] = major;
			version[1] = minor;
		}
		
		public byte[] getTagSizeBytes() {
			return size;
		}
		
		/**
		 * Does not include the size of the header (10 bytes).
		 * Total tag size: getTagSize() + 10 + padding
		 * @return
		 */
		public int getTagSize() {
			return Util.twentyEightBitByteArrayToInt(size);
		}
		
		//public int getTagSizeTotal() {
		//	return getTagSize() + 10;
		//}
		
		protected void setTagSize(int size) {
			this.size = Util.intToTwentyEightBitByteArray(size);
		}
		
		/**
		 * Should be encoded with Util.intToTwentyEightBitByteArray().
		 * @param size
		 */
		protected void setTagSize(byte[] size) {
			this.size = size;
		}

		public byte getFlags() {
			return flags;
		}
		
		protected void setFlags(byte flags) {
			this.flags = flags;
		}
		
		public boolean getFlag(byte flag) {
			return (flags & flag) > 0 ? true : false;
		}
		
		protected void setFlag(byte flag, boolean b) {
			if (b) {
				flags = (byte)(flags | flag);
			}
			else {
				flags = (byte)(flags ^ flag);
			}
		}
		
		public synchronized byte[] getBytes() throws IOException {
			bos.reset();
			bos.write(TAG_IDENTIFIER);
			bos.write(version);
			bos.write(flags);
			bos.write(size);
			return bos.toByteArray();
		}
	}
	
	public static class ExtHeader3 {
		
		final private ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		
		public final static byte FLAG_CRC = (1 << 6);
		
		private byte[] size;	// = new byte[4];
		private byte[] flags;	// = new byte[2];
		private byte[] psize;	// = new byte[4];
		private byte[] crc;		// = new byte[4];
		
		protected ExtHeader3(byte[] rawtag) {
			Util.byteCopy(rawtag, 0, 4, size, 0);
			Util.byteCopy(rawtag, 4, 2, flags, 0);
			Util.byteCopy(rawtag, 6, 4, psize, 0);
			if ( getFlag(FLAG_CRC) ) {
				Util.byteCopy(rawtag, 10, 4, crc, 0);
			}
		}
		
		protected ExtHeader3(byte[] size, byte[] flags, byte[] psize, byte[] crc) {
			this.size = size;
			this.flags = flags;
			this.psize = psize;
			this.crc = crc;
		}
		
		protected byte[] getFlags() {
			return flags;
		}
		
		protected void setFlags(byte[] flags) {
			this.flags = flags;
		}
		
		protected int getPaddingSize() {
			return Util.byteArrayToInt(this.psize);
		}
		
		protected void setPaddingSize(int psize) {
			this.psize = Util.intToByteArray(psize);
		}
		
		/**
		 * Only the first byte in the flags is looked at.
		 * @param flag
		 * @return
		 */
		protected boolean getFlag(byte flag) {
			return (flags[0] & flag) > 0 ? true : false;
		}
		
		public synchronized byte[] getBytes() throws IOException {
			bos.reset();
			bos.write(size);
			bos.write(flags);
			bos.write(psize);
			if (getFlag(FLAG_CRC)) {
				bos.write(crc);
			}
			return bos.toByteArray();
		}

	}
	
	/** 
	 * Not implemented.
	 * @author Aaron
	 *
	 */
	public class ExtHeader4 {
		protected byte[] getBytes() {
			return new byte[0];
		}
	}
}
