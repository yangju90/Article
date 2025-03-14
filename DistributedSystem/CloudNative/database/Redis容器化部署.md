#### 1. Redis在K8s环境中部署

###### 1.1 创建ConfigMap

```shell
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-single-config
data:
  redis.conf: |
    daemonize no
    bind 0.0.0.0
    port 6379
    tcp-backlog 511
    timeout 0
    tcp-keepalive 300
    pidfile /data/redis-server.pid
    logfile /data/redis.log
    loglevel notice
    databases 16
    always-show-logo yes
    save 900 1
    save 300 10
    save 60 10000
    stop-writes-on-bgsave-error yes
    rdbcompression yes
    rdbchecksum yes
    dbfilename dump.rdb
    dir /data
    slave-serve-stale-data yes
    slave-read-only yes
    repl-diskless-sync no
    repl-diskless-sync-delay 5
    repl-disable-tcp-nodelay no
    slave-priority 100
    appendonly yes
    appendfilename "appendonly.aof"
    appendfsync everysec
    no-appendfsync-on-rewrite no
    auto-aof-rewrite-percentage 100
    auto-aof-rewrite-min-size 64mb
    aof-load-truncated yes
    lua-time-limit 5000
    slowlog-log-slower-than 10000
    slowlog-max-len 128
    latency-monitor-threshold 0
    notify-keyspace-events ""
    hash-max-ziplist-entries 512
    hash-max-ziplist-value 64
    list-max-ziplist-size -2
    list-compress-depth 0
    set-max-intset-entries 512
    zset-max-ziplist-entries 128
    zset-max-ziplist-value 64
    hll-sparse-max-bytes 3000
    activerehashing yes
    client-output-buffer-limit normal 0 0 0
    client-output-buffer-limit slave 256mb 64mb 60
    client-output-buffer-limit pubsub 32mb 8mb 60
    hz 10
    aof-rewrite-incremental-fsync yes
    requirepass redis#single#test
```

###### 2.2 创建 HostPath 存储卷和 Redis 的 Deployment

```yaml
apiVersion: v1
kind: Service
metadata:
  name: service-redis-single
  labels:
    app: redis-single
spec:
  selector:
    app: redis-single
  ports:
    - name: redis-single
      port: 30379
      targetPort: 6379
      nodePort: 30379
  type: NodePort
```

###### 2.3 创建 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-single
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis-single
  template:
    metadata:
      labels:
        app: redis-single
    spec:
      initContainers:
        - name: init-0
          image: db/busybox:1.37.0
          imagePullPolicy: IfNotPresent
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          command: [ "sysctl", "-w", "net.core.somaxconn=511" ]
          securityContext:
            privileged: true
      containers:
        - name: redis-single
          image: redis:7
          imagePullPolicy: IfNotPresent
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          volumeMounts:
            - name: redis-data
              mountPath: /data
            - name: redis-config
              mountPath: /usr/local/etc/redis/redis.conf
              subPath: redis.conf
          command: [ "redis-server" ,"/usr/local/etc/redis/redis.conf" ]
          env:
            - name: TZ
              value: "Asia/Shanghai"
      volumes:
        - name: timezone
          hostPath:
            path: /usr/share/zoneinfo/Asia/Shanghai
        - name: redis-data
          hostPath:
            path: /data/redis
            type: DirectoryOrCreate
        - name: redis-config
          configMap:
            name: redis-single-config
            items:
              - key: redis.conf
                path: redis.conf
```

###### 2.4 集群参考

`https://blog.csdn.net/muguazhi/article/details/132455056`