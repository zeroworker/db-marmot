package db.marmot.enums;

/**
 * @author shaokang
 */
public enum OrderType {
		
		non("non", "不排序"),
		
		asc("asc", "顺序排序"),
		
		desc("desc", "倒叙排序");
	
	private final String code;
	
	private final String message;
	
	OrderType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static OrderType getByCode(String code) {
		for (OrderType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}