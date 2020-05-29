package db.marmot.enums;

/**
 * @author shaokang
 */
public enum GraphicLayout {
		
		detail("detail", "明细"),
		
		aggregate("aggregate", "聚合");
	
	private final String code;
	
	private final String message;
	
	GraphicLayout(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}