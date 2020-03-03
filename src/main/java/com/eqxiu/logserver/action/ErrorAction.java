package com.eqxiu.logserver.action;

import cn.hutool.core.util.StrUtil;
import com.eqxiu.logserver.handler.Request;
import com.eqxiu.logserver.handler.Response;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 错误堆栈Action类
 * @author Looly
 *
 */
public class ErrorAction implements Action{
	private static final Logger log = LoggerFactory.getLogger(ErrorAction.class);
	
	public final static String ERROR_PARAM_NAME = "_e";
	
	private final static String TEMPLATE_ERROR = "<!DOCTYPE html><html><head><title>LoServer - Error report</title><style>h1,h3 {color:white; background-color: gray;}</style></head><body><h1>HTTP Status {} - {}</h1><hr size=\"1\" noshade=\"noshade\" /><p>{}</p><hr size=\"1\" noshade=\"noshade\" /><h3>LoServer</h3></body></html>";

	@Override
	public void doAction(Request request, Response response) {
		Object eObj = request.getObjParam(ERROR_PARAM_NAME);
		if(eObj == null){
			response.sendError(HttpResponseStatus.NOT_FOUND, "404 File not found!");
			return;
		}
		
		if(eObj instanceof Exception){
			Exception e = (Exception)eObj;
			log.error("Server action internal error!", e);
			final StringWriter writer = new StringWriter();
			// 把错误堆栈储存到流中
			e.printStackTrace(new PrintWriter(writer));
			String content = writer.toString().replace("\tat", "&nbsp;&nbsp;&nbsp;&nbsp;\tat");
			content = content.replace("\n", "<br/>\n");
			content = StrUtil.format(TEMPLATE_ERROR, 500, request.getUri(), content);
			
			response.sendServerError(content);
		}
	}

}