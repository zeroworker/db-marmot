package db.marmot.enums;

/**
 * @author shaokang
 */
public enum DownloadStatus {
		
		download_wait("download_wait", "等待下载"),
		
		download_ing("download_ing", "下载中"),
		
		download_success("download_success", "下载成功"),
		
		download_fail("download_fail", "下载失败");
	
	private final String code;
	
	private final String message;
	
	DownloadStatus(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static DownloadStatus getByCode(String code) {
		for (DownloadStatus _enum : values()) {
			if (_enum.getCode().equals(code)) {
				return _enum;
			}
		}
		return null;
	}
}