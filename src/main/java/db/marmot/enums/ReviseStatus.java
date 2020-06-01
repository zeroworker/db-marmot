package db.marmot.enums;

/**
 * @author shaokang
 */
public enum ReviseStatus {
		
		roll_backing("roll_backing", "回滚中"),
		
		rolled_back("rolled_back", "已回滚"),
		
		revising("revising", "订正中"),
		
		revised("revised", "已订正");
	
	private final String code;
	private final String message;
	
	ReviseStatus(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static ReviseStatus getByCode(String code) {
		for (ReviseStatus _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
	
}