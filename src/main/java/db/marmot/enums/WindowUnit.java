package db.marmot.enums;

/**
 * @author shaokang
 */
public enum WindowUnit {
		
		non("non", "non"),
		
		second("second", "秒"),
		
		minute("minute", "分"),
		
		hour("hour", "小时"),
		
		day("day", "天");
	
	private final String code;
	
	private final String message;
	
	WindowUnit(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static WindowUnit getByCode(String code) {
		for (WindowUnit _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}