package db.marmot.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
public enum TotalType {
		
		sum("sum", "求和"),

		max("max", "最大值"),

		min("min", "最小值"),

		avg("avg", "平均值");
	
	/**
	 * 枚举值
	 */
	private final String code;
	
	/**
	 * 枚举描述
	 */
	private final String message;
	
	/**
	 * @param code 枚举值
	 * @param message 枚举描述
	 */
	TotalType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String code() {
		return code;
	}
	
	/**
	 * @return Returns the message.
	 */
	public String message() {
		return message;
	}
	
	/**
	 * 根据<code>code</code>获取枚举
	 *
	 * @param code
	 * @return TotalType
	 */
	public static TotalType getByCode(String code) {
		for (TotalType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 获取枚举集合
	 *
	 * @return List<TotalType>
	 */
	public static List<TotalType> getAllEnum() {
		List<TotalType> list = new ArrayList<TotalType>(values().length);
		for (TotalType _enum : values()) {
			list.add(_enum);
		}
		return list;
	}
	
	/**
	 * 获取枚举集合
	 *
	 * @return List<String>
	 */
	public static List<String> getAllEnumCode() {
		List<String> list = new ArrayList<String>(values().length);
		for (TotalType _enum : values()) {
			list.add(_enum.code());
		}
		return list;
	}
	
	/**
	 * 根据<code>code</code>获取枚举描述信息
	 *
	 * @param code 枚举值
	 * @return
	 */
	public static String getMsgByCode(String code) {
		if (code == null) {
			return null;
		}
		TotalType _enum = getByCode(code);
		if (_enum == null) {
			return null;
		}
		return _enum.getMessage();
	}
	
	/**
	 * 根据枚举获取枚举值
	 *
	 * @param _enum
	 * @return
	 */
	public static String getCode(TotalType _enum) {
		if (_enum == null) {
			return null;
		}
		return _enum.getCode();
	}
	
}