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
	
	/**
	 * 颜色编码
	 */
	private final String code;
	
	/**
	 * 颜色名称
	 */
	private final String message;
	
	/**
	 * 颜色值
	 */
	private final Short color;
	
	DataColor(String code, String message, int color) {
		this.code = code;
		this.message = message;
		this.color = (short) color;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
	
	public Short getColor() {
		return color;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String code() {
		return code;
	}
	
	/**
	 * @return Returns the message.
	 */
	public String message() {
		return message;
	}
	
	/**
	 * @return Returns the message.
	 */
	public Short color() {
		return color;
	}
	
	/**
	 * 根据<code>code</code>获取枚举
	 *
	 * @param code
	 * @return DataColor
	 */
	public static DataColor getByCode(String code) {
		for (DataColor _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 根据枚举获取枚举值
	 *
	 * @param _enum
	 * @return
	 */
	public static String getCode(DataColor _enum) {
		if (_enum == null) {
			return null;
		}
		return _enum.getCode();
	}
	
}