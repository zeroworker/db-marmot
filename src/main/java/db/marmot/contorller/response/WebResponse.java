package db.marmot.contorller.response;

import lombok.Getter;

import java.util.StringJoiner;

/**
 * @author shaokang
 */
@Getter
public class WebResponse<D> {
	
	/**
	 * 响应数据
	 */
	private D data;
	
	/**
	 * 响应状态
	 */
	private boolean status = true;
	
	/**
	 * 描述信息
	 */
	private String message = "处理成功";
	
	public WebResponse setData(D data) {
		this.data = data;
		return this;
	}
	
	public WebResponse setStatus(boolean status) {
		this.status = status;
		return this;
	}
	
	public WebResponse setMessage(String message) {
		this.message = message;
		return this;
	}
	
	@Override
	public String toString() {
		return new StringJoiner(", ", WebResponse.class.getSimpleName() + "[", "]").add("status=" + status).add("message='" + message + "'").toString();
	}
}
