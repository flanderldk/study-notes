java io 模型

io模型一共有5种：阻塞IO模型、非阻塞IO模型、多路复用IO模型、信号驱动IO模型、异步IO模型

说明：

- IO有内存IO、网络IO和磁盘IO三种，通常我们说的IO指的是后两者。

- 阻塞和非阻塞，是函数/方法的实现方式，即在数据就绪之前是立刻返回还是等待，即发起IO请求是否会被阻塞。

- 以文件IO为例,一个IO读过程是文件数据从磁盘→内核缓冲区→用户内存的过程。同步与异步的区别主要在于数据从内核缓冲区→用户内存这个过程需不需要用户进程等待，即实际的IO读写是否阻塞请求进程。(网络IO把磁盘换做网卡即可)

- 用户空间&内核空间

  现在操作系统都是采用虚拟存储器，操作系统将虚拟空间划分为两部分，一部分为内核空间，一部分为用户空间。

  ```text
  1.1. 内核空间：受保护的内存空间，只有操作系统可以访问；
  1.2. 用户空间：供应用程序使用
  ```

## 1.阻塞IO模型(BIO)

最传统的一种IO模型，即在读写数据过程中会发生阻塞现象。

　　当用户线程发出IO请求之后，内核会去查看数据是否就绪，如果没有就绪就会等待数据就绪，而用户线程就会处于阻塞状态，用户线程交出CPU。当数据就绪之后，内核会将数据拷贝到用户线程，并返回结果给用户线程，用户线程才解除block状态。

网络编程中，读取客户端的数据需要调用recvfrom。在默认情况下，这个调用会一直阻塞直到数据接收完毕，就是一个同步阻塞的IO方式。这也是最简单的IO模型，在通常fd较少、就绪很快的情况下使用是没有问题的。

即在数据传输完成之前，该用户线程会一直处于block状态，无法使用，直到传输完成，才会解除block状态；

![img](H:\学习\后端\java\基本概念\io模型\图片\ABUIABAEGAAg0MKrwAUosJqimgYwgAU4xQI.png)

## 2.非阻塞IO模型

当用户线程发起一个read操作后，并不需要等待，而是马上就得到了一个结果。如果结果是一个error时，它就知道数据还没有准备好，于是它可以再次发送read操作。一旦内核中的数据准备好了，并且又再次收到了用户线程的请求，那么它马上就将数据拷贝到了用户线程，然后返回。

　　所以事实上，在非阻塞IO模型中，用户线程需要不断地询问内核数据是否就绪，也就说非阻塞IO不会交出CPU，而会一直占用CPU。

即在线程开始数据传输时，会立刻得到一个结果，这个结果说明数据是否准备完毕，此后该用户线程会不断询问cpu的数据准备结果，从而导致cpu的占有率提高，一旦数据准备结束，将会从内核进行数据传输，所以非阻塞IO模型不会释放cpu，会一直占用着cpu。

这种方式在编程中对socket设置O_NONBLOCK即可。但此方式仅仅针对网络IO有效，对磁盘IO并没有作用。因为本地文件IO就没有被认为是阻塞，我们所说的网络IO的阻塞是因为网路IO有无限阻塞的可能，而本地文件除非是被锁住，否则是不可能无限阻塞的，因此只有锁这种情况下，O_NONBLOCK才会有作用。而且，磁盘IO时要么数据在内核缓冲区中直接可以返回，要么需要调用物理设备去读取，这时候进程的其他工作都需要等待。因此，后续的IO复用和信号驱动IO对文件IO也是没有意义的。

![img](H:\学习\后端\java\基本概念\io模型\图片\ABUIABAEGAAg7MKrwAUogfiO-QUwgAU48gI.png)

## 3.多路复用IO模型(NIO)

多路复用IO模型是目前使用得比较多的模型。Java NIO实际上就是多路复用IO。

多路：多个网络连接

复用：线程复用

　　在多路复用IO模型中，会有一个线程不断去轮询多个socket的状态，只有当socket真正有读写事件时，才真正调用实际的IO读写操作。因为在多路复用IO模型中，只需要使用一个线程就可以管理多个socket，系统不需要建立新的进程或者线程，也不必维护这些线程和进程，并且只有在真正有socket读写事件进行时，才会使用IO资源，所以它大大减少了资源占用。

　　在Java NIO中，是通过selector.select()去查询每个通道是否有到达事件，如果没有事件，则一直阻塞在那里，因此这种方式会导致用户线程的阻塞。

　　也许有朋友会说，我可以采用多线程+ 阻塞IO 达到类似的效果，但是由于在多线程 + 阻塞IO 中，每个socket对应一个线程，这样会造成很大的资源占用，并且尤其是对于长连接来说，线程的资源一直不会释放，如果后面陆续有很多连接的话，就会造成性能上的瓶颈。

　　而多路复用IO模式，通过一个线程就可以管理多个socket，只有当socket真正有读写事件发生才会占用资源来进行实际的读写操作。因此，多路复用IO比较适合连接数比较多的情况。

　　另外多路复用IO为何比非阻塞IO模型的效率高是因为在非阻塞IO中，不断地询问socket状态时通过用户线程去进行的，而在多路复用IO中，轮询每个socket状态是内核在进行的，这个效率要比用户线程要高的多。

　　不过要注意的是，多路复用IO模型是通过轮询的方式来检测是否有事件到达，并且对到达的事件逐一进行响应。因此对于多路复用IO模型来说，一旦事件响应体很大，那么就会导致后续的事件迟迟得不到处理，并且会影响新的事件轮询

![img](H:\学习\后端\java\基本概念\io模型\图片\ABUIABAEGAAg_sKrwAUojMPt1gIwgAU4mgM.png)

详细解析：

**一、NIO的概念**

![1578970741274](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1578970741274.png)

1. Java NIO全称 java non-blocking IO，是指JDK提供的新API，从java 1.4开始提供的一系列改进输入输出的新特性，被统一称为NIO（New IO）,是同步非阻塞的。
2. NIO相关类都被放在java.nio包及其子包下，并且对原java.io包中很多类进行改写。
3. NIO三大核心部分：Channel（通道），Buffer（缓冲），Selector（选择器）；
4. NIO是**面向缓冲区，或者面向块编程**的。数据读取到一个稍后处理的缓冲区，需要时可在缓冲区前后移动，这就是增加了处理过程中的灵活性，使用它可以提供**非阻塞式的高伸缩性网络**。
5. Java NIO非阻塞式模式，使一个线程从某通道发送请求或者读取数据，但是它仅仅能得到目前可用的数据，如何没有数据可用时，就就什么都不会获取，而不是保持线程阻塞，所以直到数据变得可以读取之前，该线程可以继续做其他事。非阻塞写也是如此，一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情。
6. 通俗一点，Java NIO可以做到用一个线程来处理多个操作。假设有10000个请求过来，根据实际情况，可以分配50或者100个线程来处理。不想之前的阻塞IO那样，非得10000个线程处理。
7. HTTP2.0使用了多路复用的技术，做到了同一连接并发处理多个请求，而且并发请求的数量比HTTP1.1大了好几个数量级。

**二、缓冲区（Buffer）**

Buffer定义了所有缓冲区都具有四个属性来提供关于其所包含的数据元素的信息

| 属性     | 描述                                                         |
| :------- | :----------------------------------------------------------- |
| Capacity | 容器，即可以容纳的最大数量，在缓冲区创建时设定并且不能修改。 |
| Limit    | 表示缓冲区的当前终点，不能对缓冲区超过极线的位置进行读写操作。且极限时可以修改的。 |
| Position | 位置，下一个要读或者写的元素的索引，每次读写缓冲区数据时，都会改变Position值，为下次读写做准备。 |
| Mark     | 标记，调用mark()来设置mark=position，再调用reset()可以让position恢复标记位置。 |

**三、NIO和BIO的比较**

1. BIO以流的方式处理数据，而NIO已块的方式处理数据，块I/O的效率比流I/O的效率高很多。
2. BIO是阻塞的，NIO是非阻塞的。
3. BIO基于字节流和字符流进行操作，而NIO基于Channel（通道）和Buffer（缓冲区）进行操作，数据总是从通道读入缓冲区中，或者从缓冲区写入通道中，Selector（选择器）用于监听多个通道的事件（连接请求、数据到达等），也因此单个线程可以监听多个客户端通道。

**四、NIO三大核心组件示意图**

![img](https://mmbiz.qpic.cn/mmbiz_png/wbiax4xEAl5xxYbFZU3NGciciaHZyOZa93FtEPd34RlwWs3SgufYTccAAVrjcTnNUukibYuKKkNqfUsJLH0FWx5TDQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 每个channel都对应一个Buffer;
- 一个selector对应一个线程,- 一个线程对应多个channel（连接）;
- 该图反应了有三个channel注册到该selector；
- 程序切换到哪一个channel是由事件决定的，Event就是一个重要的概念；
- selector会根据不同的事件会在不同的channel上进行切换；
- Buffer是一个内存块，底层是一个数组；
- 数据的读取写入是通过buffer的，这个和BIO有本质的不同，BIO要么是输入流，要么是输出流，不能双向，但是NIO的buffer可以读也可以写（需要filp切换）；
- channel是双向的，可以返回底层操作系统的情况。

**五、NIO的实现方式**

**select、poll 和 epoll 底层数据各不相同。select 使用数组；poll 采用链表，解决了 fd 数量的限制；epoll 底层使用的是红黑树，能够有效的提升效率。**

select 最大的缺陷就是单个进程所打开的 FD 是有一定限制的，它由 FD_SETSIZE 设置，默认值是 1024。对于那些需要支持上万个 TCP 连接的大型服务器来说显然太少了。可以选择修改这个宏然后重新编译内核，不过这会带来网络效率的下降。我们也可以通过选择多进程的方案(传统的 Apache 方案)解决这个问题，不过虽然在 Linux 上创建进程的代价比较小，但仍旧是不可忽视的。另外，进程间的数据交换非常麻烦，对于 Java 来说，由于没有共享内存，需要通过 Socket 通信或者其他方式进行数据同步，这带来了额外的性能损耗，増加了程序复杂度，所以也不是一种完美的解决方案。值得庆幸的是， epoll 并没有这个限制，它所支持的 FD 上限是操作系统的最大文件句柄数，这个数字远远大于 1024。例如，在 1GB 内存的机器上大约是 10 万个句柄左右，具体的值可以通过 cat proc/sys/fs/file-max 查看，通常情况下这个值跟系统的内存关系比较大。

```
# (所有进程)当前计算机所能打开的最大文件个数。受硬件影响，这个值可以改（通过limits.conf）
cat /proc/sys/fs/file-max

# (单个进程)查看一个进程可以打开的socket描述符上限。缺省为1024
ulimit -a 
# 修改为默认的最大文件个数。【注销用户，使其生效】
ulimit -n 2000

# soft软限制 hard硬限制。所谓软限制是可以用命令的方式修改该上限值，但不能大于硬限制
vi /etc/security/limits.conf
* soft nofile 3000      # 设置默认值。可直接使用命令修改
* hard nofile 20000     # 最大上限值
```

**I/O 效率不会随着 FD 数目的増加而线性下降**

传统 select/poll 的另一个致命弱点，就是当你拥有一个很大的 socket 集合时，由于网络延时或者链路空闲，任一时刻只有少部分的 socket 是“活跃”的，但是 select/poll 每次调用都会线性扫描全部的集合，导致效率呈现线性下降。 epoll 不存在这个问题，它只会对“活跃”的 socket 进行操作一一这是因为在内核实现中， epoll 是根据每个 fd 上面的 callback 函数实现的。那么，只有“活跃”的 socket オ会去主动调用 callback 函数，其他 idle 状态的 socket 则不会。在这点上， epoll 实现了一个伪 AIO。针对 epoll 和 select 性能对比的 benchmark 测试表明：如果所有的 socket 都处于活跃态 - 例如一个高速 LAN 环境， epoll 并不比 select/poll 效率高太多；相反，如果过多使用 epoll_ctl，效率相比还有稍微地降低但是一旦使用 idle connections 模拟 WAN 环境， epoll 的效率就远在 select/poll 之上了。

**使用 mmap 加速内核与用户空间的消息传递**

无论是 select、poll 还是 epoll 都需要内核把 FD 消息通知给用户空间，如何避免不必要的内存复制就显得非常重要，epoll 是通过内核和用户空间 mmap 同一块内存来实现的

**Reactor线程模型**

- **Reactor**: 负责响应事件，将事件分发绑定了该事件的Handler处理
- **Handler**: 事件处理器，绑定了某类事件，负责执行对应事件的任务对事件进行处理
- **Acceptor**：Handler的一种，绑定了 connect 事件，当客户端发起connect请求时，Reactor会将accept事件分发给Acceptor处理

1.单线程Reactor

- Reactor对象通过select监控连接事件，收到事件后通过dispatch进行分发

- 如果是连接建立的事件，则交由 Acceptor 通过accept 处理连接请求，然后创建一个 Handler 对象处理连接完成后的后续业务处理

- 如果不是建立连接事件，则 Reactor 会分发调用连接对应的 Handler来响应

- Handler 会完成 read -> 业务处理 -> send 的完整业务流程

  ![1578984267001](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1578984267001.png)

2.多线程Reactor

![1578984294822](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1578984294822.png)

3.主从Reactor

![1578984352702](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1578984352702.png)

![1578984318461](C:\Users\flanderldk\AppData\Roaming\Typora\typora-user-images\1578984318461.png)



## 4.信号驱动IO模型

　　在信号驱动IO模型中，当用户线程发起一个IO请求操作，会给对应的socket注册一个信号函数，然后用户线程会继续执行，当内核数据就绪时会发送一个信号给用户线程，用户线程接收到信号之后，便在信号函数中调用IO读写操作来进行实际的IO请求操作。这个一般用于UDP中，对TCP套接口几乎是没用的，原因是该信号产生得过于频繁，并且该信号的出现并没有告诉我们发生了什么事情

![img](H:\学习\后端\java\基本概念\io模型\图片\ABUIABAEGAAglcOrwAUoqPiNiQYwgAU4lgM.png)

## 5.异步IO模型（AIO）

　　异步IO模型才是最理想的IO模型，在异步IO模型中，当用户线程发起read操作之后，立刻就可以开始去做其它的事。而另一方面，从内核的角度，当它受到一个asynchronous read之后，它会立刻返回，说明read请求已经成功发起了，因此不会对用户线程产生任何block。然后，内核会等待数据准备完成，然后将数据拷贝到用户线程，当这一切都完成之后，内核会给用户线程发送一个信号，告诉它read操作完成了。也就说用户线程完全不需要关心实际的整个IO操作是如何进行的，只需要先发起一个请求，当接收内核返回的成功信号时表示IO操作已经完成，可以直接去使用数据了。

　　也就说在异步IO模型中，IO操作的两个阶段都不会阻塞用户线程，这两个阶段都是由内核自动完成，然后发送一个信号告知用户线程操作已完成。用户线程中不需要再次调用IO函数进行具体的读写。这点是和信号驱动模型有所不同的，在信号驱动模型中，当用户线程接收到信号表示数据已经就绪，然后需要用户线程调用IO函数进行实际的读写操作；而在异步IO模型中，收到信号表示IO操作已经完成，不需要再在用户线程中调用iO函数进行实际的读写操作。

　　注意，异步IO是需要操作系统的底层支持，在Java 7中，提供了Asynchronous IO。简称AIO

前面四种IO模型实际上都属于同步IO，只有最后一种是真正的异步IO，因为无论是多路复用IO还是信号驱动模型，IO操作的第2个阶段都会引起用户线程阻塞，也就是内核进行数据拷贝的过程都会让用户线程阻塞。

![img](H:\学习\后端\java\基本概念\io模型\图片\ABUIABAEGAAgvMOrwAUoypzSBzCABTjtAg.png)

## 问题总结：

### 1.NIO中的selector的角色

答：selector负责连接通道（acceptor），注册通道，轮询事件状态，以及根据状态进行对应的处理（handler）

### 2.websocket与http的差别

答：最主要的差别是一个是全双工一个是半双工

### 3.seletor是基于事件驱动的，事件驱动是什么



### 4.线程的概念和连接的概念 

答：可以通过tomcat的两个属性分析可得

> ## maxConnections
>
> 　　Tomcat在任意时刻接收和处理的最大连接数。当Tomcat接收的连接数达到maxConnections时，Acceptor线程不会读取accept队列中的连接；这时accept队列中的线程会一直阻塞着，直到Tomcat接收的连接数小于maxConnections。如果设置为-1，则连接数不受限制。
>
> 　　默认值与连接器使用的协议有关：NIO的默认值是10000，APR/native的默认值是8192，而BIO的默认值为maxThreads（如果配置了Executor，则默认值是Executor的maxThreads）。
>
> ## maxThreads
>
> 　　请求处理线程的最大数量。默认值是200（Tomcat7和8都是的）。如果该Connector绑定了Executor，这个值会被忽略，因为该Connector将使用绑定的Executor，而不是内置的线程池来执行任务。
>
> 　　maxThreads规定的是最大的线程数目，并不是实际running的CPU数量；实际上，maxThreads的大小比CPU核心数量要大得多。这是因为，处理请求的线程真正用于计算的时间可能很少，大多数时间可能在阻塞，如等待数据库返回数据、等待硬盘读写数据等。因此，在某一时刻，只有少数的线程真正的在使用物理CPU，大多数线程都在等待；因此线程数远大于物理核心数才是合理的。
>
> 　　换句话说，Tomcat通过使用比CPU核心数量多得多的线程数，可以使CPU忙碌起来，大大提高CPU的利用率。

### 5.socket与线程

答：一个socket只会有一个线程跟其对接

### 6.NIO的阻塞是在哪？

答：主要在于放出可读可写信号的时候，selector需要对其进行遍历处理，是在这里堵塞的

### 7.socket的对应

答：多个socket对应一个serverSocket