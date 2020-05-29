package db.marmot.enums;

/**
 * @author shaokang
 */
public enum GraphicType {
		
		cross_tab("cross_tab", "表格");
	
	private final String code;
	
	private final String message;
	
	GraphicType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}

	public static GraphicType getByCode(String code) {
		for (GraphicType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}

}