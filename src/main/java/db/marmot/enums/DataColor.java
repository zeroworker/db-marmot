package db.marmot.enums;

/**
 * @author shaokang
 */
public enum DataColor {
		
		black("black", "黑色", 8),
		
		red("red", "红色", 10),
		
		green("green", "绿色", 17),
		
		blue("blue", "蓝色", 12),
		
		yellow("yellow", "黄色", 13);
	
	private final String code;
	private final String message;
	private final Short color;
	
	DataColor(String code, String message, int color) {
		this.code = code;
		this.message = message;
		this.color = (short) color;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Short getColor() {
		return color;
	}
	
	public static DataColor getByCode(String code) {
		for (DataColor _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}