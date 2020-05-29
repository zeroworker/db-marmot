package db.marmot.enums;

/**
 * @author shaokang
 */
public enum DbType {
		
		mysql("mysql"),
		
		oracle("oracle");
	
	private final String code;
	
	DbType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public String code() {
		return code;
	}
}