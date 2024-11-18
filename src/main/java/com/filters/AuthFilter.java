package com.filters;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Component
public class AuthFilter implements Filter {
	
	public AuthFilter() {
		publicUrl.add("");
	}
	
	ArrayList<String> publicUrl = new ArrayList<>();
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, response);
	}
}
