package com.eqxiu.logserver.handler;

import cn.hutool.core.util.StrUtil;
import com.eqxiu.logserver.ServerSetting;
import com.eqxiu.logserver.action.Action;
import com.eqxiu.logserver.action.ErrorAction;
import com.eqxiu.logserver.filter.Filter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Action处理单元
 *
 * @author Looly
 */
public class ActionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LoggerFactory.getLogger(ActionHandler.class);
    private static String className = "com.eqxiu.logserver.handler.ActionHandler";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        try {
            final Request request = Request.build(ctx, fullHttpRequest);
            final Response response = Response.build(ctx, request);
            try {
                setMyCookie(request, response);
                addMyHeader(request, response);
                // do filter
                boolean isPass = this.doFilter(request, response);

                if (isPass) {
                    // do action

                    this.doAction(request, response);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Action errorAction = ServerSetting.getAction(ServerSetting.MAPPING_ERROR);
                request.putParam(ErrorAction.ERROR_PARAM_NAME, e);
                errorAction.doAction(request, response);
            }

            // 如果发送请求未被触发，则触发之，否则跳过。
            if (false == response.isSent()) {
                response.send();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error uri: "+fullHttpRequest.getUri());
        } finally {
        }
    }

    private void addMyHeader(Request request, Response response) {
        if (!StrUtil.isEmpty(request.getHeader("Origin"))) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        }
//		response.addHeader("Vary", "Orign");
    }

    private void setMyCookie(Request request, Response response) {
//        if (request.getCookie(Constants.EQS_COOKIEID_KEY) == null) {
//            response.addCookie(Constants.EQS_COOKIEID_KEY, "c" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6), Integer.MAX_VALUE);
//        }
//        if (request.getCookie(Constants.EQS_SESSIONID_KEY) == null) {
//            String emptyDomain = Constants.EQS_SESSIONID_KEY + "=" + "s" + DateUtil
//                    .format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6)
//                    + ";Domain=;Path=/";
//            Cookie cookie = ClientCookieDecoder.STRICT.decode(emptyDomain);
//            response.addCookie(cookie);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.warn("{}", cause.getMessage());
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    // ----------------------------------------------------------------------------------------
    // Private method start

    /**
     * 执行过滤
     *
     * @param request  请求
     * @param response 响应
     */
    private boolean doFilter(Request request, Response response) {
        // 全局过滤器
        Filter filter = ServerSetting.getFilter(ServerSetting.MAPPING_ALL);
        if (null != filter) {
            if (false == filter.doFilter(request, response)) {
                return false;
            }
        }

        // 自定义Path过滤器
        filter = ServerSetting.getFilter(request.getPath());
        if (null != filter) {
            if (false == filter.doFilter(request, response)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 执行Action
     *
     * @param request  请求对象
     * @param response 响应对象
     */
    private void doAction(Request request, Response response) {
        Action action = ServerSetting.getAction(request.getUri().split("\\?")[0]);
        if (null == action) {
            // 查找匹配所有路径的Action
            action = ServerSetting.getAction(ServerSetting.MAPPING_ALL);

        }

        action.doAction(request, response);
    }
    // ----------------------------------------------------------------------------------------
    // Private method start
}
