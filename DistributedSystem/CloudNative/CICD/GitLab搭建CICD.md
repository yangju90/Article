<div align=center><font face="黑体" size=6>GitLab搭建CI/CD</font></div>

# 1.安装

#### 1.1 安装gitlab

通过doker镜像安装 `kubectl apply -f  gitlab-deploy.yaml -n gitlab`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gitlab
spec:
  replicas: 1
  selector:
    matchLabels:
      run: gitlab
  template:
    metadata:
      labels:
        run: gitlab
    spec:
      # nodeSelector保证pod 运维到指定label节点
      nodeSelector:
        indi.mat.gitlab: gitlab
      # emptyDir 保证 pod 异常关闭停止，数据还在（如果不配置，每次重启集群，gitlab用户数据丢失）
      volumes:
      - name: v-data
        emptyDir: {}
      - name: v-opt
        emptyDir: {}
      - name: v-opt
        emptyDir: {}
      containers:
      - image: gitlab/gitlab-ee
        imagePullPolicy: IfNotPresent
        name: gitlab
        volumeMounts:
        - mountPath: /var/opt/gitlab
          name: v-data
        - mouuntPath: /var/log/gitlab
          name: v-log
        - mountPath: /etc/gitlab
          name: v-opt
        
---
apiVersion: v1
kind: Service
metadata:
  name: gitlab
spec:
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 80
  selector:
    run: gitlab
  type: NodePort
```



#### 1.2 安装 gitlab-runner
