package db.marmot.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
public enum Operators {
		
		equals("equals", "等于"),

		not_equals("not_equals", "不等于"),

		greater_than("greater_than", "大于"),
		
		greater_equal("greater_equal", "大于等于"),
		
		less_than("less_than", "小于"),
		
		less_equal("less_equal", "小于等于"),
		
		like("like", "模糊匹配"),
		
		in("in", "包含"),
		
		inter_section("inter_section", "交集"),
		
		not_in("not_in", "不包含");
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
	Operators(String code, String message) {
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
	 * @return Operators
	 */
	public static Operators getByCode(String code) {
		for (Operators _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 获取枚举集合
	 *
	 * @return List<Operators>
	 */
	public static List<Operators> getAllEnum() {
		List<Operators> list = new ArrayList<Operators>(values().length);
		for (Operators _enum : values()) {
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
		for (Operators _enum : values()) {
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
		Operators _enum = getByCode(code);
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
	public static String getCode(Operators _enum) {
		if (_enum == null) {
			return null;
		}
		return _enum.getCode();
	}
	
}