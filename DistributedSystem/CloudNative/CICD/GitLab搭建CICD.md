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
      containers:
      - image: gitlab/gitlab-ee
        imagePullPolicy: IfNotPresent
        name: gitlab
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
