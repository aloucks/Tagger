package net.cofront.audio.tagger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class ID3v2ExtendedHeader {
	public final static byte FLAG_MASK_CRC = (1 << 6);
	
	
	private byte[] size = new byte[4];
	private byte[] flags = new byte[2];
	private byte[] psize = new byte[4];
	private byte[] crc = new byte[4];
	
	
	public ID3v2ExtendedHeader(byte[] rawtag) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();

		Util.byteCopy(rawtag, 0, 4, size, 0);
		Util.byteCopy(rawtag, 4, 2, flags, 0);
		Util.byteCopy(rawtag, 6, 4, psize, 0);
		
		if ( hasFlagCrc() ) {
			Util.byteCopy(rawtag, 10, 4, crc, 0);
		}
	}
	
	public boolean hasFlagCrc() {
		return (flags[0] & FLAG_MASK_CRC) > 0;
	}
	
	/**
	 * This does NOT include the 4 bytes to describe the ext header.
	 * This number will always be 4 bytes short of the entire ext header length.
	 * @return
	 */
	public int getExtendedHeaderSize() {
		return Util.byteArrayToInt(size);
		//return ( size[0] << 24 | size[1] << 16 | size[2] << 8 | size[3] );
	}
	
	public int getTotalExtendedHeaderSize() {
		return getExtendedHeaderSize() + 4;
	}
	
	public synchronized int getPaddingSize() {
		return Util.byteArrayToInt(psize);
		//return ( psize[0] << 24 | psize[1] << 16 | psize[2] << 8 | psize[3] );
	}
	
	public synchronized void setPaddingSize(int s) {
		psize = Util.intToByteArray(s);
		/*
		psize[0] = (byte)(s >> 21);
		psize[1] = (byte)(s >> 14);
		psize[2] = (byte)(s >> 7);
		psize[3] = (byte)(s);
		*/
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			b.write(size);
			b.write(flags);
			b.write(psize);
			if (hasFlagCrc()) {
				b.write(crc);
			}
			return b.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
