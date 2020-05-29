package db.marmot.enums;

/**
 * @author shaokang
 */
public enum TotalType {
		
		sum("sum", "求和"),
		
		max("max", "最大值"),
		
		min("min", "最小值"),
		
		avg("avg", "平均值");
	
	private final String code;
	
	private final String message;
	
	TotalType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static TotalType getByCode(String code) {
		for (TotalType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}