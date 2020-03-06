# Netty-调度模块

## 一、实现类及其依赖的结构图

![1579405553530](H:\学习\后端\java\framker\netty\图片\1579405553530.png)

![1579405587993](H:\学习\后端\java\framker\netty\图片\1579405587993.png)

## 二、EventLoopGroup分析

**自顶向下分析：**

1.Excutor接口提供了线程执行能力

```java
 void execute(Runnable command); //执行一个线程任务
```

2.ExecutorService提供了多个线程任务执行能力，为一个线程执行池

```java
	boolean isShutdown();

    boolean isTerminated();

    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    <T> Future<T> submit(Runnable task, T result); //执行线程后返回执行结果

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException; 
   ......

    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;
 	......
```

3.ScheduledExecutorService为线程池提供了处理定时任务的能力

4.*EventExecutorGroup*为事件执行组，继承了*Iterable*对*EventExecutor*进行迭代处理

```java
	boolean isShuttingDown();	//判断是否关闭

    Future<?> shutdownGracefully();	//关闭

    Future<?> shutdownGracefully(long var1, long var3, TimeUnit var5); //延迟关闭

    Future<?> terminationFuture();

    /** @deprecated */
    @Deprecated
    void shutdown();	

    /** @deprecated */
    @Deprecated
    List<Runnable> shutdownNow();

    EventExecutor next();	//获取下一个EventExecutor

    Iterator<EventExecutor> iterator(); //迭代器
```

5.*AbstractEventExecutorGroup*（实现*EventExecutorGroup*中的基本方法）

```java
this.next().xxx(); //方法实现大多通过next()方法获取EventExecutor后执行
```

6.EventLoopGroup

```java
 	EventLoop next();	

    ChannelFuture register(Channel var1);	//注册通道

    ChannelFuture register(ChannelPromise var1);
```

7.MultithreadEventExecutorGroup

MultithreadEventExecutorGroup的属性

```java
    private final EventExecutor[] children;		//EventExecutorGroup中的子EventExecutor对象
    private final Set<EventExecutor> readonlyChildren; //只读对象
    private final AtomicInteger terminatedChildren;
    private final Promise<?> terminationFuture;
    private final EventExecutorChooser chooser;	//EventExecutor选择器
```

MultithreadEventExecutorGroup的构造器

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
        this.terminatedChildren = new AtomicInteger();  //终止的子线程数量
        this.terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
        if (nThreads <= 0) {   //校验参数并对其初始化
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        } else {
            if (executor == null) {
                executor = new ThreadPerTaskExecutor(this.newDefaultThreadFactory());
            }

            this.children = new EventExecutor[nThreads];//生成对应数量的子线程

            int j;
            for(int i = 0; i < nThreads; ++i) {
                boolean success = false;
                boolean var18 = false;

                try {
                    var18 = true;
                    this.children[i] = this.newChild((Executor)executor, args);//生成子线程
                    success = true;
                    var18 = false;
                } catch (Exception var19) {
                    throw new IllegalStateException("failed to create a child event loop", var19);
                } finally {
                    if (var18) {
                        if (!success) {
                            int j;
                            for(j = 0; j < i; ++j) {
                                this.children[j].shutdownGracefully();////调用全部子线程的关闭方法
                            }

                            for(j = 0; j < i; ++j) {
                                EventExecutor e = this.children[j];//获取全部子线程的关闭方法

                                try {
                                    while(!e.isTerminated()) {//判断是否关闭
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

		//创建EventExecutorChooser选择器，默认为GenericEventExecutorChooser
            this.chooser = chooserFactory.newChooser(this.children); 
            FutureListener<Object> terminationListener = new FutureListener<Object>() {
                public void operationComplete(Future<Object> future) throws Exception {
                    if (MultithreadEventExecutorGroup.this.terminatedChildren.incrementAndGet() == MultithreadEventExecutorGroup.this.children.length) {
                       //判断创建的子线程数量是否为零，为零返回失败 MultithreadEventExecutorGroup.this.terminationFuture.setSuccess((Object)null);
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
            Collections.addAll(childrenSet, this.children);//收集只读子线程
            this.readonlyChildren = Collections.unmodifiableSet(childrenSet);
```

8.MultithreadEventLoopGroup

MultithreadEventLoopGroup的属性

```java
private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
//获取cpu核心数的两倍数量（cpu io密集型）
```

MultithreadEventLoopGroup的构造器

```java
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

9.NioEventLoopGroup

NioEventLoopGroup的构造器

```java
public NioEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectorProvider selectorProvider, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler, EventLoopTaskQueueFactory taskQueueFactory) {
        super(nThreads, executor, chooserFactory, new Object[]{selectorProvider, selectStrategyFactory, rejectedExecutionHandler, taskQueueFactory});
    //rejectedExecutionHandler在nio中默认为RejectedExecutionHandlers.reject()
    //selectorProvider默认为SelectorProvider.provider()
    //selectStrategyFactory默认为DefaultSelectStrategyFactory.INSTANCE
    }
```

生成NioEventLoop的方法

```java
 protected EventLoop newChild(Executor executor, Object... args) throws Exception {
        EventLoopTaskQueueFactory queueFactory = args.length == 4 ? (EventLoopTaskQueueFactory)args[3] : null;
        return new NioEventLoop(this, executor, (SelectorProvider)args[0], ((SelectStrategyFactory)args[1]).newSelectStrategy(), (RejectedExecutionHandler)args[2], queueFactory);
    }
```



1..可以看到EventLoopGroup模型中，需要注意的部分为EventExecutorChooser、SelectStrategyFactory、RejectedExecutionHandler、EventLoopTaskQueueFactory

- EventExecutorChooser （EventLoop选择器，默认实现GenericEventExecutorChooser）

```java
private static final class GenericEventExecutorChooser implements EventExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final EventExecutor[] executors;

        GenericEventExecutorChooser(EventExecutor[] executors) {
            this.executors = executors;
        }

        public EventExecutor next() {
            return this.executors[Math.abs(this.idx.getAndIncrement() % this.executors.length)];
        }
    }
```

- SelectStrategyFactory（选择策略，默认实现为DefaultSelectStrategy）

```java
final class DefaultSelectStrategy implements SelectStrategy {
    static final SelectStrategy INSTANCE = new DefaultSelectStrategy();

    private DefaultSelectStrategy() {
    }

    public int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks) throws Exception {
        return hasTasks ? selectSupplier.get() : -1;
    }
}
```

- RejectedExecutionHandler（拒绝连接处理器，默认实现为RejectedExecutionHandler 中 reject）

```java
 private static final RejectedExecutionHandler REJECT = new RejectedExecutionHandler() {
        public void rejected(Runnable task, SingleThreadEventExecutor executor) {
            throw new RejectedExecutionException();//直接抛出RejectedExecutionException异常
        }
    };

    private RejectedExecutionHandlers() {
    }

    public static RejectedExecutionHandler reject() {
        return REJECT;
    }

    public static RejectedExecutionHandler backoff(final int retries, long backoffAmount, TimeUnit unit) {
        ObjectUtil.checkPositive(retries, "retries");
        final long backOffNanos = unit.toNanos(backoffAmount);
        return new RejectedExecutionHandler() {
            public void rejected(Runnable task, SingleThreadEventExecutor executor) {
                if (!executor.inEventLoop()) {
                    for(int i = 0; i < retries; ++i) {
                        executor.wakeup(false);
                        LockSupport.parkNanos(backOffNanos);
                        if (executor.offerTask(task)) {
                            return;
                        }
                    }
                }

                throw new RejectedExecutionException();
            }
        };
    }
```

- EventLoopTaskQueueFactory

暂定

1.可以看到EventLoopGroup模型中占据绝大多数的为*EventLoopGroup*以及*EventLoop*，这两者的不同在于*EventLoopGroup*为多个*Executor*组成，而*EventLoop*为单个*Executor*

2.无论是EventLoopGroup还是EventLoop，本质上都为Executor，但是EventLoopGroup中包含多个Executor，所以加入了Iterable接口进行迭代循环



## 三、EventLoop分析

1.AbstractExecutorService

实现了ExecutorService中的功能

2.EventExecutor

```java
	EventExecutor next();		//在EventLoop中会返回本身

    EventExecutorGroup parent();	//返回EventLoop所属的EventExecutorGroup

    boolean inEventLoop(); //对于当前执行的Thread的身份的确定，因为可以获取其他EventLoop的执行线程推送任务

    boolean inEventLoop(Thread var1);

    <V> Promise<V> newPromise();

    <V> ProgressivePromise<V> newProgressivePromise();

    <V> Future<V> newSucceededFuture(V var1);

    <V> Future<V> newFailedFuture(Throwable var1);
```



3.EventLoop为单线程运行