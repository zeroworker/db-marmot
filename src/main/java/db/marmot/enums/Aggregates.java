package db.marmot.enums;

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
		
		count_distinct("count_distinct", "去重次数"),
		
		calculate("calculate", "计算"),;
	
	private final String code;
	
	private final String message;
	
	Aggregates(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static Aggregates getByCode(String code) {
		for (Aggregates _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}