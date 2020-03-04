package com.eqxiu.logserver;

import cn.hutool.core.date.DateUtil;
import com.eqxiu.logserver.action.FastPushAction;
import com.eqxiu.logserver.action.PushLogAction;
import com.eqxiu.logserver.conf.LogConfigManager;
import com.eqxiu.logserver.conf.SysConfigManager;
import com.eqxiu.logserver.handler.ActionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogServer starter<br>
 * 用于启动服务器的主对象<br>
 * 使用LogServer.start()启动服务器<br>
 * 服务的Action类和端口等设置在ServerSetting中设置
 *
 * @gyc
 */
public class LogServer {
    //	private static final Log log = StaticLog.get();
    private static Logger log = LoggerFactory.getLogger(LogServer.class);

    /**
     * 启动服务
     *
     * @param port 端口
     * @throws InterruptedException
     */
    public void start(int port) throws InterruptedException {
        long start = System.currentTimeMillis();

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("s1",true));
        EventLoopGroup workerGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("s2",true));
        String runType = SysConfigManager.getProperty("server.type");
        if("n".equals(runType)){
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

        }

        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .channel(NioServerSocketChannel.class)
//				.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    //把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse
                                    .addLast(new HttpObjectAggregator(65536))
                                    //压缩Http消息
//						.addLast(new HttpChunkContentCompressor())
                                    //大文件支持
//                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new ActionHandler());
                        }
                    });

            final Channel ch = b.bind(port).sync().channel();
            log.info("***** Welcome To LogServer on port [{}], startting spend {}ms *****", port, DateUtil.spendMs(start));
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 启动服务器
     */
    public static void start() {

        ServerSetting.setAction(PushLogAction.class);
        ServerSetting.setAction(FastPushAction.class);
        try {
            new LogServer().start(ServerSetting.getPort());
        } catch (InterruptedException e) {
            log.error("LoServer start error!", e);
        }
    }


    public static void main(String[] args) {
//		org.apache.log4j.Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
//		org.apache.log4j.Logger.getLogger("org.apache.eclipse.jetty.server").setLevel(Level.OFF); 
        /*ServerSetting.setAction("/example", ExampleAction.class);
		ServerSetting.setRoot("root");
		ServerSetting.setPort(8090);*/
        LogServer.start();
    }
}
