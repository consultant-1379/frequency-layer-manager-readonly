{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-son-frequency-layer-manager.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-son-frequency-layer-manager.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-son-frequency-layer-manager.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
External DataStore Configuration
*/}}
{{- define "eric-son-frequency-layer-manager.externalDB" -}}
{{- if .Values.externalDataStores }}
{{- $externalDbConfig := index .Values "externalDataStores" }}
{{- range $key, $val := $externalDbConfig }}
{{printf "- name: " | indent 10 }}
{{- printf "%s_DB_USER" $key | replace "-" "_" | upper }}
{{printf "valueFrom:" | indent 12 }}
{{printf "secretKeyRef:" | indent 14 }}
{{printf "name: %s" .kubernetesSecretName | indent 16 }}
{{printf "key: username" | indent 16 }}
{{printf "- name: " | indent 10 }}
{{- printf "%s_DB_PASSWORD" $key | replace "-" "_" | upper }}
{{printf "valueFrom:" | indent 12 }}
{{printf "secretKeyRef:" | indent 14 }}
{{printf "name: %s" .kubernetesSecretName | indent 16 }}
{{printf "key: password" | indent 16 }}
{{printf "- name: " | indent 10 }}
{{- printf "%s_DB_EXTERNAL_USER" $key | replace "-" "_" | upper }}
{{printf "valueFrom:" | indent 12 }}
{{printf "secretKeyRef:" | indent 14 }}
{{printf "name: %s" .kubernetesExternalUserSecretName | indent 16 }}
{{printf "key: username" | indent 16 }}
{{printf "- name: " | indent 10 }}
{{- printf "%s_DB_EXTERNAL_PASSWORD" $key | replace "-" "_" | upper }}
{{printf "valueFrom:" | indent 12 }}
{{printf "secretKeyRef:" | indent 14 }}
{{printf "name: %s" .kubernetesExternalUserSecretName | indent 16 }}
{{printf "key: password" | indent 16 }}
{{printf "- name: " | indent 10 }}
{{- printf "%s_DB_DRIVER" $key | replace "-" "_" | upper }}
{{printf "value: org.%sql.Driver" (.databaseVendor | lower) | indent 12  }}
{{printf "- name: " | indent 10 }}
{{- printf "%s_DB_JDBC_CONNECTION" $key | replace "-" "_" | upper}}
{{printf "value: jdbc:postgresql://%s:%g/%s" .databaseHost .databasePort .postgresDatabase | indent 12  }}
{{- end }}
{{- end }}
{{- end -}}

{{ define "eric-son-frequency-layer-manager.global" }}
  {{- $globalDefaults := dict "pullSecret" -}}
  {{ if .Values.global }}
    {{- mergeOverwrite $globalDefaults .Values.global | toJson -}}
  {{ else }}
    {{- $globalDefaults | toJson -}}
  {{ end }}
{{ end }}

{{/*
Create image pull secrets
*/}}
{{- define "eric-son-frequency-layer-manager.pullSecrets" -}}
{{- $g := fromJson (include "eric-son-frequency-layer-manager.global" .) -}}
{{- if .Values.imageCredentials.pullSecret -}}
{{- print .Values.imageCredentials.pullSecret -}}
{{- else -}}
{{- print $g.pullSecret -}}
{{- end -}}
{{- end -}}

{{/*
Create hostname
*/}}
{{- define "eric-son-frequency-layer-manager.hostname" -}}
{{- if .Values.ingress.hostname -}}
{{- print .Values.ingress.hostname -}}
{{- else -}}
{{- if .Values.global -}}
{{- if .Values.global.ingress -}}
{{- if .Values.global.ingress.hostname -}}
{{- print .Values.global.ingress.hostname -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-son-frequency-layer-manager.product-info" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end}}

{{/*
Expand the name of the chart.
*/}}
{{- define "eric-son-frequency-layer-manager.service.name" -}}
{{- default .nameOverride .fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "eric-son-frequency-layer-manager.fullname" -}}
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

{{/*
Return service name to be used in Ingress Resource according to .Values.authorizationProxy.enabled
*/}}
{{- define "eric-son-frequency-layer-manager.service-name" }}
{{- if .Values.authorizationProxy.enabled -}}
{{- print (include "eric-son-frequency-layer-manager.authz-proxy-service-name" .) -}}
{{- else -}}
{{- print (include "eric-son-frequency-layer-manager.name" .) -}}
{{- end -}}
{{- end -}}


{{/*
Support user defined labels (DR-D1121-068)
*/}}
{{- define "eric-son-frequency-layer-manager.user-labels" }}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end }}
{{- end }}