package com.eqxiu.logserver.conf;

public interface CodeManager {
	int success = 10200;
	int not_found = 10404;
	int server_error = 10501;
	int server_unable = 10503; //服务内部错误
	int not_found_param = 10504; //没有发现足够的参数
	
	int request_method_no_match = 10601; //请求方式错误
	
	int log_check_empty = 21001 ;//2:日志业务 1：push操作 001：逻辑
	int log_parse_error = 21002 ;//json转换异常

	int RESPONSE_CODE_NORMAL = 204;
	int RESPONSE_CODE_INVALID_DOMAIN = 1000;
	int RESPONSE_CODE_INVALID_EVENT_CONFIGE = 1001;
	int RESPONSE_CODE_INVALID_PRODUCT = 1002;
	int RESPONSE_CODE_PRODUCT_NAME_NOT_SET = 1003;
	int RESPONSE_CODE_NOT_CONTENT = 1004;
	int RESPONSE_CODE_ERROR = 500;
}
