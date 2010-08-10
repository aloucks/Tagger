package net.cofront.audio.tagger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ID3v2 {
	
	private ID3v2Header header = null;
	private ID3v2ExtendedHeader exthead = null;
	private ArrayList<ID3v2Frame> frames = null;
	private long filePosition = -1;
	
	final public static byte[] SYNCBYTES_MASK = new byte[] { 
		// 11111111
		1 << 6 | 1 << 5 | 1 << 4 | 1 << 3 | 1 << 2 | 1 << 1 | 1 << 0,
		// 11100000
		1 << 6 | 1 << 5 | 1 << 4
	};
	
	public static byte[] bpush(byte b, byte[] block) {
		int len = block.length;
		for (int i=1; i<len; i++) {
			block[i-1] = block[i];
		}
		block[len - 1] = b;
		return block;
	}
	
	public static ID3v2 read(RandomAccessFile raf) throws IOException, UnexpectedFrameDataException {
		ID3v2 tag = null;
		int r;
		byte[] buff;
		
		buff = new byte[1024];
		long bytesRead = 0;
		
		// Byte block for checking against the ID3v2 header tag ("ID3") 
		// and for detecting MPEG sync.
		byte[] block = new byte[3];
		
		boolean foundhead = false;
		boolean foundsync = false;
		
		raf.seek(0);
		
		while (!foundhead && !foundsync && (r = raf.read(buff)) > -1) {
			
			for (int i=0; i<r; i++) {
				bytesRead += i;
				bpush(buff[i], block);
				//System.out.println(bytesRead + " " + new String(block));
				if (Arrays.equals(block, ID3v2Header.ID3)) {
					foundhead = true;
					//System.out.println(bytesRead); System.exit(1);
					break;
				}
				if ( (block[0] & ID3v2.SYNCBYTES_MASK[0]) > 0 &&  (block[1] & ID3v2.SYNCBYTES_MASK[1]) > 0 ) {
					foundsync = true;
					break;
				}
			}
		}
		
		// The the MPEG sync bits were found before we found a tag.
		if (foundsync) {
			return null;
		}
		
		// Didn't find a tag header. 
		if (! foundhead) {
			return null;
		}
		
		tag = new ID3v2();
		
		// Go back to the beginning of the ID3 header.
		tag.filePosition = bytesRead - block.length;
		raf.seek(tag.filePosition);
		
		// read the header bytes
		buff = new byte[10];
		r = raf.read(buff);
		
		tag.header = new ID3v2Header(buff);
		int size = tag.header.getTagSize();
		
		//System.out.println(new String(tag.header.getBytes()));
		
		//System.out.println("size = " + size);
		//System.exit(1);
		buff = new byte[size];
		
		// raw tag data: (possibly)unsynchronized extended headers + frames + padding
		// does not include the header.
		r = raf.read(buff); 
		byte[] rawtag; 
		if (tag.header.hasFlagUnsyncronized()) {
			rawtag = usync(buff);
		}
		else {
			rawtag = buff;
		}
		
		//System.out.println( new String(rawtag) ); System.exit(1);

		int framesoffset = 0;
		int frameslength = rawtag.length;
		if (tag.header.hasFlagExtendedHeader()) {
			tag.exthead = new ID3v2ExtendedHeader(rawtag);
			framesoffset = tag.exthead.getTotalExtendedHeaderSize();
			frameslength = size - tag.exthead.getPaddingSize();
		}
		
	
		
		tag.frames = new ArrayList<ID3v2Frame>();
		//System.out.println( new String(rawtag) ); System.exit(1);
		ByteArrayInputStream in = new ByteArrayInputStream(rawtag, framesoffset, frameslength);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] fid = new byte[4];
		byte[] fsize = new byte[4];
		byte[] fflags = new byte[2];
		byte[] hbuff = new byte[10];
		buff = new byte[1];
		int hbytecount = 0;
		int fdatasize = 0;
		byte[] fdata = null;
		while ( (r = in.read(buff)) > -1 ) {
			hbytecount += r;
			bpush(buff[0], hbuff);
			// we've read a full frame header
			if (hbytecount == 10) {
				Util.byteCopy(hbuff, 0, 4, fid, 0);
				Util.byteCopy(hbuff, 4, 4, fsize, 0);
				Util.byteCopy(hbuff, 8, 2, fflags, 0);
				ID3v2Frame frame = new ID3v2Frame(fid, fsize, fflags);
				fdatasize = frame.getFrameDataSize();
				hbytecount = 0;
				
				//System.out.println("data size = " + fdatasize);

				if (fdatasize < 1) {
					// Empty frames are invalid. There's padding on this file
					// but it's not declared in the extended header 
					// (or there is no extended header)
					break;
				}
				
				for (int i=0; i<fdatasize; i++) {
					r = in.read(buff);
					if (r == 1) {
						out.write(buff);
					}
				}
				fdata = out.toByteArray();
				//System.out.println("fdata[" + frame.getFrameIdAsString() + "] = " + new String(fdata));
				out.reset();
				frame.setFrameData(fdata);
				if (fdatasize == fdata.length) {
					tag.frames.add(frame);
				}
				else {
					throw new UnexpectedFrameDataException("Expected: " + fdatasize + ", Actual: " + fdata.length);
				}
				
			}
		}
		
		
		
		return tag;
	}
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		b.write(header.getBytes());
		if (exthead != null) {
			b.write(exthead.getBytes());
		}
		Iterator<ID3v2Frame> i = frames.iterator();
		while (i.hasNext()) {
			b.write(i.next().getBytes());
		}
		return b.toByteArray();
	}
	
	public void write(RandomAccessFile raf) throws IOException {
		// this is the original tag size inclding header
		int tsize = header.getTotalTagSize();
		
		// this is the new data including header
		byte[] tagdata = getBytes();
		
		// if this is a new tag that wasn't read from a file
		// or our total amount of data has grown, we need to 
		// grow the file.
		if (filePosition < 0 || tagdata.length > tsize) {
			// need to grow the file.
			File tmp = File.createTempFile("temp", null);
			tmp.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tmp);
			raf.seek(0);
			
		}
		else {
			raf.seek(filePosition);
			raf.write(tagdata);
			
		}
	}
	
	public List<ID3v2Frame> getFrames() {
		return this.frames;
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
