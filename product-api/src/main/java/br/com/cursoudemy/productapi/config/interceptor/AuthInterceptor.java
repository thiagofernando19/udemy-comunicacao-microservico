package br.com.cursoudemy.productapi.config.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.cursoudemy.productapi.modules.jwt.service.JwtService;
import feign.Request.HttpMethod;

public class AuthInterceptor implements HandlerInterceptor{
	private static final String AUTHORIZATION = "Authorization";
	
	
	@Autowired
	private JwtService jwtService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, 
			                 HttpServletResponse response,
			                 Object handler) {
		if(isOptions(request)) {
			return true;
		}
		 		
		var authorization = request.getHeader(AUTHORIZATION);
		jwtService.validateAuthorization(authorization);
		return true;
	}
	
	private boolean isOptions(HttpServletRequest request) {
		return HttpMethod.OPTIONS.name().equals(request.getMethod());
	}
}
