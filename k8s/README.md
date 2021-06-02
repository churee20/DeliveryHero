로컬환경 세팅

    현재 Mac 기준 세팅으로 각자 환경에 따라 맞는 로컬환경 설치 필요

    1. awscli 설치
       로컬 환경에서 AWS를 제어하기 위해 설치
       https://aws.amazon.com/ko/cli/
    
    파이썬 설치
    ➜  ~ pip3 install awscli
    
    
    2. EKS Client (eksctl) 설치
       https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/eksctl.html
    
    ➜  ~ brew tap weaveworks/tap
    ➜  ~ brew install weaveworks/tap/eksctl

클러스터 생성

    - AWS 관리콘솔 IAM에서 자격증명 생성
    * AWS 관리콘솔 접속
    * IAM 서비스의 ‘사용자’ 메뉴 접속
    * 사용자의 Access Key와 Secret Access Key 복사

    - Cloud IDE - AWS Client Config
      $ aws configure
      $ Input Access Key
      $ Input Secret Access Key
      $ Input your Region Code
      $ Input 'json' in Output format

    - EKS (Elastic Kubernetes Service) 생성
    ➜  ~ eksctl create cluster --name user01-eks --version 1.17 --nodegroup-name standard-workers --node-type t3.medium --nodes 4 --nodes-min 1 --nodes-max 4

    [참조]실습 스크립트 - 컨테이너 오케스트레이션 Lab. Guide
    http://msaschool.io/operation/operation/operation-seven/

    클러스터 설정 로컬 저장
    ➜  ~ aws eks --region ap-northeast-2 update-kubeconfig --name user01-eks

k8s에 kafka 설치 및 실행

    [참조]kafka 설치
    http://msaschool.io/operation/implementation/implementation-seven/

    로컬환경 세팅(helm 설치)
    ➜  ~ brew install helm

    kafka 설치
    ➜  ~ kubectl --namespace kube-system create sa tiller
    serviceaccount/tiller created
    ➜  ~ kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
    clusterrolebinding.rbac.authorization.k8s.io/tiller created
    ➜  ~ helm repo add incubator https://charts.helm.sh/incubator
    "incubator" has been added to your repositories
    ➜  ~ helm repo update
    Hang tight while we grab the latest from your chart repositories...
    ...Successfully got an update from the "incubator" chart repository
    ...Successfully got an update from the "bitnami" chart repository
    Update Complete. ⎈Happy Helming!⎈
    ➜  ~ kubectl create ns kafka
    namespace/kafka created
    ➜  ~ helm install my-kafka --namespace kafka incubator/kafka
    WARNING: This chart is deprecated
    NAME: my-kafka
    .. 이하 생략

ECR 생성
    customer/delivery/gateway/order/product 개별 수행

    ➜  ~ aws ecr create-repository --repository-name customer --region ap-northeast-2
    {
    "repository": {
    "repositoryArn": "arn:aws:ecr:ap-northeast-2:740569282574:repository/customer",
    "registryId": "740569282574",
    "repositoryName": "customer",
    "repositoryUri": "740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/customer",
    "createdAt": 1621481757.0,
    "imageTagMutability": "MUTABLE",
    "imageScanningConfiguration": {
    "scanOnPush": false
    },
    "encryptionConfiguration": {
    "encryptionType": "AES256"
    }
    }
    }

Docker 이미지 생성 및 ECR push
    customer/delivery/gateway/order/product 개별 수행

    이미지 생성
    ➜  customer (main) ✗ docker build -t 740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/customer:v1 .
    
    이미지 확인(1회)
    ➜  customer (main) ✗ docker images
    REPOSITORY                                                   TAG       IMAGE ID       CREATED         SIZE
    740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/customer   v1        22ac70a0386d   2 minutes ago   164MB

    ECR 인증(1회)
    ➜  customer (main) ✗ aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/
    Login Succeeded

    ECR push
    ➜  customer (main) ✗ docker push 740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/customer:v1
    The push refers to repository [740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/customer]
    d3ffee86f8ff: Pushed
    ceaf9e1ebef5: Pushed
    9b9b7f3d56a0: Pushed
    f1b5933fe4b5: Pushed
    v1: digest: sha256:e01dac57504bc29da660fba46e1db01e19582ddd53fe5b384c3570e95391a609 size: 1159

[k8s] 

namespace 생성

    ➜  ~ kubectl create ns coffee
    namespace/coffee created

Service 생성

    ➜  ~ kubectl apply -f /Users/joonhopark/workspace/study/coffee/product/kubernetes/service.yaml
    service/product created

Deployment 생성

    ➜  ~ kubectl apply -f /Users/joonhopark/workspace/study/coffee/product/kubernetes/deployment.yml
    deployment.apps/product created
    주의점: ECR image 경로가 맞아야함


Gateway 작업

    ➜  ~ kubectl create deploy gateway -n coffee --image=740569282574.dkr.ecr.ap-northeast-2.amazonaws.com/gateway:v1
    deployment.apps/gateway created
    ➜  ~ kubectl expose deploy gateway -n coffee --type="LoadBalancer" --port=8080
    service/gateway exposed

HPA 적용

    ➜  ~ kubectl autoscale deployment gateway -n coffee --cpu-percent=25 --min=2 --max=4
    horizontalpodautoscaler.autoscaling/gateway autoscaled

➜  ~ kubectl get --raw /apis/metrics.k8s.io/v1beta1
➜  ~ kubectl apply -f components.yaml

ConfigMap

    kubectl apply -f /Users/joonhopark/workspace/study/coffee/k8s/report-configmap.yml
