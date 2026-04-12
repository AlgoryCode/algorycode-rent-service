{{- define "algorycode-rent-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "algorycode-rent-service.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "algorycode-rent-service.labels" -}}
app.kubernetes.io/name: {{ include "algorycode-rent-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
{{- end -}}

{{/*
Eureka defaultZone: pod içinden erişilebilen tam URL.
- clientServiceUrlDefaultZone doluysa olduğu gibi kullanılır.
- crossNamespace.enabled: Eureka başka namespace’te → cluster DNS FQDN.
- aksi halde aynı namespace kısa isim (http://servis:port/eureka/).
*/}}
{{- define "algorycode-rent-service.eurekaDefaultZone" -}}
{{- $explicit := trim (.Values.eureka.clientServiceUrlDefaultZone | default "") }}
{{- if $explicit }}
{{- $explicit }}
{{- else if .Values.eureka.crossNamespace.enabled }}
{{- printf "http://%s.%s.svc.cluster.local:%v/eureka/" .Values.eureka.crossNamespace.k8sServiceName .Values.eureka.crossNamespace.k8sNamespace (int .Values.eureka.crossNamespace.port) }}
{{- else }}
{{- printf "http://%s:%v/eureka/" .Values.eureka.sameNamespace.serviceName (int .Values.eureka.sameNamespace.port) }}
{{- end }}
{{- end }}
