#### 1. DM在K8s环境中部署

###### 1.1 创建ConfigMap

```shell
# Deploys a new Namespace for the MinIO Pod
apiVersion: v1
kind: Namespace
metadata:
  name: minio-dev # Change this value if you want a different namespace name
  labels:
    name: minio-dev # Change this value to match metadata.name
---
# Deploys a new MinIO Pod into the metadata.namespace Kubernetes namespace
#
# The `spec.containers[0].args` contains the command run on the pod
# The `/data` directory corresponds to the `spec.containers[0].volumeMounts[0].mountPath`
# That mount path corresponds to a Kubernetes HostPath which binds `/data` to a local drive or volume on the worker node where the pod runs
# 
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: minio
  name: minio
  namespace: minio-dev # Change this value to match the namespace metadata.name
spec:
  containers:
  - name: minio
    image: minio/minio:latest
    command:
    - /bin/bash
    - -c
    args: 
    - minio server /data --console-address :9090
    ports:
    - containerPort: 9090
    volumeMounts:
    - mountPath: /data
      name: localvolume # Corresponds to the `spec.volumes` Persistent Volume
  nodeSelector:
    kubernetes.io/hostname: server # Specify a node label associated to the Worker Node on which you want to deploy the pod.
  volumes:
  - name: localvolume
    hostPath: # MinIO generally recommends using locally-attached volumes
      path: /data/minio # Specify a path to a local drive or volume on the Kubernetes worker node
      type: DirectoryOrCreate # The path to the last directory must exist
```

###### 2.2 创建 HostPath 存储卷和 MySQL 的 Deployment

```yaml
apiVersion: v1
kind: Service
metadata:
  name: minio-service
  namespace: minio-dev
spec:
  selector:
    app: minio
  ports:
    - protocol: TCP
      port: 30090
      targetPort: 9090
      nodePort: 30090
  type: NodePort
```
