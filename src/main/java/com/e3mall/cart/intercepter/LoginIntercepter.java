package com.e3mall.cart.intercepter;

import com.e3mall.pojo.TbUser;
import com.e3mall.sso.service.TokenService;
import com.e3mall.utils.CookieUtils;
import com.e3mall.utils.E3Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by qimenggao on 2017/12/30.
 */
public class LoginIntercepter implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;

    @Value("${COOKIE_NAME}")
    private String COOKIE_NAME;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        // 执行handler方法之前执行此方法
        // 1、实现一个HandlerInterceptor接口。
        // 2、在执行handler方法之前做业务处理
        // 3、从cookie中取token。使用CookieUtils工具类实现。
        String token = CookieUtils.getCookieValue(request, COOKIE_NAME);
        // 4、没有取到token，用户未登录。放行
        if (StringUtils.isBlank(token)) {
            return true;
        }
        // 5、取到token，调用sso系统的服务，根据token查询用户信息。
        E3Result e3Result = tokenService.getUserByToken(token);
        // 6、没有返回用户信息。登录已经过期，未登录，放行。
        if (e3Result.getStatus() != 200) {
            return true;
        }
        // 7、返回用户信息。用户是登录状态。可以把用户对象保存到request中，在Controller中可以通过判断request中是否包含用户对象，确定是否为登录状态。
        TbUser user = (TbUser) e3Result.getData();
        request.setAttribute("user", user);
        //返回true放行
        //返回false拦截
        return true;

    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
