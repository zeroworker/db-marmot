package db.marmot.enums;

/**
 * @author shaokang
 */
public enum GraphicCycle {
		
		non("non", "无周期"),
		
		second("second", "秒"),
		
		minute("minute", "分"),
		
		hour("hour", "小时"),
		
		day("day", "天"),
		
		week("week", "周"),
		
		month("month", "月"),
		
		season("season", "季"),
		
		year("year", "年");
	
	private final String code;
	
	private final String message;
	
	GraphicCycle(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static GraphicCycle getByCode(String code) {
		for (GraphicCycle _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}