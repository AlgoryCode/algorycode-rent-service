# algorycode-rent-service — Helm + Kubernetes

`services-platform/helm/algory.qrservice` ve `GreenProjectBE/.../authservice` desenine yakın, bağımsız bir chart. Görüntü portu **8090** (`server.port` ve `pom.xml` Jib ile uyumlu).

## Önkoşullar

- Kubernetes kümesi ve `kubectl` / `helm` yapılandırılmış olmalı.
- Kümede erişilebilir **PostgreSQL** ve **RabbitMQ** (host adlarını `values.yaml` ile eşleştirin).

## 1) Jib ile imaj (AuthService / mail-service ile aynı düzen)

`pom.xml`: Jib **3.5.1**, taban `eclipse-temurin:25-jre-alpine`, hedef `tarikhamarat/algorycode-rent-service:${jib.image.tag}` (varsayılan etiket `r1`). Yeni sürümde `jib.image.tag` ve `values.yaml` içindeki `image.tag` değerini birlikte artır.

```bash
cd algorycode-rent-service
docker login
./mvnw -DskipTests package jib:build
```

CI’de ek etiket için AuthService’teki gibi: `-Djib.to.image=... -Djib.to.tags=...` (ör. `.github/workflows` içindeki `jib:build` satırları).

## 2) Helm ile kurulum / güncelleme

PostgreSQL bağlantısı imaj içindeki `application.yml` ile gelir; Helm Secret’ta `SPRING_DATASOURCE_*` yok.

```bash
helm upgrade --install rent-api ./helm/algorycode-rent-service \
  --namespace YOUR_NS --create-namespace \
  --set image.repository=YOUR_REGISTRY/algorycode-rent-service \
  --set image.tag=r1 \
  --set secret.data.springRabbitmqPassword='***' \
  --set rabbitmq.host=rabbitmq.YOUR_NS.svc.cluster.local
```

Şifreleri repoda tutmamak için ayrı bir `values-prod.yaml` (gitignore) kullanabilirsiniz:

```bash
helm upgrade --install rent-api ./helm/algorycode-rent-service -n YOUR_NS -f values-prod.yaml
```

## 3) Ingress (isteğe bağlı)

`values.yaml` içinde `ingress.enabled: true` yapıp host ve `ingressClassName` değerlerini doldurun.

## Doğrulama

```bash
kubectl get pods,svc -n YOUR_NS -l app.kubernetes.io/instance=rent-api
kubectl port-forward -n YOUR_NS svc/rent-api-algorycode-rent-service 8090:8090
curl -s http://127.0.0.1:8090/actuator/health
```

Chart adı uzun release ile servis adı `RELEASE-NAME-algorycode-rent-service` olur; `fullnameOverride` ile kısaltılabilir.
