package net.cofront.audio.tagger.mp3;

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
import net.cofront.audio.tagger.UnexpectedFrameDataException;
import net.cofront.audio.tagger.Util;

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
	
	

	private ID3v2.Header header;
	private ID3v230.ExtHeader eheader;	// ID3v2.3.0
	
	public static void write(File f) {
		ID3v230 oldtag = null;
	}
	
	public static ID3v230 read(File f) throws FileNotFoundException, IOException, UnexpectedFrameDataException {
		ID3v230 tag = null;
		int r = 0;
		byte[] buff;
		
		buff = new byte[2048];
		
		// Byte block for detecting the ID3 header 
		// and for detecting MPEG sync.
		byte[] block = new byte[10];
		
		boolean foundtag  = false;
		boolean foundsync = false;
		long tagPosition = 0;

		RandomAccessFile ras = new RandomAccessFile(f, "r");
		
		while ( !foundtag && !foundsync && (r = ras.read(buff)) > -1 ) {
			
			for (int i=0; i<r; i++) {
				tagPosition++;
				Util.rpush(buff[i], block);
				if (detectHeader(block)) {
					tagPosition = tagPosition = block.length;
					tag = new ID3v230();
					tag.header = new ID3v2.Header(block);
					if (tag.header.version[0] != 0x03) {
						return null;
					}
					int tsize = tag.header.getTagSize();
					System.out.println("tag size = " + tsize);
					int frameoffset = 0;
					buff = new byte[tsize];
					ras.seek(tagPosition);
					r = ras.read(buff);
					
					if (r != buff.length) {
						// something bad happened
						return null;
					}
					if (tag.header.getFlag(FLAG_UNSYNCHRONIZED)) {
						byte[] tmp = reverseunsync(buff);
						buff = tmp;
					}
					if (tag.header.getFlag(FLAG_EXTENDEDHEADER)) {
						tag.eheader = new ID3v230.ExtHeader(buff);
						frameoffset = tag.eheader.getSize();
					}
					byte[] fid = new byte[4];
					byte[] fsize = new byte[4];
					byte[] fflags = new byte[2];
					byte[] fhbuff = new byte[10];
					int hbytecount = 0;
					int fdatasize = 0;
					int totalframesize = 0;
					byte[] fdata = null;
					System.out.println("frameoffset = " + frameoffset);
					System.out.println(new String(buff));
					System.exit(1);
					for (int x=frameoffset; x<tsize; x++) {
						hbytecount++;
						Util.rpush(buff[x], fhbuff);
						System.out.println(new String(fhbuff));
						// we've read a full frame header
						if (hbytecount == 10 && ID3v230.Frame.isValidFrameId(fhbuff)) {
							Util.byteCopy(fhbuff, 0, 4, fid, 0);
							Util.byteCopy(fhbuff, 4, 4, fsize, 0);
							Util.byteCopy(fhbuff, 8, 2, fflags, 0);
							ID3v230.Frame frame = new ID3v230.Frame(fid, fsize, fflags);
							fdatasize = frame.getFrameDataSize();
							totalframesize += fdatasize;
							hbytecount = 0;

							System.out.println("fdatasize = " + fdatasize);
							if (fdatasize < 1) {
								// Empty frames are invalid. There's padding on this file
								// but it's not declared in the extended header 
								// (or there is no extended header)
								break;
							}
							
							fdata = new byte[fdatasize];
							Util.byteCopy(buff, x+1, fdatasize, fdata, 0);
							frame.setFrameData(fdata);
							if (fdatasize == fdata.length) {
								tag.addFrame(frame);
							}
							else {
								throw new UnexpectedFrameDataException("Expected: " + fdatasize + ", Actual: " + fdata.length);
							}
							
						}
					}
					foundtag = true; 
					Tagger.close(ras);
					break;
				}
				if ( (block[0] & SYNCBYTES_MASK[0]) > 0 &&  (block[1] & SYNCBYTES_MASK[1]) > 0 ) {
					System.out.println("Found sync: " + i);
					foundsync = true;
					break;
				}
			}
		}
		return tag;
	}
	
	private boolean modified = false;
	
	private HashMap<String, ArrayList<Frame>> framemap = new HashMap<String, ArrayList<Frame>>();
	
	private void checkmodified() throws IOException {
		if (modified) {
			getBytes();
		}
	}
	
	public void setPadding(int psize) {
		if (eheader == null) {
			eheader = new ID3v230.ExtHeader(Util.intToByteArray(6), new byte[] { 0, 0 }, Util.intToByteArray(psize), new byte[] { 0, 0 } );
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
	
	// ---------------------------------------------------------------------------------------
	
	
	
	public static class ExtHeader {
		
		final private ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		
		public final static byte FLAG_CRC = (1 << 6);
		
		private byte[] size;	// = new byte[4];
		private byte[] flags;	// = new byte[2];
		private byte[] psize;	// = new byte[4];
		private byte[] crc;		// = new byte[4];
		
		protected ExtHeader(byte[] rawtag) {
			Util.byteCopy(rawtag, 0, 4, size, 0);
			Util.byteCopy(rawtag, 4, 2, flags, 0);
			Util.byteCopy(rawtag, 6, 4, psize, 0);
			if ( getFlag(FLAG_CRC) ) {
				Util.byteCopy(rawtag, 10, 4, crc, 0);
			}
		}
		
		protected ExtHeader(byte[] size, byte[] flags, byte[] psize, byte[] crc) {
			this.size = size;
			this.flags = flags;
			this.psize = psize;
			this.crc = crc;
		}
		
		protected int getSize() {
			return Util.byteArrayToInt(size);
		}
		
		protected void setSize(int size) {
			this.size = Util.intToByteArray(size);
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
	
	public static class Frame {
		private byte[] id = new byte[4];
		private byte[] size = new byte[4];
		private byte[] flags = new byte[2];
		private byte[] data = null;
		
		public static boolean isValidFrameId(byte[] b) {
			// Frame IDs are A-Z0-9 only
			return (
				(b[0] > 47 && b[0] < 58) || (b[0] > 64 && b[0] < 91) &&
				(b[1] > 47 && b[1] < 58) || (b[1] > 64 && b[1] < 91) &&
				(b[2] > 47 && b[2] < 58) || (b[2] > 64 && b[2] < 91) &&
				(b[3] > 47 && b[3] < 58) || (b[3] > 64 && b[3] < 91)
			);
		}
		
		public byte[] getBytes() {
			ByteArrayOutputStream b = new ByteArrayOutputStream(id.length + size.length + flags.length + data.length);
			try {
				b.write(id);
				b.write(size);
				b.write(flags);
				b.write(data);
				return b.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
		
		protected Frame(byte[] id, byte[] size, byte[] flags) {
			Util.byteCopy(id, 0, 4, this.id, 0);
			Util.byteCopy(size, 0, 4, this.size, 0);
			Util.byteCopy(flags, 0, 2, this.flags, 0);
		}

		protected synchronized void setFrameData(byte[] data) {
			this.data = data;
			this.setFrameDataSize(this.data.length);
		}
		
		public byte[] getFrameId() {
			return this.id;
		}
		
		public String getFrameIdAsString() {
			return String.valueOf(id);
		}
		
		public synchronized byte[] getFrameData() {
			return this.data;
		}
		
		protected synchronized void setFrameDataSize(int s) {
			size = Util.intToByteArray(s);
		}
		
		public synchronized int getFrameDataSize() {
			return Util.byteArrayToInt(size);
		}
	}

}
