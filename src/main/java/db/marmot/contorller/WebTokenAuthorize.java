package db.marmot.contorller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import db.marmot.repository.validate.ValidateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * public String getToken() {
 * 		String token = JWT
 *                 .create()
 *                 .withIssuer(issuer) //签名生成者
 * 				   .withSubject("marmot web token") //签名主题
 * 				   .withAudience("customer") //签名接受者
 * 				   .withIssuedAt(new Date()) //生成签名时间
 * 				   .withNotBefore(new Date()) //在当前时间之前 签名不可用
 * 				    .withExpiresAt(new Date(System.currentTimeMillis()+24*60*60*1000)) //签名过期时间 24 小时
 *                 .sign(Algorithm.HMAC256(secret) //签名验签方式
 *                 );
 * 		return token;
 * }
 * @author shaokang
 */
@Slf4j
public class WebTokenAuthorize extends WebMvcConfigurerAdapter {

	private static final String issuer ="marmot";
	private static final String secret = "6428d106f78a24743512d6c894881c5a";

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HandlerInterceptor() {
			@Override
			public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
				String token = request.getHeader("Authorization");
				if (StringUtils.isBlank(token)){
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,"token 不能为空");
					return false;
				}
				try {
					JWTVerifier jwtVerifier = JWT
							.require(Algorithm.HMAC256(secret))
							.withIssuer(issuer)
							.withAudience("customer")
							.withSubject("marmot web token")
							.build();
					jwtVerifier.verify(token);
				} catch (JWTVerificationException | ValidateException e) {
					response.sendError(401,e.getMessage());
					return false;
				}catch (Exception e){
					log.error("token 验证异常",e);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"未知错误");
					return false;
				}
				return true;
			}

			@Override
			public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

			}

			@Override
			public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

			}
		}).addPathPatterns("/marmot/**/**");
	}

	public static void main(String[] args) {
		String token = JWT
				               .create()
				                 .withIssuer(issuer) //签名生成者
				 				   .withSubject("marmot web token") //签名主题
				 				   .withAudience("customer") //签名接受者
				 				   .withIssuedAt(new Date()) //生成签名时间
				 				   .withNotBefore(new Date()) //在当前时间之前 签名不可用
				 				    .withExpiresAt(new Date(System.currentTimeMillis()+24*60*60*1000)) //签名过期时间 24 小时
				                 .sign(Algorithm.HMAC256(secret) //签名验签方式
				                 );

		System.out.println(token);
	}
}
