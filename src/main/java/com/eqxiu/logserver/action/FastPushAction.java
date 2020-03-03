package com.eqxiu.logserver.action;


import cn.hutool.core.util.StrUtil;
import com.eqxiu.logserver.annotation.Route;
import com.eqxiu.logserver.conf.CodeManager;
import com.eqxiu.logserver.conf.LogConfigManager;
import com.eqxiu.logserver.handler.Request;
import com.eqxiu.logserver.handler.Response;
import com.eqxiu.logserver.util.KafkaLoggerFactory;
import com.eqxiu.logserver.util.Utils;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "/fast_push")
public class FastPushAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(PushLogAction.class);


    @Override
    public void doAction(Request request, Response response) {

            String logs = request.getContent();
            String topic = Utils.isNulDefault(request.getParam("topic"),LogConfigManager.trashTopic);

            int responseStatus = CodeManager.RESPONSE_CODE_NORMAL;

            //从post body中获取参数
            if (StrUtil.isEmpty(logs)) {
                logs = Utils.toJson(request.getParams());
            }
            if (StrUtil.isEmpty(logs)) {
                response.setContent("<h2>not find log content</h2>");
                responseStatus = CodeManager.RESPONSE_CODE_NOT_CONTENT;
            }else {
                KafkaLoggerFactory.getLogger(topic).info(logs);
            }
            // 设置返回信息：
            response.setStatus(responseStatus);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader(CookieHeaderNames.EXPIRES , -1);
            // 返回：
            response.send();


    }






}