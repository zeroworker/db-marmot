package db.marmot.contorller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonJsonView;
import db.marmot.contorller.response.WebResponse;
import db.marmot.repository.validate.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * @author shaokang
 */
@Slf4j
public abstract class AbstractWebController<R, D> extends AbstractController {
	
	public AbstractWebController() {
		super(false);
		super.setSupportedMethods("POST");
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		WebResponse webResponse = new WebResponse();
		try {
			String requestJson = getRequestJson(servletRequest);
			R request = deserializeRequest(requestJson);
			log.info("marmot 收到post请求 body:{}", requestJson);
			handleRequest(webResponse, request);
			log.info("marmot post请求处理完成 response:{}", webResponse);
		} catch (Exception e) {
			log.error("marmot 解析请求json body 异常", e);
			webResponse.setStatus(false).setMessage("解析请求数据异常数据格式必须为json格式");
		}
		ModelAndView modelAndView = new ModelAndView(new FastJsonJsonView());
		modelAndView.addObject(webResponse);
		return modelAndView;
	}
	
	/**
	 * 处理request
	 *
	 * @param webResponse
	 * @param request
	 */
	private void handleRequest(WebResponse webResponse, R request) {
		try {
			Validators.notNull(request, "request 为空");
			Validators.assertJSR303(request);
			Class responseDataClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
			if (responseDataClass.equals(Void.class)) {
				postHandle(request);
				return;
			}
			webResponse.setData(postHandleResult(request));
		} catch (Exception e) {
			log.error("marmot 获取post响应数据异常 request:{}", request, e);
			webResponse.setStatus(false).setMessage(e.getMessage());
		}
	}
	
	/**
	 * 获取请求数据
	 *
	 * @param httpServletRequest
	 * @return
	 * @throws IOException
	 */
	private String getRequestJson(HttpServletRequest httpServletRequest) throws IOException {
		String str;
		BufferedReader br = httpServletRequest.getReader();
		StringBuilder builder = new StringBuilder();
		while ((str = br.readLine()) != null) {
			builder.append(str);
		}
		return builder.toString();
	}
	
	/**
	 * 反序列化request json body
	 *
	 * @param requestJson
	 * @return
	 */
	protected R deserializeRequest(String requestJson) {
		return JSONObject.parseObject(requestJson, deserializeClass(requestJson));
	}
	
	/**
	 * 反序列化class
	 *
	 * @return
	 */
	protected Class<R> deserializeClass(String requestJson) {
		return (Class<R>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	/**
	 * 处理post请求
	 *
	 * @param request
	 * @return
	 */
	protected D postHandleResult(R request) {
		return null;
	}
	
	/**
	 * 处理post请求
	 *
	 * @param request
	 * @return
	 */
	protected void postHandle(R request) {
	}
}