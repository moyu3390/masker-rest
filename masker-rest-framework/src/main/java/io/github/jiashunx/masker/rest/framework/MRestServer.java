package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerInitializeException;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.type.MRestNettyThreadType;
import io.github.jiashunx.masker.rest.framework.util.MRestThreadFactory;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiashunx
 */
public class MRestServer {

    private static final Logger logger = LoggerFactory.getLogger(MRestServer.class);

    private volatile boolean started = false;

    private int listenPort;
    private String serverName;
    private int bossThreadNum = 0;
    private int workerThreadNum = 0;
    private boolean connectionKeepAlive;
    private final Map<String, MRestContext> contextMap = new ConcurrentHashMap<>();

    public MRestServer() {
        this(MRestUtils.getDefaultServerPort(), MRestUtils.getDefaultServerName());
    }

    public MRestServer(String serverName) {
        this(MRestUtils.getDefaultServerPort(), serverName);
    }

    public MRestServer(int listenPort) {
        this(listenPort, MRestUtils.getDefaultServerName());
    }

    public MRestServer(int listenPort, String serverName) {
        listenPort(listenPort);
        serverName(serverName);
        contextMap.put(Constants.DEFAULT_CONTEXT_PATH, new MRestContext(this, Constants.DEFAULT_CONTEXT_PATH));
    }

    public MRestServer listenPort(int listenPort) {
        if (listenPort <= 0 || listenPort > 65535) {
            throw new IllegalArgumentException("listenPort -> " + listenPort);
        }
        this.listenPort = listenPort;
        return this;
    }

    public MRestServer serverName(String serverName) {
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalArgumentException("serverName -> " + serverName);
        }
        this.serverName = serverName;
        return this;
    }

    public MRestServer bossThreadNum(int bossThreadNum) {
        if (bossThreadNum < 0) {
            throw new IllegalArgumentException("bossThreadNum -> " + bossThreadNum);
        }
        this.bossThreadNum = bossThreadNum;
        return this;
    }

    public MRestServer workerThreadNum(int workerThreadNum) {
        if (workerThreadNum < 0) {
            throw new IllegalArgumentException("workThreadNum -> " + workerThreadNum);
        }
        this.workerThreadNum = workerThreadNum;
        return this;
    }

    public MRestServer connectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
        return this;
    }

    public boolean isConnectionKeepAlive() {
        return this.connectionKeepAlive;
    }

    public MRestContext context() {
        return context(Constants.DEFAULT_CONTEXT_PATH);
    }

    public synchronized MRestContext context(String contextPath) {
        MRestContext context = getContext(contextPath);
        if (context == null) {
            String _ctxPath = MRestContext.formatContextPath(contextPath);
            context = new MRestContext(this, _ctxPath);
            contextMap.put(_ctxPath, context);
        }
        return context;
    }

    public MRestContext getContext(String contextPath) {
        return contextMap.get(MRestContext.formatContextPath(contextPath));
    }

    public List<String> getContextList() {
        return new ArrayList<>(contextMap.keySet());
    }

    /**
     * 检查server是否已启动
     * @throws MRestServerInitializeException MRestServerInitializeException
     */
    public void checkServerState() throws MRestServerInitializeException {
        if (started) {
            throw new MRestServerInitializeException(String.format("Server[%s] has already been initialized", serverName));
        }
    }

    /**
     * 启动server
     * @throws MRestServerInitializeException MRestServerInitializeException
     */
    public synchronized void start() throws MRestServerInitializeException {
        checkServerState();
        if (logger.isInfoEnabled()) {
            logger.info("Server[{}] start, ListenPort: {}, Context: {}", serverName, listenPort, getContextList());
        }
        try {
            contextMap.forEach((key, restContext) -> {
                restContext.init();
            });
            EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadNum, new MRestThreadFactory(MRestNettyThreadType.BOSS, listenPort));
            EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadNum, new MRestThreadFactory(MRestNettyThreadType.WORKER, listenPort));
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MRestServerChannelInitializer(this));
            Channel channel = bootstrap.bind(listenPort).sync().channel();
            if (logger.isInfoEnabled()) {
                logger.info("Server[{}] start succeed, ListenPort: {}", serverName, listenPort);
            }
            final Thread syncThread = new Thread(() -> {
                try {
                    channel.closeFuture().syncUninterruptibly();
                } catch (Throwable throwable) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Server[{}] channel close future synchronized failed", serverName, throwable);
                    }
                }
            });
            syncThread.setName(serverName + "-closeFuture.Sync");
            syncThread.setDaemon(true);
            syncThread.start();
            started = true;
        } catch (Throwable throwable) {
            throw new MRestServerInitializeException(String.format("Server[%s] start failed", serverName), throwable);
        }
    }

}
