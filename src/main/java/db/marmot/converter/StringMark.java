package db.marmot.converter;

import java.util.Arrays;

/**
 * @author shaokang
 */
public class StringMark {
	
	public static final char SEPARATOR_CHAR_ASTERISK = '*';
	
	/**
	 * 把字符串mask
	 *
	 * @param str 字符串
	 * @return mask后的字符串
	 */
	public static String mask(String str) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return str;
		}
		if (len == 1) {
			return String.valueOf(SEPARATOR_CHAR_ASTERISK);
		}
		int maskLen;
		int begin;
		
		if ((len >= 8 || len <= 11) && str.matches("[A-Z]{1,2}\\d{7,10}")) {
			// 台胞证/回乡证/护照
			// 台胞证上面有两个号码：台胞证号码：0099730503(B) 身份证号码：H125039525
			if (Character.isDigit(str.charAt(1))) {
				begin = 2;
			} else {
				begin = 3;
			}
			maskLen = len - begin - 3;
		} else if ((len == 10 || len == 13) && str.matches("[A-Z]?\\d{8,10}(\\([A-Z]\\))?")) {
			// 台胞证
			// 台胞证上面有两个号码：台胞证号码：0099730503(B) 身份证号码：H125039525
			begin = 2;
			if (str.charAt(str.length() - 1) == ')') {
				maskLen = len - begin - 4;
			} else {
				maskLen = len - begin - 1;
			}
		} else if (len >= 16 && len <= 22) {
			// 卡号位于这个区间，特殊处理，保证前6后4
			maskLen = len - 6 - 4;
			begin = 6;
		} else {
			len = str.length();
			maskLen = Math.max((len) / 2, 1);
			begin = (len - maskLen) / 2;
		}
		return mask(str, begin, begin + maskLen);
	}
	
	/**
	 * 掩码指定的位数为*
	 *
	 * <p>
	 * 注意:index从0开始
	 *
	 * @param str 原字符串
	 * @param beginIndex 开始index,从0开始
	 * @param endIndex 结束index,掩码不包括此位
	 * @return 返回掩码后的字符串
	 */
	public static String mask(String str, int beginIndex, int endIndex) {
		if (str == null || str.length() == 0) {
			return str;
		}
		if (str.length() == 1) {
			return String.valueOf(SEPARATOR_CHAR_ASTERISK);
		}
		
		if (beginIndex < 0) {
			beginIndex = 0;
		}
		if (endIndex > str.length()) {
			endIndex = str.length();
		}
		int subLen = endIndex - beginIndex;
		if (subLen < 0) {
			throw new StringIndexOutOfBoundsException(subLen);
		}
		
		// 复制整个str
		char[] chars = str.toCharArray();
		char[] mask = repeatAsterisk(subLen);
		// 复制mask
		System.arraycopy(mask, 0, chars, beginIndex, subLen);
		// 复制输出
		return new String(chars);
	}
	
	protected static char[] repeatAsterisk(int len) {
		char[] chars = new char[len];
		Arrays.fill(chars, StringMark.SEPARATOR_CHAR_ASTERISK);
		return chars;
	}
}
