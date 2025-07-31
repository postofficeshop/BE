package com.example.demo.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.example.demo.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final String LOCAL_REDIRECT_URL = "http://localhost:3000";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		
		TokenProvider tokenProvider = new TokenProvider();
		String token = tokenProvider.create(authentication);

		log.info("token {}", token);

		Optional<Cookie> oCookie = Arrays.stream(request.getCookies())
				.filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
				.findFirst();
		Optional<String> redirectUri = oCookie.map(Cookie::getValue);

		log.info("redirectUri {}", redirectUri);

		String targetUrl = redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL) + "/sociallogin?token=" + token;

		log.info("targetUrl {}", targetUrl);

		response.sendRedirect(targetUrl);
	}

}