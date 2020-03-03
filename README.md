## loServer

Q&A 见：http://wiki.yqxiu.cn/pages/viewpage.action?pageId=13697069

基于Netty的Http应用服务器

### 介绍
自己实现一个Http应用服务器来处理简单的Get和Post请求（就是请求文本，响应的也是文本或json），了解了下Http协议，发现解析起来写的东西很多，而且自己写性能也是大问题（性能这个问题为未来考虑），于是考虑用Netty，发现它有Http相关的实现，便按照Example的指示，自己实现了一个应用服务器。思想很简单，在ServerHandler中拦截请求，把Netty的Request对象转换成我自己实现的Request对象，经过用户的Action对象后，生成自己的Response，最后转换为Netty的Response返回给用户。

这个项目中使用到的Netty版本是4.X，毕竟这个版本比较稳定，而且最近也在不停更新，于是采用了。之后如果有时间，会在项目中添加更多功能，希望它最终成为一个完善的高性能的Http服务器。

### 代码逻辑
字段说明：
manual: 手动打点上报的日志，这种日志，不做判断全部接收。(之后会被rdt代替)
rdt: report_data_type 日志上报类型.
    100以内的数据，都是不用判断圈选配置，直接接收。
    1.表示以前的manual字段内容，
    2.  PC 搜索推荐链路追踪数据
    21. iOS 表示PC搜索或推荐的样例被被点击后上报的element_click日志
    22. Android 表示PC搜索或推荐的样例被被点击后上报的element_click日志
    3. 前端加的元素上报标识，如果rdt为3就直接上报，不再需要后续的圈选


ps aux | grep LogServer
echo ""
cd /data/work/log_server/classes
ll
echo ""
rm -f com.tar
ll
echo ""
mv com com.bak20190416001
ll
echo ""
download com.tar
ll
echo ""
tar -zxvf com.tar
ll
echo ""
ps aux | grep LogServer
ll
echo ""

cd ..
./stop-all.sh
ps aux | grep LogServer

./start-all.sh
ps aux | grep LogServer
tail -f logs/*.log

10.0.2.229
10.0.2.217
10.0.2.94
10.0.2.148

10.0.0.41  10.0.0.159  10.0.0.162

