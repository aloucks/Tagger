package net.cofront.audio.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ID3v230FrameSet {
	private ArrayList<ID3v230Frame> frameList = new ArrayList<ID3v230Frame>();
	private HashMap<String, ArrayList<ID3v230Frame>> frameMap = new HashMap<String, ArrayList<ID3v230Frame>>();
	
	
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
