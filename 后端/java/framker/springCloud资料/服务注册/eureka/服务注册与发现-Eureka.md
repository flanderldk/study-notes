# [服务注册与发现-Eureka](https://github.com/Netflix/eureka/wiki/Eureka-at-a-glance)



​		在微服务架构中，每个微服务通常有多个实例，每个实例具有不同的位置，而且实例会动态变化，比如在负载发生变化时服务会进行扩容或缩容，失败或者更新，服务实力的配置变化等，都会导致服务实例地址的变化。因此使用微服务架构开发的应用，必须通过服务注册和发现技术解决此问题。


​	Eureka是一项基于REST（代表性状态转移）的服务，主要在AWS云中用于定位服务，以实现负载均衡和中间层服务器的故障转移。我们称此服务为**Eureka Server**。Eureka还带有一个基于Java的客户端组件**Eureka Client**，它使与服务的交互更加容易。客户端还具有一个内置的负载均衡器，可以执行基本的循环负载均衡。在Netflix，更复杂的负载均衡器将Eureka包装起来，以基于流量，资源使用，错误条件等多种因素提供加权负载均衡，以提供出色的弹性。



## 1.Eureka服务注册中心

### 1.1Eureka服务注册中心的特点

### 1.2Eureka在SpringCloud中如何启动

1. 开启eureka服务，添加注解@EnableEurekaServer

2. 设置配置

   ```yml
   spring:
     application:
       name: EurekaServer
   server:
     port: 8080
   eureka:
     instance:
       hostname: localhost #配置域名或者相关ip
     client:
       fetch-registry: false   #服务器无需拉取配置
       service-url:
         defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka1 #服务注册地址
       register-with-eureka: false #eureka服务器无需注册自己
   
   ```

   

### 1.3.可通过http://l{hostname}:{port}/ 查看当前eureka服务器的注册情况

![1583301050038](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\eureka查看服务注册情况.png)



## 2.Eureka服务发现

### 2.1开启eureka服务，添加注解@EnableDiscoveryClient

### 2.2设置配置

```yml
spring:
  application:
    name: EurakaProvider1
server:
  port: 1080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8080/eureka  #配置服务注册中心注册地址
  instance:
    instance-id: EurakaProvider1  #配置服务的实例名称

```

### 1.3.查看当前eureka服务注册中心的注册情况

![1583303771123](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\eureka服务注册情况.png)



## 3.Eureka的一些概念

### 1.Euraka实例

Euraka实例是指初始化成功，并且要向服务注册中心进行注册的实例，一旦实例初始化成功，则在springCloud中会初始化EurekaInstanceConfigBean这个bean，这个bean记载着这个实例的所有[instanceinfo](https://github.com/Netflix/eureka/blob/master/eureka-client/src/main/java/com/netflix/appinfo/InstanceInfo.java)信息。

```java
    private static final String UNKNOWN = "unknown";
    private HostInfo hostInfo;			//记录该实例的地址信息
    private InetUtils inetUtils;
    private String actuatorPrefix = "/actuator";	//actuator的路径
    private String appname = "unknown";
    private String appGroupName;
    private boolean instanceEnabledOnit;
    private int nonSecurePort = 80;		//非加密连接端口
    private int securePort = 443;		//加密端口
    private boolean nonSecurePortEnabled = true;	//非加密连接，默认是true
    private boolean securePortEnabled;
    private int leaseRenewalIntervalInSeconds = 30;
    private int leaseExpirationDurationInSeconds = 90;
    private String virtualHostName = "unknown";		//虚拟域名
    private String instanceId;					//实例id
    private String secureVirtualHostName = "unknown";
    private String aSGName;
    private Map<String, String> metadataMap = new HashMap();
    private DataCenterInfo dataCenterInfo;
    private String ipAddress;  //ip地址
    private String statusPageUrlPath;
    private String statusPageUrl;
    private String homePageUrlPath;
    private String homePageUrl;
    private String healthCheckUrlPath;
    private String healthCheckUrl;	//健康检查路径
    private String secureHealthCheckUrl;
    private String namespace;	//命名空间，  是否可以将服务分割开？
    private String hostname;
    private boolean preferIpAddress;
    private InstanceStatus initialStatus;
    private String[] defaultAddressResolutionOrder;
    private Environment environment;	//环境，可以从中获取配置信息
```

### 2.Eureka客户端

EurekaClientConfigBean的配置

```java
    public static final String PREFIX = "eureka.client";
    public static final String DEFAULT_URL = "http://localhost:8761/eureka/"; //eureka的默认路径
    public static final String DEFAULT_ZONE = "defaultZone";
    private static final int MINUTES = 60;
    @Autowired(
        required = false
    )
    PropertyResolver propertyResolver;
    private boolean enabled = true;
    @NestedConfigurationProperty
    private EurekaTransportConfig transport = new CloudEurekaTransportConfig();
    private int registryFetchIntervalSeconds = 30;
    private int instanceInfoReplicationIntervalSeconds = 30;
    private int initialInstanceInfoReplicationIntervalSeconds = 40;
    private int eurekaServiceUrlPollIntervalSeconds = 300;
    private String proxyPort;
    private String proxyHost;
    private String proxyUserName;
    private String proxyPassword;
    private int eurekaServerReadTimeoutSeconds = 8;
    private int eurekaServerConnectTimeoutSeconds = 5;
    private String backupRegistryImpl;
    private int eurekaServerTotalConnections = 200;
    private int eurekaServerTotalConnectionsPerHost = 50;
    private String eurekaServerURLContext;
    private String eurekaServerPort;
    private String eurekaServerDNSName;
    private String region = "us-east-1";
    private int eurekaConnectionIdleTimeoutSeconds = 30;
    private String registryRefreshSingleVipAddress;
    private int heartbeatExecutorThreadPoolSize = 2;
    private int heartbeatExecutorExponentialBackOffBound = 10;
    private int cacheRefreshExecutorThreadPoolSize = 2;
    private int cacheRefreshExecutorExponentialBackOffBound = 10;
    private Map<String, String> serviceUrl = new HashMap();
    private boolean gZipContent;
    private boolean useDnsForFetchingServiceUrls;
    private boolean registerWithEureka;
    private boolean preferSameZoneEureka;
    private boolean logDeltaDiff;
    private boolean disableDelta;
    private String fetchRemoteRegionsRegistry;
    private Map<String, String> availabilityZones;
    private boolean filterOnlyUpInstances;
    private boolean fetchRegistry;
    private String dollarReplacement;
    private String escapeCharReplacement;
    private boolean allowRedirects;
    private boolean onDemandUpdateStatusChange;
    private String encoderName;
    private String decoderName;
    private String clientDataAccept;
    private boolean shouldUnregisterOnShutdown;
    private boolean shouldEnforceRegistrationAtInit;
    private int order;
```

### 3.Eureka服务端

EurekaServerConfigBean

```java
public static final String PREFIX = "eureka.server";
    private static final int MINUTES = 60000;
    @Autowired(
        required = false
    )
    PropertyResolver propertyResolver;
    private String aWSAccessId;
    private String aWSSecretKey;
    private int eIPBindRebindRetries = 3;
    private int eIPBindingRetryIntervalMs = 300000;
    private int eIPBindingRetryIntervalMsWhenUnbound = 60000;
    private boolean enableSelfPreservation = true;
    private double renewalPercentThreshold = 0.85D;
    private int renewalThresholdUpdateIntervalMs = 900000;
    private int peerEurekaNodesUpdateIntervalMs = 600000;
    private int numberOfReplicationRetries = 5;
    private int peerEurekaStatusRefreshTimeIntervalMs = 30000;
    private int waitTimeInMsWhenSyncEmpty = 300000;
    private int peerNodeConnectTimeoutMs = 200;
    private int peerNodeReadTimeoutMs = 200;
    private int peerNodeTotalConnections = 1000;
    private int peerNodeTotalConnectionsPerHost = 500;
    private int peerNodeConnectionIdleTimeoutSeconds = 30;
    private long retentionTimeInMSInDeltaQueue = 180000L;
    private long deltaRetentionTimerIntervalInMs = 30000L;
    private long evictionIntervalTimerInMs = 60000L;
    private int aSGQueryTimeoutMs = 300;
    private long aSGUpdateIntervalMs = 300000L;
    private long aSGCacheExpiryTimeoutMs = 600000L;
    private long responseCacheAutoExpirationInSeconds = 180L;
    private long responseCacheUpdateIntervalMs = 30000L;
    private boolean useReadOnlyResponseCache = true;
    private boolean disableDelta;
    private long maxIdleThreadInMinutesAgeForStatusReplication = 10L;
    private int minThreadsForStatusReplication = 1;
    private int maxThreadsForStatusReplication = 1;
    private int maxElementsInStatusReplicationPool = 10000;
    private boolean syncWhenTimestampDiffers = true;
    private int registrySyncRetries = 0;
    private long registrySyncRetryWaitMs = 30000L;
    private int maxElementsInPeerReplicationPool = 10000;
    private long maxIdleThreadAgeInMinutesForPeerReplication = 15L;
    private int minThreadsForPeerReplication = 5;
    private int maxThreadsForPeerReplication = 20;
    private int maxTimeForReplication = 30000;
    private boolean primeAwsReplicaConnections = true;
    private boolean disableDeltaForRemoteRegions;
    private int remoteRegionConnectTimeoutMs = 1000;
    private int remoteRegionReadTimeoutMs = 1000;
    private int remoteRegionTotalConnections = 1000;
    private int remoteRegionTotalConnectionsPerHost = 500;
    private int remoteRegionConnectionIdleTimeoutSeconds = 30;
    private boolean gZipContentFromRemoteRegion = true;
    private Map<String, String> remoteRegionUrlsWithName = new HashMap();
    private String[] remoteRegionUrls;
    private Map<String, Set<String>> remoteRegionAppWhitelist = new HashMap();
    private int remoteRegionRegistryFetchInterval = 30;
    private int remoteRegionFetchThreadPoolSize = 20;
    private String remoteRegionTrustStore = "";
    private String remoteRegionTrustStorePassword = "changeit";
    private boolean disableTransparentFallbackToOtherRegion;
    private boolean batchReplication;
    private boolean rateLimiterEnabled = false;
    private boolean rateLimiterThrottleStandardClients = false;
    private Set<String> rateLimiterPrivilegedClients = Collections.emptySet();
    private int rateLimiterBurstSize = 10;
    private int rateLimiterRegistryFetchAverageRate = 500;
    private int rateLimiterFullFetchAverageRate = 100;
    private boolean logIdentityHeaders = true;
    private String listAutoScalingGroupsRoleName = "ListAutoScalingGroups";
    private boolean enableReplicatedRequestCompression = false;
    private String jsonCodecName;
    private String xmlCodecName;
    private int route53BindRebindRetries = 3;
    private int route53BindingRetryIntervalMs = 300000;
    private long route53DomainTTL = 30L;
    private AwsBindingStrategy bindingStrategy;
    private int minAvailableInstancesForPeerReplication;
    private int initialCapacityOfResponseCache;
    private int expectedClientRenewalIntervalSeconds;
    private boolean useAwsAsgApi;
    private String myUrl;
```

各个配置的详情可以通过https://www.cnblogs.com/chry/p/7992885.html进行了解

## 4.Eurekad的自我保护模式

默认情况下，如果Eureka Server在一定时间内没有接收到某个微服务实例的心跳，Eureka Server将会注销该实例（默认90秒）。但是当网络分区故障发生时，微服务与Eureka Server之间无法正常通信，这就可能变得非常危险了----因为微服务本身是健康的，此时本不应该注销这个微服务。

 Eureka Server通过“自我保护模式”来解决这个问题----当Eureka Server节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。一旦进入该模式，Eureka Server就会保护服务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何微服务）。当网络故障恢复后，该Eureka Server节点会自动退出自我保护模式。

自我保护模式是一种对网络异常的安全保护措施。使用自我保护模式，而已让Eureka集群更加的健壮、稳定。

在Spring Cloud中，可以使用eureka.server.enable-self-preservation=false来禁用自我保护模式

![1583309443887](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\自我保护模式.png)

那么丢失过多客户端的阈值在哪呢？RenewalPercentThreshold(*)变量，即为上图中的Renews threshold/Renews ，如果大于0.85（阈值默认），则出发自动保护

由于Eureka Server与Eureka Client之间使用心跳机制来确定Eureka Client的状态，默认情况下，服务器端与客户端的心跳保持正常，应用程序就会始终保持“UP”状态，所以微服务的UP并不能完全反应应用程序的状态。

Spring Boot Actuator提供了/health端点，该端点可展示应用程序的健康信息，只有将该端点中的健康状态传播到Eureka Server就可以了，实现这点很简单，只需为微服务配置如下内容：

```yml
#开启健康检查（需要spring-boot-starter-actuator依赖）
eureka.client.healthcheck.enabled = true //默认启动
```


  如果需要更细粒度健康检查，可实现HealthCheckHandler接口 

```java
public class CustomerHealthCheckHandler implements HealthCheckHandler {

    private EurekaInstanceConfigBean eurekaInstanceConfigBean;

    public CustomerHealthCheckHandler(EurekaInstanceConfigBean eurekaInstanceConfigBean){
        this.eurekaInstanceConfigBean = eurekaInstanceConfigBean;
    }

    @Override
    public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus instanceStatus) {
        System.out.println(String.valueOf(new Date().getDate()) + ":" +eurekaInstanceConfigBean.getHostname() + "is" +instanceStatus);
        return instanceStatus;
    }
}
```

```java
@Configuration
public class EurekaConfiguration {

    @Bean
    public HealthCheckHandler returnHealthCheckHandler(EurekaInstanceConfigBean eurekaInstanceConfigBean){
        return new CustomerHealthCheckHandler(eurekaInstanceConfigBean);
    }
}
```

## 5.配置Eureka的高可用集群

Eureka的高可用配置不复杂，只需要将服务注册者的defaultZone写上多个eureka集群的url，服务注册中心写上相对应集群的url

![img](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\client访问集群.png)

![img](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\server集群.png)

坑：需要在本地host配置对应的域名映射，直接使用localhost会出现异常

Eureka集群的特点：

![img](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\集群特点.png)

## 6.Eureka具体功能

![img](H:\学习\后端\java\framker\springCloud资料\服务注册\eureka\图片\总体架构图.png)

### 6.1Eureka注册服务过程

​	在SpringCloud环境中服务注册会执行InstanceRegistry类中的register方法，这个方法会发布一个EurekaInstanceRegisteredEvent  事件，并且调用AbstractInstanceRegistry的register方法

​	Eureka的服务注册过程主要是执行AbstractInstanceRegistry的register方法

AbstractInstanceRegistry类的一些重要属性

```java
private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry = new ConcurrentHashMap();		//实例注册信息
protected volatile ResponseCache responseCache;  //注册信息缓存
private final AbstractInstanceRegistry.CircularQueue<Pair<Long, String>> recentRegisteredQueue;//最近注册实例队列
private ConcurrentLinkedQueue<AbstractInstanceRegistry.RecentlyChangedItem> recentlyChangedQueue;//最近改变实例队列
```

register方法

```java
      this.read.lock();  //写锁锁住，为了防止注册过程中线程不安全问题
            Map<String, Lease<InstanceInfo>> gMap = (Map)this.registry.get(registrant.getAppName());  //获取对应服务的注册信息
            EurekaMonitors.REGISTER.increment(isReplication); 	//注册总量加1
            if (gMap == null) {	
                ConcurrentHashMap<String, Lease<InstanceInfo>> gNewMap = new ConcurrentHashMap(); 
                gMap = (Map)this.registry.putIfAbsent(registrant.getAppName(), gNewMap);  //可能出现获取不到的情况，并对其进行初始化
                if (gMap == null) {		
                    gMap = gNewMap;
                }
            }

            Lease<InstanceInfo> existingLease = (Lease)((Map)gMap).get(registrant.getId()); //初始化对应实例的租期信息（包含示例）
            if (existingLease != null && existingLease.getHolder() != null) {	
                Long existingLastDirtyTimestamp = ((InstanceInfo)existingLease.getHolder()).getLastDirtyTimestamp(); //获取现有的租期到期时间
                Long registrationLastDirtyTimestamp = registrant.getLastDirtyTimestamp();	//获取注册的租期到期时间
                logger.debug("Existing lease found (existing={}, provided={}", existingLastDirtyTimestamp, registrationLastDirtyTimestamp);
                if (existingLastDirtyTimestamp > registrationLastDirtyTimestamp) {
                    logger.warn("There is an existing lease and the existing lease's dirty timestamp {} is greater than the one that is being registered {}", existingLastDirtyTimestamp, registrationLastDirtyTimestamp);
                    logger.warn("Using the existing instanceInfo instead of the new instanceInfo as the registrant");
                    registrant = (InstanceInfo)existingLease.getHolder();  //如果现有的租期到期时间大于注册的租期到期时间，则使用现有的实例信息
                }
            } else {
                synchronized(this.lock) {
                    if (this.expectedNumberOfClientsSendingRenews > 0) {
                        ++this.expectedNumberOfClientsSendingRenews;  //预计发送更新的客户数量加1
                        this.updateRenewsPerMinThreshold();  //计算每分钟最小阈值更新的次数
                    }
                }

                logger.debug("No previous lease information found; it is new registration");
            }

            Lease<InstanceInfo> lease = new Lease(registrant, leaseDuration);  //初始化实例租期， leaseDuration租赁期限
            if (existingLease != null) {
                lease.setServiceUpTimestamp(existingLease.getServiceUpTimestamp());  //设置实例的服务启动时间
            }

            ((Map)gMap).put(registrant.getId(), lease);
            synchronized(this.recentRegisteredQueue) {  //对最近注册队列进行加锁
                this.recentRegisteredQueue.add(new Pair(System.currentTimeMillis(), registrant.getAppName() + "(" + registrant.getId() + ")")); //记录当前注册的信息
            }

            if (!InstanceStatus.UNKNOWN.equals(registrant.getOverriddenStatus())) {  //查看实例的覆盖状态
                logger.debug("Found overridden status {} for instance {}. Checking to see if needs to be add to the overrides", registrant.getOverriddenStatus(), registrant.getId());
                if (!this.overriddenInstanceStatusMap.containsKey(registrant.getId())) {
                    logger.info("Not found overridden id {} and hence adding it", registrant.getId());
                    this.overriddenInstanceStatusMap.put(registrant.getId(), registrant.getOverriddenStatus()); //覆盖map里的实例状态
                }
            }

            InstanceStatus overriddenStatusFromMap = (InstanceStatus)this.overriddenInstanceStatusMap.get(registrant.getId());
            if (overriddenStatusFromMap != null) {
                logger.info("Storing overridden status {} from map", overriddenStatusFromMap);
                registrant.setOverriddenStatus(overriddenStatusFromMap);
            }

            InstanceStatus overriddenInstanceStatus = this.getOverriddenInstanceStatus(registrant, existingLease, isReplication);
            registrant.setStatusWithoutDirty(overriddenInstanceStatus);
            if (InstanceStatus.UP.equals(registrant.getStatus())) {
                lease.serviceUp();   //当serviceUpTimestamp为零时，则为System.currentTimeMillis();
            }

            registrant.setActionType(ActionType.ADDED);  //设置实例的动作类型为add
            this.recentlyChangedQueue.add(new AbstractInstanceRegistry.RecentlyChangedItem(lease)); 在最近改变队列中加入这个实例
            registrant.setLastUpdatedTimestamp();  //设置最后启动时间
            this.invalidateCache(registrant.getAppName(), registrant.getVIPAddress(), registrant.getSecureVipAddress()); //设置responseCache缓存
            logger.info("Registered instance {}/{} with status {} (replication={})", new Object[]{registrant.getAppName(), registrant.getId(), registrant.getStatus(), isReplication});
        } finally {
            this.read.unlock();
        }
```



### 6.2Eureka获取配置过程



### 6.3Eureka心跳检测过程

在SpringCloud环境中服务注册会执行InstanceRegistry类中的renew方法，这个方法会发布一个EurekaInstanceRenewedEvent事件，并且调用AbstractInstanceRegistry的register方法

​	Eureka的服务注册过程主要是执行AbstractInstanceRegistry的renew方法







### 6.4Eureka的缓存机制



### 6.5Eureka的延迟注册



### 6.6Eureka集群的之间的通信



## 7.Eureka的一些api

| Operation                                                    | HTTP action（针对SpringCloudNetflix环境下启动的Eureka）      | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 注册新服务实例或者修改实例基本信息（就是InstanceInfo类）     | POST /eureka/apps/appID                                      | Input:JSON/XMLpayload HTTPCode: 204 on success               |
| 撤销删除服务实例                                             | DELETE /eureka/apps/appID/instanceID                         | HTTP Code: 200 on success                                    |
| 实例心跳                                                     | PUT /eureka/apps/appID/instanceID                            | HTTP Code:* 200 on success * 404 ifinstanceIDdoesn’t exist   |
| 查询所有服务实例列表                                         | GET /eureka/apps                                             | HTTP Code: 200 on success Output:JSON/XML                    |
| 查询某个服务的实例列表                                       | GET /eureka/apps/appID                                       | HTTP Code: 200 on success Output:JSON/XML                    |
| 查询某个服务某个实例信息                                     | GET /eureka/apps/appID/instanceID                            | HTTP Code: 200 on success Output:JSON/XML                    |
| 查询某个实例信息                                             | GET /eureka/instances/instanceID                             | HTTP Code: 200 on success Output:JSON/XML                    |
| 将某个实例设置为下线，这个和删除不同，如果你手动调用删除，但如果客户端还活着，定时任务还是会将实例注册上去。但是改成这个状态，定时任务更新不了这个状态 | PUT /eureka/apps/appID/instanceID/status?value=OUT_OF_SERVICE | HTTP Code:* 200 on success * 500 on failure                  |
| 下线状态恢复                                                 | DELETE /eureka/apps/appID/instanceID/status?value=UP (The value=UP is optional, it is used as a suggestion for the fallback status due to removal of the override) | HTTP Code:* 200 on success * 500 on failure                  |
| 更新元数据（这个不是InstanceInfo，是自己可以往里面自定义的数据） | PUT /eureka/apps/appID/instanceID/metadata?key=value         | HTTP Code: * 200 on success * 500 on failure                 |
| 查询某个VIP下的所有实例                                      | GET /eureka/vips/vipAddress                                  | HTTP Code: 200 on success Output:JSON/XML * 404 if thevipAddressdoes not exist. |
| 查询某个SVIP下的所有实例                                     | GET /eureka/svips/svipAddress                                | HTTP Code: 200 on success Output:JSON/XML * 404 if thesvipAddressdoes not exist. |



## Eureka 服务实例实现快速下线快速感知快速刷新配置解析

```yml
EurekaServer修改如下配置：
#eureka server刷新readCacheMap的时间，注意，client读取的是readCacheMap，这个时间决定了多久会把readWriteCacheMap的缓存更新到readCacheMap上
#默认30s
eureka.server.responseCacheUpdateIntervalMs=3000
#eureka server缓存readWriteCacheMap失效时间，这个只有在这个时间过去后缓存才会失效，失效前不会更新，过期后从registry重新读取注册服务信息，registry是一个ConcurrentHashMap。
#由于启用了evict其实就用不太上改这个配置了
#默认180s
eureka.server.responseCacheAutoExpirationInSeconds=180

#启用主动失效，并且每次主动失效检测间隔为3s
eureka.server.eviction-interval-timer-in-ms=3000
Eureka服务提供方修改如下配置：
#服务过期时间配置,超过这个时间没有接收到心跳EurekaServer就会将这个实例剔除
#注意，EurekaServer一定要设置eureka.server.eviction-interval-timer-in-ms否则这个配置无效，这个配置一般为服务刷新时间配置的三倍
#默认90s
eureka.instance.lease-expiration-duration-in-seconds=15
#服务刷新时间配置，每隔这个时间会主动心跳一次
#默认30s
eureka.instance.lease-renewal-interval-in-seconds=5
Eureka服务调用方修改如下配置：
#eureka client刷新本地缓存时间
#默认30s
eureka.client.registryFetchIntervalSeconds=5
#eureka客户端ribbon刷新时间
#默认30s
ribbon.ServerListRefreshInterval=5000
```

## Eureka监听各服务状态，下线、重连等，并做相应的处理

SpringCloud为Eureka监听各个服务的不同操作发布了不同的event

- EurekaInstanceRegisteredEvent  服务注册事件
- EurekaInstanceRenewedEvent    服务续租时间
- EurekaRegistryAvailableEvent     客户端启动事件
- EurekaServerStartedEvent       Server启动事件
- EurekaInstanceCanceledEvent   服务下线事件

```java
@Component
public class EurekaServerEventListener {

    @EventListener(classes = EurekaServerStartedEvent.class)
    public void eurekaServerStarted(EurekaServerStartedEvent eurekaServerStartedEvent){
        System.out.println("服务启动");
    }

    @EventListener(classes = EurekaInstanceRegisteredEvent.class)
    public void eurekaInstanceRegistered(EurekaInstanceRegisteredEvent eurekaInstanceRegisteredEvent){
        String instanceId = eurekaInstanceRegisteredEvent.getInstanceInfo().getInstanceId();
        System.out.println(instanceId + "服务注册");
    }

    @EventListener(classes = EurekaInstanceRenewedEvent.class)
    public void eurekaInstanceRenewed(EurekaInstanceRenewedEvent eurekaInstanceRenewedEvent){
        String instanceId = eurekaInstanceRenewedEvent.getInstanceInfo().getInstanceId();
        System.out.println(instanceId + "服务续租");
    }

    @EventListener(classes = EurekaRegistryAvailableEvent.class)
    public void eurekaServerStarted(EurekaRegistryAvailableEvent eurekaRegistryAvailableEvent){
        Long time = eurekaRegistryAvailableEvent.getTimestamp();
        System.out.println(time + "client启动");
    }

    @EventListener(classes = EurekaInstanceCanceledEvent.class)
    public void eurekaInstanceCanceled(EurekaInstanceCanceledEvent eurekaInstanceCanceledEvent){
        String instanceId = eurekaInstanceCanceledEvent.getServerId();
        System.out.println(instanceId + "服务下线");
    }
}
```

## Eureka服务注册加密

​	由于Eureka是通过http协议进行服务注册与发现的，所以只需要对其暴露的资源进行保护即可，所以引入Spring Security模块，对这些节点进行加密保护

服务端：

1.引入Spring Security文件，配置相关的认证

```yml
  security:
    user:
      name: admin
      password: admin
```

2.开放相关的路径，使客户端可以注册

```java
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // /eureka路径忽略csrf
        http.csrf().ignoringAntMatchers("/eureka/**");
        super.configure(http);
    }
}
```

客户端：修改相关配置，添加认证

```yml
defaultZone: http://admin:admin@localhost:8080/eureka
```

