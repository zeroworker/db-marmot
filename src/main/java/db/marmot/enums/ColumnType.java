package db.marmot.enums;

/**
 * @author shaokang
 */
public enum ColumnType {
		
		string("string", "文本"),
		
		number("number", "数字"),
		
		date("date", "时间");
	
	private final String code;
	
	private final String message;
	
	ColumnType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static ColumnType getByCode(String code) {
		for (ColumnType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}