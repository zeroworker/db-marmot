package db.marmot.contorller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import db.marmot.repository.validate.Validators;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shaokang
 */
public class WebTokenAuthorize {
	
	private static final String audience ="marmot";
	private static final String secret = "6428d106f78a24743512d6c894881c5a";

	public WebTokenAuthorize() {
		throw new WebTokenException("WebTokenAuthorize 不允许实例化");
	}
	
	public static String getToken() {
		String token = JWT
                .create()
                .withAudience(audience)
                .sign(Algorithm
                        .HMAC256(secret)
                );
		return token;
	}
	
	public static void verify(String token) {
	    Validators.notNull(token,"token 不能为空");
		String audience = JWT.decode(token).getAudience().get(0);
		Validators.isTrue(StringUtils.equals(audience, audience), "token audience 不正确");
		JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
		try {
			jwtVerifier.verify(token);
		} catch (JWTVerificationException e) {
			throw new WebTokenException("token 非法", e);
		}
	}
	
	public static class WebTokenException extends RuntimeException {
		public WebTokenException(String message) {
			super(message);
		}
		
		public WebTokenException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
