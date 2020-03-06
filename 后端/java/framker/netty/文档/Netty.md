# Netty

## 一、Netty是什么

Netty是 *一个异步事件驱动的网络应用程序框架，* 
用于快速开发可维护的高性能协议服务器和客户端。

Netty是一个NIO客户端服务器框架，可以快速轻松地开发网络应用程序，例如协议服务器和客户端。它极大地简化和简化了TCP和UDP套接字服务器等网络编程。

**特征**

设计

- 适用于各种传输类型的统一API-阻塞和非阻塞套接字
- 基于灵活且可扩展的事件模型，可将关注点明确分离
- 高度可定制的线程模型-单线程，一个或多个线程池，例如SEDA
- 真正的无连接数据报套接字支持（从3.1开始）

使用方便

- 记录良好的Javadoc，用户指南和示例
- 没有其他依赖关系，JDK 5（Netty 3.x）或6（Netty 4.x）就足够了
  - 注意：某些组件（例如HTTP / 2）可能有更多要求。请参阅 [需求页面](https://netty.io/wiki/requirements.html) 以获取更多信息。

性能

- 更高的吞吐量，更低的延迟
- 减少资源消耗
- 减少不必要的内存复制

**应用场景：**

- RPC服务框架
- 游戏
- 大数据

![img](H:\学习\后端\java\framker\netty\图片\components.png)

## 二、Netty的模块以及组成

### **结构说明**

- event model：可扩展的事件模型
- Universal Communication API:统一的通讯API
- zero-copy-capable byte buffer:零拷贝buffer,支持动态扩容

- channel:socket支持http,nio,oio (AIO基于操作系统实现 )

- transport service:传输层服务（socket&diagram、http）
- protocl support:应用层协议支持

### Netty模块组件

- bootstrap:netty服务端和客户端
- buffer:缓冲相关,对NIO Buffer做了一些优化、封装
- channel:处理客户端和服务端的连接通道
- container：连接其他容器的代码
- handler:实现协议编解码的功能
- logging/util:日志/工具类

------

问题：

1、netty各个模块之间的作用？

### Netty的线程模型

主从多线程Reactor模型

![1579053172445](H:\学习\后端\java\framker\netty\图片\1579053172445.png)

Netty的线程模型主要以主从多线程Reactor修改而来

![1579074559630](H:\学习\后端\java\framker\netty\图片\1579074559630.png)

**对应模型类的关系**

```text
MainReactor=NioServerBoss  （MainReactor主要是接收客户端的连接请求，并将SocketChannel绑定到SubReactor）
SubReactor=NioWorker	（SubReactor主要是处理各个连接的IO的操作）
```

![1579233679520](H:\学习\后端\java\framker\netty\图片\1579233679520.png)

- event model：可扩展的事件模型
  Universal Communication API:统一的通讯API

- zero-copy-capable byte buffer:零拷贝buffer,支持动态扩容

- channel:socket支持http,nio,oio (AIO基于操作系统实现 )

- transport service:传输层服务

  socket&diagram
  http
  protocl support:应用层协议支持

### Netty的异步模型

![1579240161157](H:\学习\后端\java\framker\netty\图片\1579240161157.png)





![1579240343067](H:\学习\后端\java\framker\netty\图片\1579240343067.png)

## 三、Netty的初入门

### Netty的简单服务器编写

```java

            //1.创建bossGroup，用于接收客户端socket请求，连接并绑定到workGroup上
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            //2.创建workGroup，用于处理客户端的io请求
            EventLoopGroup workGroup = new NioEventLoopGroup();
            //3.创建启动服务
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //4.绑定服务配置
        try {
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)//服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列得到的连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//设置保存活动链接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道
                        //设置pipeline的处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ServerHandler());// 添加处理器
                        }
                    });//workGroup的对应管道设置处理器
            //5.服务器启动,绑定端口并且同步返回ChannelFuture
            ChannelFuture channelFuture = serverBootstrap.bind(6666).sync();
            channelFuture.channel().closeFuture().sync();

        } finally {
            //关闭bossGroup线程组
            bossGroup.shutdownGracefully();
            //关闭workGroup线程组
            workGroup.shutdownGracefully();
        }
```

Netty的简单服务器处理器

```java
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 通道读取到的数据，
     * @param ctx  ChannelHandlerContext是通道的上下文，可以获得通道，处理器等数据
     * @param msg  msg是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf是netty封装的数据缓冲，本质上是字节数组
        ByteBuf msgByteBuf = (ByteBuf) msg;
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        log.info("客户端" + socketAddress.toString() + "发送的数据是： " + msgByteBuf.toString(CharsetUtil.UTF_8));
        //super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("读取客户端数据完毕");
        log.info("正在回复");
        //服务器回复信息时，需要进行编码  "已经成功接受到消息".getBytes(CharsetUtil.UTF_8)
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("已经成功接受到消息", CharsetUtil.UTF_8));
       // super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        log.info("发生异常");
        ctx.channel().close();
    }
}
```

### Netty的简单客户端编写

```java

        //1.客户端需要一个无限循环的的事件循环组
        EventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        //2.创建客户端的启动对象
        Bootstrap bootstrap = new Bootstrap();
        //3.设置客户端的配置
        try {
            bootstrap.group(nioEventLoopGroup) //设置线程组
                    .channel(NioSocketChannel.class) //设置客户端通道的实现类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientHandler());// 添加处理器
                        }
                    });
            //客户端进行连接
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6666).sync();
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("这是我写出的数据", CharsetUtil.UTF_8));
            //监听通道关闭信息
            channelFuture.channel().closeFuture().sync();
        }finally {
            //关闭线程组
            nioEventLoopGroup.shutdownGracefully();
        }
```

Netty的简单客户端处理器

```java
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        log.info("客户端已经连接:" + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("客户端发送消息", CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        ByteBuf msgByteBuf = (ByteBuf) msg;
        log.info("客户端读取到数据:" + msgByteBuf.toString(CharsetUtil.UTF_8));
    }
}
```

### Netty简单http服务器编写

```java
log.info(channelHandlerContext.channel().toString());
        log.info(this.toString());
        if (is100ContinueExpected(msg)) {
            channelHandlerContext.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE));
        }
        // 获取请求的uri
        String uri = msg.uri();
        String msgbody = "<html><head><title>test</title></head><body>你请求uri为：" + uri+"</body></html>";
        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(msgbody, CharsetUtil.UTF_8));
        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        // 将html write到客户端
       channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
```

```java
 protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //socketChannel.pipeline().addLast(new ServerHandler());// 添加处理器
                            socketChannel.pipeline().addLast(new HttpServerCodec());//添加http处理器
                            socketChannel.pipeline().addLast("httpAggregator",new HttpObjectAggregator(512*1024));
                            socketChannel.pipeline().addLast(new SimpleServerHandler());
                        }
```



### Netty服务器解析：

*1.Netty在生成bossGroup或者workGroup时，默认有几个线程？*

答：bossGroup和workGroup都是依靠NioEventLoopGroup实现，而NioEventLoopGroup的的构造方法可以指定当前线程的数量，如果不指定则以系统当前cpu核心数的2倍

NioEventLoopGroup的构造函数（列举几个）：

```java
  public NioEventLoopGroup() {
        this(0);
    }

    public NioEventLoopGroup(int nThreads) {
        this(nThreads, (Executor)null);
    }

    public NioEventLoopGroup(ThreadFactory threadFactory) {
        this(0, (ThreadFactory)threadFactory, SelectorProvider.provider());
    }

    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, SelectorProvider.provider());
    }

    public NioEventLoopGroup(int nThreads, Executor executor) {
        this(nThreads, executor, SelectorProvider.provider());
    }

    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectorProvider selectorProvider) {
        this(nThreads, threadFactory, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
    }

    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectorProvider selectorProvider, SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, threadFactory, new Object[]{selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject()});
    }
```

最终会使用的父类MultithreadEventLoopGroup的构造方法

```java
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultithreadEventLoopGroup.class);
    private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
//获取当前cpu的核心数
    protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
    }

    protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
    }

    protected MultithreadEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
    }

```

如果指定了线程，则会调用MultithreadEventExecutorGroup的构造方法,指定对应线程数的事件处理器

```java
 protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
     .....
         this.terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
     .....
         if (executor == null) {
                executor = new ThreadPerTaskExecutor(this.newDefaultThreadFactory());
            }
     .....
          this.children = new EventExecutor[nThreads];
     
 }
```

------

2.为什么要指定线程数是核心数的两倍？

答：这个涉及到并发中如何计算线程池最佳线程数，参考文章[Java并发（八）计算线程池最佳线程数](https://www.cnblogs.com/jpfss/p/11016169.html)

IO密集型 = 2Ncpu（可以测试后自己控制大小，2Ncpu一般没问题）（常出现于线程中：数据库数据交互、文件上传下载、网络数据传输等等）

计算密集型 = Ncpu（常出现于线程中：复杂算法）

获取核心线程数

```java
Runtime.getRuntime().availableProcessors();
```

3.Netty如何对HTTP请求的注意点

答：由于http请求响应完成后需要断开相应的连接，不然会使客户端一直处于响应状态，所以需要手动关闭连接

```java
channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
```

4.Netty中的handler是多线程运行吗

答：不是，一旦事件触发一个线程，则会重新初始化一个handler进行处理，所以handler是多例存在的

> 16:26:24.920 [nioEventLoopGroup-3-1] INFO com.flanderstudy.nettydemo.handler.SimpleServerHandler - io.netty.channel.nio.NioEventLoop@702657cc
> 16:26:24.920 [nioEventLoopGroup-3-1] INFO com.flanderstudy.nettydemo.handler.SimpleServerHandler - com.flanderstudy.nettydemo.handler.SimpleServerHandler@64f3e18c
> 16:26:24.954 [nioEventLoopGroup-3-1] INFO com.flanderstudy.nettydemo.handler.SimpleServerHandler - io.netty.channel.nio.NioEventLoop@702657cc
> 16:26:24.954 [nioEventLoopGroup-3-1] INFO com.flanderstudy.nettydemo.handler.SimpleServerHandler - com.flanderstudy.nettydemo.handler.SimpleServerHandler@29d2fe2d









## 四、Netty的模块：

### TaskQueue（任务队列）

![1579232155869](H:\学习\后端\java\framker\netty\图片\1579232155869.png)

普通任务简单示例，此时该任务为异步执行，任务放在TaskQueue，符合队列的特性，任务按顺序执行，只有执行完一个任务后才会执行下一个，并且执行的线程为EvenLoop

```
ctx.channel().eventLoop().execute(() ->{    System.out.println("正在执行");});
```

定时任务简单示例，任务放在ScheduledTaskQueue

```
ctx.channel().eventLoop().scheduled(() ->{    System.out.println("正在执行");});
```





### Future

![1579240324606](H:\学习\后端\java\framker\netty\图片\1579240324606.png)

![1579240595145](H:\学习\后端\java\framker\netty\图片\1579240595145.png)

![1579240643658](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1579240643658.png)



### BootStrap

![1579248233140](H:\学习\后端\java\framker\netty\图片\1579248233140.png)



### 调度模块

#### EventLoopGroup（调度线程组）

EventLoopGroup是Netty的调度模块，channel的连接以及绑定，IO之间的操作都是由其调度完成。

EventLoopGroup中包含中许多EventLoop，具体的功能是由EventLoop实现的

![img](H:\学习\后端\java\framker\netty\图片\webp)

EventLoopGroup接口

```java
public interface EventLoopGroup extends EventExecutorGroup {
    EventLoop next();  //根据chooser选择下一个线程

    ChannelFuture register(Channel var1); 

    ChannelFuture register(ChannelPromise var1);  //注册绑定到

    /** @deprecated */
    @Deprecated
    ChannelFuture register(Channel var1, ChannelPromise var2);
}
```

![img](H:\学习\后端\java\framker\netty\图片\171517_TRn8_3101476.png)

可以看到实际上EventLoopGroup包含的模块有

- EventLoopGroup （线程循环调度组）
- EventLoop  （循环调度线程）
- EventExecutorGroup  （线程执行组）
- EventExecutor  （线程执行器）

EventLoopGroup的具体实现类有：

![1579317524413](H:\学习\后端\java\framker\netty\图片\1579317524413.png)

可以看到EventLoopGroup与EventLoop是一一对应的，大致可分为DefaultEventLoopGroup、EmbeddedEventLoopGroup、EpollEventLoopGroup、KQueueEventLoopGroup、NioEventLoopGroup

其中SingleThreadEventLoop即为单线程运行调度，MultithreadEventLoopGroup即为多线程调度组

- DefaultEventLoopGroup（创建一个BIO的线程组）
- EmbeddedEventLoopGroup （嵌入式的线程组，更多是用于测试使用）
- KQueueEventLoopGroup （ macosx专属）
- EpollEventLoopGroup （linux专属，现在只有**Linux kernels >= 2.6**，是epoll模式，可以获得更好的GC，更好的效果）
- NioEventLoopGroup （创建一个NIO线程组）

------

下面重点分析NioEventLoopGroup 

NioEventLoopGroup是Netty线程模型中，bossGroup和workGroup的具体实现，

```java
  public NioEventLoopGroup() {
        this(0); // 规定线程数为0，采用当前cpu核心数的两倍线程，cpuio密集型的一般做法
    }

    public NioEventLoopGroup(int nThreads) { 
        this(nThreads, (Executor)null);  //ThreadFactory netty执行task任务使用的线程工厂
    }

    public NioEventLoopGroup(ThreadFactory threadFactory) {
        this(0, (ThreadFactory)threadFactory, SelectorProvider.provider());
    }

    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, SelectorProvider.provider());
    }
	...............................
```

这些构造方法最终都会调用的父类MultithreadEventLoopGroup的MultithreadEventExecutorGroup中的构造方法

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
        this.terminatedChildren = new AtomicInteger();
        this.terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        } else {
            if (executor == null) {
                //若无指定，则默认为ThreadPerTaskExecutor
                executor = new ThreadPerTaskExecutor(this.newDefaultThreadFactory());
            }
            //创建nThreads数量的子线程
            this.children = new EventExecutor[nThreads];
            int j;
            for(int i = 0; i < nThreads; ++i) {
                boolean success = false;
                boolean var18 = false;
                try {
                    var18 = true;
                    //初始化每一个子线程
                    this.children[i] = this.newChild((Executor)executor, args);
                    success = true;
                    var18 = false;
                } catch (Exception var19) {
                    throw new IllegalStateException("failed to create a child event loop", var19);
                } finally {
                    if (var18) {
                        if (!success) {
                            int j;
                            for(j = 0; j < i; ++j) {
                                this.children[j].shutdownGracefully();
                            }
                            for(j = 0; j < i; ++j) {
                                EventExecutor e = this.children[j];
                                try {
                                    while(!e.isTerminated()) {
                                        e.awaitTermination(2147483647L, TimeUnit.SECONDS);
                                    }
                                } catch (InterruptedException var20) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }

                    }
                }
                if (!success) {
                    for(j = 0; j < i; ++j) {
                        this.children[j].shutdownGracefully();
                    }

                    for(j = 0; j < i; ++j) {
                        EventExecutor e = this.children[j];

                        try {
                            while(!e.isTerminated()) {
                                e.awaitTermination(2147483647L, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException var22) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            //初始化子线程选择器
            this.chooser = chooserFactory.newChooser(this.children);
            FutureListener<Object> terminationListener = new FutureListener<Object>() {
                public void operationComplete(Future<Object> future) throws Exception {
                    if (MultithreadEventExecutorGroup.this.terminatedChildren.incrementAndGet() == MultithreadEventExecutorGroup.this.children.length) {
                     
                        MultithreadEventExecutorGroup.this.terminationFuture.setSuccess((Object)null);
                    }

                }
            };
            EventExecutor[] var24 = this.children;
            j = var24.length;

            for(int var26 = 0; var26 < j; ++var26) {
                EventExecutor e = var24[var26];
                e.terminationFuture().addListener(terminationListener);
            }
            Set<EventExecutor> childrenSet = new LinkedHashSet(this.children.length);
            Collections.addAll(childrenSet, this.children);
            //记录属性为可读的线程
            this.readonlyChildren = Collections.unmodifiableSet(childrenSet);
        }
    }
```

可以看出该构造器的入参的意义为：

- int nThreads,  该线程组中线程的数量
- Executor executor,  创建子线程时使用的执行器，
- EventExecutorChooserFactory chooserFactory,  选择子线程的方法
- Object... args， 额外参数

其中会调用this.newChild((Executor)executor, args)去生成子线程

NioEventLoopGroup的newChild((Executor)executor, args)

```java
protected EventLoop newChild(Executor executor, Object... args) throws Exception {
        EventLoopTaskQueueFactory queueFactory = args.length == 4 ? (EventLoopTaskQueueFactory)args[3] : null;
        return new NioEventLoop(this, executor, (SelectorProvider)args[0], ((SelectStrategyFactory)args[1]).newSelectStrategy(), (RejectedExecutionHandler)args[2], queueFactory);
    }  //生成4个参数的意思解析，SelectorProvider：Selector的提供者， SelectorProvider.provider()    SelectStrategyFactory：生成Select策略的工厂，DefaultSelectStrategyFactory.INSTANCE RejectedExecutionHandler：拒绝请求回调 默认为RejectedExecutionHandlers.reject()，EventLoopTaskQueueFactory：queue生成taskQueue的工厂
```

#### EventLoop（调度线程）

EventLoop是与EventLoopGroup种类一一对应的

```java
public interface EventLoop extends OrderedEventExecutor, EventLoopGroup {
    EventLoopGroup parent();  //获取当前线程组
}
```

重点解析NioEventLoop（EventLoop的具体实现都是继承SingleThreadEventLoop，SingleThreadEventLoop继承SingleThreadEventExecutor）

NioEventLoop的构造函数

```java
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler, EventLoopTaskQueueFactory queueFactory) {
        super(parent, executor, false, newTaskQueue(queueFactory), newTaskQueue(queueFactory), rejectedExecutionHandler);
        this.provider = (SelectorProvider)ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
        this.selectStrategy = (SelectStrategy)ObjectUtil.checkNotNull(strategy, "selectStrategy");
        NioEventLoop.SelectorTuple selectorTuple = this.openSelector();
        this.selector = selectorTuple.selector;
        this.unwrappedSelector = selectorTuple.unwrappedSelector;
    }
```

SingleThreadEventLoop的构造函数

```java
 protected static final int DEFAULT_MAX_PENDING_TASKS = Math.max(16, SystemPropertyUtil.getInt("io.netty.eventLoop.maxPendingTasks", 2147483647));
//默认为16
    private final Queue<Runnable> tailTasks;

    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp) {
        this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp) {
        this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, threadFactory, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
        this.tailTasks = this.newTaskQueue(maxPendingTasks);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
        this.tailTasks = this.newTaskQueue(maxPendingTasks);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp, Queue<Runnable> taskQueue, Queue<Runnable> tailTaskQueue, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, taskQueue, rejectedExecutionHandler);
        this.tailTasks = (Queue)ObjectUtil.checkNotNull(tailTaskQueue, "tailTaskQueue");
    }
```

SingleThreadEventExecutor中的构造函数

```java
 protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        super(parent);
        this.threadLock = new CountDownLatch(1);
        this.shutdownHooks = new LinkedHashSet();
        this.state = 1;
        this.terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
        this.addTaskWakesUp = addTaskWakesUp;
        this.maxPendingTasks = Math.max(16, maxPendingTasks);
        this.executor = ThreadExecutorMap.apply(executor, this);
        this.taskQueue = this.newTaskQueue(this.maxPendingTasks);
        this.rejectedExecutionHandler = (RejectedExecutionHandler)ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
    }

    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp, Queue<Runnable> taskQueue, RejectedExecutionHandler rejectedHandler) {
        super(parent);
        this.threadLock = new CountDownLatch(1);
        this.shutdownHooks = new LinkedHashSet();
        this.state = 1;
        this.terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
        this.addTaskWakesUp = addTaskWakesUp;
        this.maxPendingTasks = DEFAULT_MAX_PENDING_EXECUTOR_TASKS;
        this.executor = ThreadExecutorMap.apply(executor, this);
        this.taskQueue = (Queue)ObjectUtil.checkNotNull(taskQueue, "taskQueue");
        this.rejectedExecutionHandler = (RejectedExecutionHandler)ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
    }
```







!1579403939973](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1579403939973.png)

问题：

1.NioEventLoopGroup底下的NioEventLoop是如何生成的

2.NioEventLoop是如何进行循环的

3.NioEventLoop是根据事件进行驱动的

4.除了NioEventLoop，还有其他的EventLoop吗？

5.NioEventLoop包含着什么？

6.EventExecutorGroup