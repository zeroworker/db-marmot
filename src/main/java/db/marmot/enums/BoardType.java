package db.marmot.enums;

/**
 * @author shaokang
 */
public enum BoardType {
		
		general("general", "通用"),
		
		personal("personal", "个人");
	
	private final String code;
	
	private final String message;
	
	BoardType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static BoardType getByCode(String code) {
		for (BoardType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}