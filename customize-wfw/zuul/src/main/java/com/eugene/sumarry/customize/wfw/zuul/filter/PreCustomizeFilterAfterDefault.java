package com.eugene.sumarry.customize.wfw.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

@Component
public class PreCustomizeFilterAfterDefault extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(PreCustomizeFilterAfterDefault.class);

    /**
     * 指定过滤器的类型
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 过滤器的执行顺序
     * @return
     */
    @Override
    public int filterOrder() {
        /**
         * FilterConstants.PRE_DECORATION_FILTER_ORDER是zuul的默认pre类型的过滤器
         * 此过滤器执行完毕后, zuul的上下文中(threadLocal)就会存在request和response的相关信息
         * 所以，如果我们要获取request的话，最好设置在默认pre过滤器后面执行
         *
         */
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    /**
     * 标记是否支持
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        logger.info("Pre customize filter after default");
        RequestContext ctx = RequestContext.getCurrentContext();

        logger.info("经过zuul处理后访问微服务的uri: {}, 访问的微服务id: {}", ctx.get(FilterConstants.REQUEST_URI_KEY), ctx.get(FilterConstants.SERVICE_ID_KEY));

        return null;
    }
}
