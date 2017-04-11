package me.biezhi.wechat.util;

public class EmojiUtil {
	 
	/**
	 * Emoji表情库
	 * 	Array length:22(0-21)
	 */
	public static int[] emojiArr = {
			0x1F604, 0x1F60A, 0x1F603, 0x263A, 0x1F609, 0x1F60D, 0x1F618, 0x1F61A, 0x1F633, 0x1F60C, 0x1F61C, 
			0x1F62D, 0x1F602, 0x1F621, 0x2764, 0x1F4AA, 0x1F437, 0x1F437, 0x1F437, 0x1F435, 0x1F420, 0x1F478
	};  
	
	/** 
     * emoji表情转换(hex -> utf-16) 
     * @param emjCode 
     * @return 
     */  
	public static String emoji(int emjCode){
		return String.valueOf(Character.toChars(emjCode));
	}
	
}
