package com.eqxiu.logserver.action;

import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.eqxiu.logserver.annotation.Route;
import com.eqxiu.logserver.conf.CodeManager;
import com.eqxiu.logserver.conf.LogConfigManager;
import com.eqxiu.logserver.handler.Request;
import com.eqxiu.logserver.handler.Response;
import com.eqxiu.logserver.util.KafkaLoggerFactory;
import com.eqxiu.logserver.util.LogConvert;
import com.eqxiu.logserver.util.Utils;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Route(value = "/push")
public class PushLogAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(PushLogAction.class);



    @Override
    public void doAction(Request request, Response response) {

            String logs = request.getContent();;
            // 设置返回信息：
            Integer httpCode = CodeManager.RESPONSE_CODE_ERROR;
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader(CookieHeaderNames.EXPIRES, -1);

            // 批量记录：
            //从post body中获取参数
            if (StrUtil.isEmpty(logs)) {
                logs = Utils.toJson(request.getParams());
            }

            if (!StrUtil.isEmpty(logs)) {
                try{
                    Map<String, Object> collection = null;
                    if(JSON.isValidArray(logs)){
                        // 多条记录：
                        JSONArray array = JSON.parseArray(logs);
                        for (int i = 0; i < array.size(); i++) {
                            collection = array.getJSONObject(i).getInnerMap();
                            processRecord(request, collection);
                            KafkaLoggerFactory.getLogger(LogConfigManager.testTopic).info(Utils.toJson(collection));
                        }
                        httpCode = CodeManager.RESPONSE_CODE_NORMAL;
                    }else{
                        // 单条记录：
                        collection = request.getParams();
                        httpCode = processRecord(request, collection);
                        KafkaLoggerFactory.getLogger(LogConfigManager.testTopic).info(Utils.toJson(collection));
                    }

                }catch(Exception e){
                    e.printStackTrace();
                    logger.error(e.getMessage());
                    KafkaLoggerFactory.getLogger(LogConfigManager.trashTopic).info(logs);
                }

            }else{
                httpCode = CodeManager.RESPONSE_CODE_NOT_CONTENT;
                response.setContent("<h2>not find log content</h2>");
            }
            response.setStatus(httpCode);
            response.send();

    }


    private int processRecord(Request request, Map<String, Object> collection) {

        try {

            // ip:
            String ip = request.getIp();
            collection.put("ip", ip);
            collection.putAll(LogConvert.getIpipInfo(ip));
            // server_host:
            collection.put("srh", NetUtil.getLocalhostStr());
            // server_time:
            collection.put("s_t", System.currentTimeMillis());

            return CodeManager.RESPONSE_CODE_NORMAL;
        } catch (Exception e) {
            e.printStackTrace();
            return CodeManager.RESPONSE_CODE_ERROR;
        } finally {
        }
    }

/*

    private void fillCollectionAndLog(Request request, Map<String, Object> collection, List<LogConfigManager.EventConfig> eventConfigs, List<LogConfigManager.UrlConfig> urlConfigs, List<LogConfigManager.UrlConfig> refConfigs) {

        try {

            // 设置分类和事件：兼容之前版本，取第一个配置项
            if (eventConfigs != null && eventConfigs.size() > 0) {
                if(collection.get("cat") == null || collection.get("cat").toString().length() == 0 ){ //如果已经设置了cat，那么就不用从eventConfigs中取了
                    collection.put("cat", eventConfigs.get(0).getEventCategory());
                }

                if(collection.get("act") == null || collection.get("act").toString().length() == 0 ) { //如果已经设置了act，那么就不用从eventConfigs中取了
                    collection.put("act", eventConfigs.get(0).getEventAction());
                }
                // 设置分类和事件：[{cat:分类, act:操作}, ...]
                collection.put("cats", Constant.getCategories(eventConfigs));
            }

            if (urlConfigs != null && urlConfigs.size() > 0) {
                // 设置URL名称：[name1, name2, ...]
                collection.put("uns", Constant.getUrlNames(urlConfigs));
            }

            if (refConfigs != null && refConfigs.size() > 0) {
                // 设置REF名称：[name1, name2, ...]
                collection.put("rns", Constant.getUrlNames(refConfigs));
            }

            String ip = collection.get("ip").toString();

            // server_host:
            collection.put("srh", Constant.serverHost);

            // 国家、地区、城市：
            collection.putAll(Constant.getRegionInfo(ip));

            // 经纬度：
            if (StringUtils.isEmpty(collection.get("lat")) || StringUtils.isEmpty(collection.get("lon")))
                collection.putAll(Constant.getGeoInfo(ip));

            // server_time:
            collection.put("s_t", System.currentTimeMillis());

            // 处理element_data：
            collection.put("e_d", Constant.getElementData(collection.remove("e_d")));

            // 网络类型：n_t_2
            collection.put("n_t_2", Constant.netTypeMap.getOrDefault(collection.get("n_t"), "Others"));
            // 运营商类型：m_o_2
            collection.put("m_o_2", Constant.mobileOperatorMap.getOrDefault(collection.get("m_o"), "Others"));
            // 手机品牌：man_2
            collection.put("man_2", Constant.manufacturerMap.getOrDefault(collection.get("man"), "Others"));
            // 操作系统：os_2
            collection.put("os_2", Constant.getOsFrom(collection));
//            collection.put("os_2", Constant.operationSystemMap.getOrDefault(collection.get("os"), "Others"));

            // 记录日志：
            String sdk = request.getParam("sdk");
            if ("tracker-view.js".equals(sdk)) {
                // 域名白名单过滤：
                boolean flag = false;
                String url = (String) collection.get("url");
                for (String domain : LogConfigManager.getDomains()) {
                    if (url.contains(domain)) {
                        flag = true;
                        break;
                    }
                }

                if (flag) {
                    // 清理冗余字段
                    collection = Constant.clearTrackerViewCollection(collection);
                    collection = Constant.changeSimpleValueToString(collection);

                    // 按topic取logger，写日志
                    String topic = (String) collection.remove("product"); // 以前端传入的product作为topic标识，同时移除这个字段
                    if (StringUtils.isEmpty(topic) || "tracker_view".equalsIgnoreCase(topic))
                    {
                        topic = "tracker_view"; // 默认存入tracker_view中
//                        KafkaLoggerFactory.getLogger(topic).info(Utils.getGson2().toJson(collection));
                        log2kafka(topic,Utils.getGson2().toJson(collection));
                    }else{
                        collection.put("product",topic);

                        Object btObj = collection.get("b_t");
                        if(btObj != null ){
                            int btLength = btObj.toString().length();
                            if(btLength > 0 && btLength < 20){
                                collection.put("b_t",btObj.toString());
                            }else{
                                collection.put("b_t","invalid");
                            }
                        }else{
                            collection.put("b_t","def");
                        }

                        topic = Constant.smallTopic;

                        try {
                            String product = collection.get("product").toString();
                            if (!LogConfigManager.getProductList().contains(product)) {
//                                logger.error(product+"未在大数据配置");
                                collection.put("trash_type","invalid_domain");
//                                collection.put("server_ip",Constant.serverHost);
                                log2kafka(Constant.trashTopic,Utils.getGson2().toJson(collection));
                            }else{
                                log2kafka(topic,Utils.getGson2().toJson(collection));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
//                    logger.error("无效域名：" + url);
                    collection.put("url",url);
                    log2kafka(Constant.trashTopic,Utils.getGson2().toJson(collection));
                }

            } else {
                collection = Constant.clearTrackerCollection(collection);
                collection = Constant.changeSimpleValueToString(collection);
                // kafka topic : tracker
                log2kafka("tracker",Utils.getGson2().toJson(collection));
            }
        } catch (Exception e) {
//            KafkaLoggerFactory.getLogger(Constant.trashTopic).info(Utils.getGson2().toJson(collection));
            log2kafka(Constant.trashTopic,Utils.getGson2().toJson(collection));
            e.printStackTrace();
        } finally {
        }
    }
*/

/*

    // 校验参数是否合法：
    private static String check(Map<String, Object> params) {
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName) != null ? params.get(paramName).toString() : "";
            String valuePattern = LogConfigManager.getFieldConfigMap().get(paramName);

            // 没有参数配置，直接返回false
            if (valuePattern == null) {
                logger.error("未知参数：" + paramName + " = " + paramValue);
                return "unknown_"+paramName;
            }

            // 有参数配置，检验是否匹配表达式
            if (!paramValue.matches(valuePattern)) {
                logger.error("错误参数名：" +paramName + ",值为" + paramValue + " valuePattern："+valuePattern);
                return "not_match"+paramName+"_"+paramValue;
            }
        }
        return null;
    }
*/


}