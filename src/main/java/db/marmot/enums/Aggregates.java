package db.marmot.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
public enum Aggregates {
		
		non("non", "无聚合"),
		
		sum("sum", "求和"),
		
		max("max", "最大值"),
		
		min("min", "最小值"),
		
		avg("avg", "平均值"),
		
		count("count", "次数"),
		
		//update("update", "最新值"),
		
		//splice("splice", "拼装值"),
		
		count_distinct("count_distinct", "去重次数"),
		
		calculate("calculate", "计算"),;
	
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
	Aggregates(String code, String message) {
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
	 * @return AggregateType
	 */
	public static Aggregates getByCode(String code) {
		for (Aggregates _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 获取枚举集合
	 *
	 * @return List<AggregateType>
	 */
	public static List<Aggregates> getAllEnum() {
		List<Aggregates> list = new ArrayList<Aggregates>(values().length);
		for (Aggregates _enum : values()) {
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
		for (Aggregates _enum : values()) {
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
		Aggregates _enum = getByCode(code);
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
	public static String getCode(Aggregates _enum) {
		if (_enum == null) {
			return null;
		}
		return _enum.getCode();
	}
	
}