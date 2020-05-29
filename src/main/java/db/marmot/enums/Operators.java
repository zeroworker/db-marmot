package db.marmot.enums;

/**
 * @author shaokang
 */
public enum Operators {
		
		equals("equals", "等于"),
		
		not_equals("not_equals", "不等于"),
		
		greater_than("greater_than", "大于"),
		
		greater_equal("greater_equal", "大于等于"),
		
		less_than("less_than", "小于"),
		
		less_equal("less_equal", "小于等于"),
		
		like("like", "模糊匹配"),
		
		in("in", "包含"),
		
		inter_section("inter_section", "交集"),
		
		not_in("not_in", "不包含");
	
	private final String code;
	
	private final String message;
	
	Operators(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static Operators getByCode(String code) {
		for (Operators _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}