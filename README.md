### 介绍
 这是一个基于Netty框架二次封装的高性能Http接口服务，增加了对http请求路由的功能，当初设计的初衷是将接收到的日志经过简单处理后快速推送到kafka ，服务于易企秀数据埋点业务，目前春节期间日处理10亿+ ；目前该项目已把业务剥离、并简化了操作，大家可放心使用。
 
 特点：简单、高效 （在最普通的机器环境压测QPS最高可以达到3w/s）

 **js-sdk** ： [页面埋点项目](https://github.com/eqxiu/tracker-js-sdk)
 
 **博客地址** ：[https://www.jianshu.com/p/3e049008204e](https://www.jianshu.com/p/3e049008204e)

### 设计
![ ](https://github.com/yanchaoguo/log-server/blob/master/img/c.png)

### 依赖
* **Netty4**
* **logback kafka appender**
* **ipip**

### 快速启动
**1、编译**
```
git clone https://github.com/yanchaoguo/log-server.git

mvn assembly:assembly
```
**2、配置**
将编译好的工程放到 /data/work/log_server，目录结构如下：
```
----log_server
          |
          ----bin           #依赖的jar包
          |
          ----classes       #编译后生成的classes目录
          |
          ----bin           #启动脚本
          |
          ----logs          #日志文件  
```
cd /data/work/log_server/bin
编辑 ls.sh 脚本，配置端口号和kafka地址
```
port=9001
kafka=hadoop006:9092,hadoop007:9092,hadoop008:9092
```
**3、启动**

```
./ls.sh start
```

### 快速开发
**1、目录说明**
![ ](https://github.com/yanchaoguo/log-server/blob/master/img/a.png)


**2、在action目录下新增业务处理类，比如创建FastPushAction并实现Action接口的doAction方法；该类不对日志进行处理，没有太多业务逻辑，负责将接收到的日志发送到kafka ，可以作为demo参考，具体实现如下**
```
/**
 * /fast_push为该业务逻辑的请求路径 ，如http://localhost:port/log-server/fast_push
 */
@Route(value = "/fast_push")
public class FastPushAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(PushLogAction.class);


    @Override
    public void doAction(Request request, Response response) {

            String logs = request.getContent();  //从body中获取日志内容
            //从参数中获取 logger 名称，为空则取默认值
            String loger = Utils.isNulDefault(request.getParam("loger"),LogConfigManager.trashTopic);

            int responseStatus = CodeManager.RESPONSE_CODE_NORMAL;

            //如果body中没有内容 尝试从url参数中获取
            if (StrUtil.isEmpty(logs) && request.getParams().size()>0) {
                logs = Utils.toJson(request.getParams());
            }
            if (StrUtil.isEmpty(logs)) {
                response.setContent("<h2>not find log content</h2>");
                responseStatus = CodeManager.RESPONSE_CODE_NOT_CONTENT;
                logger.warn("not find log content");
            }else {
                //将日志推送到kafka，其中 logger 的名称要和logback.xml中的配置一致
                KafkaLoggerFactory.getLogger(loger).info(logs);
            }
            // 设置返回信息：
            response.setStatus(responseStatus);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader(CookieHeaderNames.EXPIRES , -1);
            // 返回：
            response.send();

    }

}

```
**3、配置logback.xml**
```
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <!--trash-->
    <logger name="trash" level="INFO" additivity="false">
        <appender-ref ref="trash_kafka"/>
    </logger>


  <appender name="trash_kafka" class="com.github.danielwegener.logback.kafka.KafkaAppender">
        <encoder class="com.github.danielwegener.logback.kafka.encoding.LayoutKafkaMessageEncoder">
            <layout class="ch.qos.logback.classic.PatternLayout">
                <pattern>%msg</pattern>
            </layout>
            <charset>UTF-8</charset>
        </encoder>
        <topic>trash</topic>
        <keyingStrategy class="com.github.danielwegener.logback.kafka.keying.RoundRobinKeyingStrategy" />
        <deliveryStrategy class="com.github.danielwegener.logback.kafka.delivery.AsynchronousDeliveryStrategy" />
    	<producerConfig>bootstrap.servers=${kafka.servers}
        </producerConfig>
        <producerConfig>linger.ms=1000</producerConfig>
        <producerConfig>compression.type=none</producerConfig>
        <producerConfig>acks=0</producerConfig>
   </appender>
</configuration>

```

**4、在 LogServer.java 中注册FastPushAction**
```
 public static void start() {
        //注册action
        ServerSetting.setAction(PushLogAction.class);
        ServerSetting.setAction(FastPushAction.class);
        try {
            new LogServer().start(ServerSetting.getPort());
        } catch (InterruptedException e) {
            log.error("LoServer start error!", e);
        }
    }
```

### 性能
**4核2G单实例**
数据大小1k，压测命令如下：
```
ab -n2000000 -c1000  "http://****:9001/log-server/fast_push?debugMode=0&sdk=tracker-view.js&ver=1.1.1&d_i=2020021955e4d920&url=https%3A%2F%2Fb.scene.eprezi.cn%2Fs%2FDPawp3qi%3Fshare_level%3D10%26from_user%3D20200211285b0f8f%26from_id%3Db9509c4e-7%26share_time%3D1581400182382%26from%3Dsinglemessage%26isappinstalled%3D0%26adpop%3D1&tit=%E6%B5%B7%E8%89%BA-%E5%8C%97%E4%BA%AC%E8%88%9E%E8%B9%88%E5%AD%A6%E9%99%A2%E4%B8%AD%E5%9B%BD%E8%88%9E%E8%80%83%E7%BA%A7%E6%95%99%E6%9D%90&ref=&u_a=&bro=%E5%BE%AE%E4%BF%A1&os=Android&o_v=8.1.0&eng=Webkit&man=Xiaomi&mod=HM-6&sns=weixin-singlemessage&n_t=wifi&s_i=v3x20200219eb3eadcb&c_i=da1c0dd6ad3b19b9a86f42bdfd31c69a&u_i=&c_p=Android&b_v=2.0&c_e=0.0.1&product=traffic_view"
```

压测结果 3w/s：
```
Concurrency Level:      1000
Time taken for tests:   66.556 seconds
Complete requests:      2000000
Failed requests:        0
Write errors:           0
Total transferred:      272000000 bytes
HTML transferred:       0 bytes
Requests per second:    30049.66 [#/sec] (mean)
Time per request:       33.278 [ms] (mean)
Time per request:       0.033 [ms] (mean, across all concurrent requests)
Transfer rate:          3990.97 [Kbytes/sec] received

```
 
![ ](https://github.com/yanchaoguo/log-server/blob/master/img/b.png)
