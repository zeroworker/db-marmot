package db.marmot.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
public enum RepositoryType {
		
		volume("volume", "数据集"),
		
		graphic("graphic", "图表"),
		
		statistical("statistical", "统计"),
		
		database("database", "数据库"),
		
		custom("custom", "自定义");
	
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
	RepositoryType(String code, String message) {
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
	 * @return RepositoryType
	 */
	public static RepositoryType getByCode(String code) {
		for (RepositoryType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 获取枚举集合
	 *
	 * @return List<RepositoryType>
	 */
	public static List<RepositoryType> getAllEnum() {
		List<RepositoryType> list = new ArrayList<RepositoryType>(values().length);
		for (RepositoryType _enum : values()) {
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
		for (RepositoryType _enum : values()) {
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
		RepositoryType _enum = getByCode(code);
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
	public static String getCode(RepositoryType _enum) {
		if (_enum == null) {
			return null;
		}
		return _enum.getCode();
	}
	
}