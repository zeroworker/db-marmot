package db.marmot.enums;

/**
 * @author shaokang
 */
public enum WindowType {
		
		sliding_time("slidingTime", "滑动时间窗口"),
		
		simple_time("simpleTime", "固定时间窗口");
	
	private final String code;
	private final String message;
	
	WindowType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static WindowType getByCode(String code) {
		for (WindowType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}