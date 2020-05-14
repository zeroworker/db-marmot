package db.marmot.enums;

/**
 * @author shaokang
 */
public enum DbType {

    mysql("mysql"),

    oracle("oracle"),
    ;

    /**
     * 枚举值
     */
    private final String code;

    /**
     * @param code    枚举值
     */
    DbType(String code) {
        this.code = code;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return Returns the code.
     */
    public String code() {
        return code;
    }
}