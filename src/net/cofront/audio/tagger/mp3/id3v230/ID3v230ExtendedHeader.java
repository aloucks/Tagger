package net.cofront.audio.tagger.mp3.id3v230;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.cofront.audio.tagger.Util;

public class ID3v230ExtendedHeader {
		
		final private ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		
		public final static byte FLAG_CRC = (1 << 6);
		
		private byte[] size;	// = new byte[4];
		private byte[] flags;	// = new byte[2];
		private byte[] psize;	// = new byte[4];
		private byte[] crc;		// = new byte[4];
		
		protected ID3v230ExtendedHeader(byte[] rawtag) {
			Util.byteCopy(rawtag, 0, 4, size, 0);
			Util.byteCopy(rawtag, 4, 2, flags, 0);
			Util.byteCopy(rawtag, 6, 4, psize, 0);
			if ( getFlag(FLAG_CRC) ) {
				Util.byteCopy(rawtag, 10, 4, crc, 0);
			}
		}
		
		protected ID3v230ExtendedHeader(byte[] size, byte[] flags, byte[] psize, byte[] crc) {
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
