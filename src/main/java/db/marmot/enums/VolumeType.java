package db.marmot.enums;

/**
 * @author shaokang
 */
public enum VolumeType {
		
		model("model", "模型"),
		
		sql("sql", "sql"),
		
		enums("enums", "枚举"),
		
		custom("custom", "自定义");
	
	private final String code;
	
	private final String message;
	
	VolumeType(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static VolumeType getByCode(String code) {
		for (VolumeType _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}