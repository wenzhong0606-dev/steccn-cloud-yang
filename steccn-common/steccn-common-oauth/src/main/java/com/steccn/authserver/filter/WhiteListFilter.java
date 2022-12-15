package com.steccn.authserver.filter;

import com.google.common.collect.Lists;
import com.steccn.authserver.path.IgnorenPath;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@RequiredArgsConstructor
public class WhiteListFilter extends OncePerRequestFilter {

    private final IgnorenPath ignorenPath;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        //白名单路径移除请求头认证信息
        List<String> urls = Lists.newArrayList(ignorenPath.getAllIgnoredPath());
        for (String url : urls) {
            if (pathMatcher.match(url, path)) {
                request = new HttpServletRequestWrapper(request) {
                    @Override
                    public Enumeration<String> getHeaderNames() {
                        Enumeration<String> headerNames = super.getHeaderNames();
                        List<String> resList = Lists.newArrayList();
                        while (headerNames.hasMoreElements()) {
                            String s = headerNames.nextElement();
                            if (!HttpHeaders.AUTHORIZATION.equals(s)) {
                                resList.add(s);
                            }
                        }
                        return Collections.enumeration(resList);
                    }
                    @Override
                    public String getHeader(String name) {
                        if (HttpHeaders.AUTHORIZATION.equals(name)) {
                            return null;
                        }
                        return super.getHeader(name);
                    }
                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        if (HttpHeaders.AUTHORIZATION.equals(name)) {
                           return Collections.emptyEnumeration();
                        }
                        return super.getHeaders(name);
                    }
                };
            }
        }
        filterChain.doFilter(request, response);
    }
}
