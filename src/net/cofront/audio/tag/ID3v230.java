package net.cofront.audio.tag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ID3v230 {
	public final static byte[] TAG_IDENTIFIER = ("ID3").getBytes();
	
	final public static int FLAG_UNSYNCHRONIZED = (1 << 7); // bit7 (1st bit)
	final public static int FLAG_EXTENDEDHEADER = (1 << 6); // bit6 (2nd bit)
	final public static int FLAG_EXPERIMENTAL   = (1 << 5); // bit5 (3rd bit)
	final public static int EH_FLAG_CRC         = (1 << 7); // bit7 (1st bit)
	
	final public static int OPT_DISCARDEXTHEADER = (1 << 1);
	final public static int OPT_SEARCHONWRITE    = (1 << 2);
	final public static int OPT_USYNCHRONIZATION = (1 << 3);
	
	byte[] header = new byte[10];  
	byte[] eheader1 = new byte[10]; // size, flags, padding
	byte[] eheader2 = new byte[4];  // crc
	
	private ArrayList<ID3v230Frame> frameList = new ArrayList<ID3v230Frame>();
	private HashMap<String, ArrayList<ID3v230Frame>> frameMap = new HashMap<String, ArrayList<ID3v230Frame>>();
	
	
	private int options = 0;
	
	public ID3v230(byte[] header) {
		this();
		this.header = header;
	}
	
	public ID3v230() {
		this.setVersion(3, 0);
		this.setOption(OPT_DISCARDEXTHEADER, true);
		this.setOption(OPT_SEARCHONWRITE, true);
		this.setOption(OPT_USYNCHRONIZATION, false);
	}
	
	public boolean getOption(int opt) {
		return (options & opt) > 0 ? true : false;
	}
	
	public void setOption(int opt, boolean on) {
		if (on) {
			options |= opt;
		}
		else {
			options &= ~opt;
		}
	}
	
	public int[] getVersion() {
		return ByteBuffer.wrap(header, 3, 2).asIntBuffer().array();
	}
	
	protected void setVersion(int major, int minor) {
		header[3] = (byte)major;
		header[4] = (byte)minor;
	}
	
	public boolean getFlag(int flag) {
		return (header[5] & flag) > 0 ? true : false;
	}
	
	public void setFlag(int flag, boolean on) {
		if (on) {
			header[5] |= flag;
		}
		else {
			header[5] &= ~flag;
		}
	}
	
	public int getSize() {
		return Util.twentyEightBitByteArrayToInt(ByteBuffer.wrap(header, 6, 4).array()) ;
	}
	
	private void setSize(int size) {
		byte[] tsize = Util.intToTwentyEightBitByteArray(size);
		Util.byteCopy(tsize, 0, 4, header, 6);
	}

	protected int getExtendedHeaderSize() {
		return Util.byteArrayToInt(ByteBuffer.wrap(eheader1, 0, 4).array()) ;
	}
	
	private void setExtendedHeaderSize(int size) {
		byte[] tsize = Util.intToByteArray(size);
		Util.byteCopy(tsize, 0, 4, eheader1, 0);
	}

	protected boolean getExtendedHeaderFlag(byte flag) {
		return (eheader1[4] & flag) > 0 ? true : false;
	}
	
	protected void setExtendedHeaderFlag(byte flag, boolean on) {
		if (on) {
			eheader1[4] |= flag;
		}
		else {
			eheader1[4] &= ~flag;
		}
	}
	
	public int getPaddingSize() {
		return Util.byteArrayToInt(ByteBuffer.wrap(eheader1, 0, 4).array());
	}
	
	public void setPaddingSize(int psize) {
		byte[] psizeb = Util.intToByteArray(psize);
		Util.byteCopy(psizeb, 0, 4, eheader1, 0);
	}
	
	public int getCRC() {
		return Util.byteArrayToInt(eheader2);
	}
	
	public void setCRC(int crc) {
		byte[] data = Util.intToByteArray(crc);
		Util.byteCopy(data, 0, 4, eheader2, 0);
	}
	
	public ID3v230Frame[] getFrames(String frameId) {
		return (ID3v230Frame[])frameMap.get(frameId).toArray();
	}
	
	public void addFrame(ID3v230Frame frame) throws IOException {
		String frameId = frame.getBytes().toString();
		ArrayList<ID3v230Frame> typeList = frameMap.get(frameId);
		if (typeList == null) {
			typeList = new ArrayList<ID3v230Frame>();
			frameMap.put(frameId, typeList);
		}
		typeList.add(frame);
		frameList.add(frame);
	}
	
	public void removeFrames(String frameId) {
		frameMap.remove(frameId);
		ArrayList<ID3v230Frame> frameList = new ArrayList<ID3v230Frame>();
		Iterator<ID3v230Frame> i = this.frameList.iterator();
		while (i.hasNext()) {
			ID3v230Frame frame = i.next();
			if (! frame.getFrameId().toString().equalsIgnoreCase(frameId)) {
				frameList.add(frame);
			}
		}
		this.frameList = frameList;
	}
	
}
