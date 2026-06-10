package com.dokseosil.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

/** /admin/* 경로는 세션 로그인 필요. */
@WebFilter(filterName = "authFilter", urlPatterns = {"/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hr = (HttpServletRequest) req;
        HttpServletResponse hs = (HttpServletResponse) res;
        HttpSession session = hr.getSession(false);
        boolean logged = session != null && Boolean.TRUE.equals(session.getAttribute("admin"));
        if (!logged) {
            hs.sendRedirect(hr.getContextPath() + "/login");
            return;
        }
        chain.doFilter(req, res);
    }
}
