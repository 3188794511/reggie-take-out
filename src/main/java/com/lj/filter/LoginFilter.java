package com.lj.filter;

import com.alibaba.fastjson.JSON;
import com.lj.common.BaseContext;
import com.lj.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginFilter",urlPatterns = "/*")
public class LoginFilter implements Filter {
    //路径匹配器,支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    //拦截未登录请求
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //不需要拦截的请求
        String[] excludeURIs =
                {
                    "/backend/**",
                    "/front/**",
                    "/employee/login",
                    "/employee/logout",
                    "/common/upload",
                    "/user/code",
                    "/user/login",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources",
                    "/v2/api-docs"
                };
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();//本次请求的URI
        //判断当前请求是否需要被拦截
        if (isExcludeURI(excludeURIs,requestURI)) {
            //不需要被拦截
            filterChain.doFilter(request,response);
            return;
        }
        //需要被拦截
        log.info("被拦截的请求:{}",requestURI);
        if(request.getSession().getAttribute("employee") != null){
            //将员工的id存储到当前线程中
            BaseContext.setCurrenId((Long) request.getSession().getAttribute("employee"));
            //员工session不为空,放行
            filterChain.doFilter(request,response);
            return;
        }
        if(request.getSession().getAttribute("user") != null){
            //将员工的id存储到当前线程中
            BaseContext.setCurrenId((Long) request.getSession().getAttribute("user"));
            //员工session不为空,放行
            filterChain.doFilter(request,response);
            return;
        }
        //员工session为空,不放行
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));//返回响应信息
        return;
    }
    //判断当前请求是否为不需要拦截的请求
    public boolean isExcludeURI(String[] excludeURIs,String requestURI){
        for (String excludeURI : excludeURIs) {
            if(PATH_MATCHER.match(excludeURI,requestURI)){
                return true;
            }
        }
        return false;
    }
}
