package net.cofront.audio.tagger.mp3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.cofront.audio.tagger.Util;

public abstract class ID3v2 {
	
	public final static byte[] TAG_IDENTIFIER = ("ID3").getBytes();
	
	final public static byte[] SYNCBYTES_MASK = new byte[] { 
		// 11111111
		1 << 6 | 1 << 5 | 1 << 4 | 1 << 3 | 1 << 2 | 1 << 1 | 1 << 0,
		// 11100000
		1 << 6 | 1 << 5 | 1 << 4
	};
	
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
	
	public static class Header {
		
		final private ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		
		protected byte[] version; // = new byte[2];
		protected byte   flags;	// = (1 byte)
		protected byte[] size;	// = new byte[4];
		
		protected Header() {
			version = new byte[] { 0x03, 0x00 };
			flags = 0x00;
			size = new byte[] { 0x00, 0x00, 0x00, 0x00 };
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
}
