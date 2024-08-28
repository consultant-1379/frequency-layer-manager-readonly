{{/*
Create eric-son-frequency-layer-manager image path
*/}}
{{- define "eric-son-frequency-layer-manager.imagePath" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := index $productInfo "images" "eric-son-frequency-layer-manager" "registry" -}}
  {{- $repoPath := index $productInfo "images" "eric-son-frequency-layer-manager" "repoPath" -}}
  {{- $name := index $productInfo "images" "eric-son-frequency-layer-manager" "name" -}}
  {{- $tag := index $productInfo "images" "eric-son-frequency-layer-manager" "tag" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl = .Values.global.registry.url -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if .Values.imageCredentials -}}
    {{- if index .Values "imageCredentials" "eric-son-frequency-layer-manager" -}}
      {{- if index .Values "imageCredentials" "eric-son-frequency-layer-manager" "registry" -}}
        {{- if index .Values "imageCredentials" "eric-son-frequency-layer-manager" "registry" "url" -}}
          {{- $registryUrl = index .Values "imageCredentials" "eric-son-frequency-layer-manager" "registry" "url" -}}
        {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-son-frequency-layer-manager" "repoPath")) -}}
        {{- $repoPath = index .Values "imageCredentials" "eric-son-frequency-layer-manager" "repoPath" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if index .Values "images"  -}}
      {{- if index .Values "images" "eric-son-frequency-layer-manager" -}}
        {{- if not (kindIs "invalid" (index .Values "images" "eric-son-frequency-layer-manager" "tag")) -}}
          {{- $tag = index .Values "images" "eric-son-frequency-layer-manager" "tag" -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
  {{- end -}}
  {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create eric-son-frequency-layer-manager-db image path
*/}}
{{- define "eric-son-frequency-layer-manager.eric-son-frequency-layer-manager-db.imagePath" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := index $productInfo "images" "eric-data-document-database-pg13" "registry" -}}
  {{- $repoPath := index $productInfo "images" "eric-data-document-database-pg13" "repoPath" -}}
  {{- $name := index $productInfo "images" "eric-data-document-database-pg13" "name" -}}
  {{- $tag := index $productInfo "images" "eric-data-document-database-pg13" "tag" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl = .Values.global.registry.url -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if .Values.imageCredentials -}}
    {{- if index .Values "imageCredentials" "eric-data-document-database-pg13" -}}
      {{- if index .Values "imageCredentials" "eric-data-document-database-pg13" "registry" -}}
        {{- if index .Values "imageCredentials" "eric-data-document-database-pg13" "registry" "url" -}}
          {{- $registryUrl = index .Values "imageCredentials" "eric-data-document-database-pg13" "registry" "url" -}}
        {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-data-document-database-pg13" "repoPath")) -}}
        {{- $repoPath = index .Values "imageCredentials" "eric-data-document-database-pg13" "repoPath" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if index .Values "images"  -}}
    {{- if index .Values "images" "eric-data-document-database-pg13" -}}
      {{- if not (kindIs "invalid" (index .Values "images" "eric-data-document-database-pg13" "tag")) -}}
        {{- $tag = index .Values "images" "eric-data-document-database-pg13" "tag" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
  {{- end -}}
  {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create jmxExporter image path
*/}}
{{- define "eric-son-frequency-layer-manager.jmxExporter.imagePath" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := index $productInfo "images" "jmx-exporter-multi-port" "registry" -}}
  {{- $repoPath := index $productInfo "images" "jmx-exporter-multi-port" "repoPath" -}}
  {{- $name := index $productInfo "images" "jmx-exporter-multi-port" "name" -}}
  {{- $tag := index $productInfo "images" "jmx-exporter-multi-port" "tag" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl = .Values.global.registry.url -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if .Values.imageCredentials -}}
    {{- if index .Values "imageCredentials" "jmx-exporter-multi-port" -}}
      {{- if index .Values "imageCredentials" "jmx-exporter-multi-port" "registry" -}}
        {{- if index .Values "imageCredentials" "jmx-exporter-multi-port" "registry" "url" -}}
          {{- $registryUrl = index .Values "imageCredentials" "jmx-exporter-multi-port" "registry" "url" -}}
        {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" (index .Values "imageCredentials" "jmx-exporter-multi-port" "repoPath")) -}}
        {{- $repoPath = index .Values "imageCredentials" "jmx-exporter-multi-port" "repoPath" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
  {{- end -}}
  {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create messagebuskf image path
*/}}
{{- define "eric-son-frequency-layer-manager.messagebuskf.imagePath" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := index $productInfo "images" "eric-data-message-bus-kf" "registry" -}}
  {{- $repoPath := index $productInfo "images" "eric-data-message-bus-kf" "repoPath" -}}
  {{- $name := index $productInfo "images" "eric-data-message-bus-kf" "name" -}}
  {{- $tag := index $productInfo "images" "eric-data-message-bus-kf" "tag" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl = .Values.global.registry.url -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if .Values.imageCredentials -}}
    {{- if index .Values "imageCredentials" "eric-data-message-bus-kf" -}}
      {{- if index .Values "imageCredentials" "eric-data-message-bus-kf" "registry" -}}
        {{- if index .Values "imageCredentials" "eric-data-message-bus-kf" "registry" "url" -}}
          {{- $registryUrl = index .Values "imageCredentials" "eric-data-message-bus-kf" "registry" "url" -}}
        {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-data-message-bus-kf" "repoPath")) -}}
        {{- $repoPath = index .Values "imageCredentials" "eric-data-message-bus-kf" "repoPath" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
  {{- end -}}
  {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create basic flm api/kpi/cm test image path
*/}}
{{- define "eric-son-frequency-layer-manager.basic-flm-cm-kpi-api-test.imagePath" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := index $productInfo "images" "ecson-basic-helm-test" "registry" -}}
  {{- $repoPath := index $productInfo "images" "ecson-basic-helm-test" "repoPath" -}}
  {{- $name := index $productInfo "images" "ecson-basic-helm-test" "name" -}}
  {{- $tag := index $productInfo "images" "ecson-basic-helm-test" "tag" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl = .Values.global.registry.url -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if .Values.imageCredentials -}}
    {{- if index .Values "imageCredentials" "ecson-basic-helm-test" -}}
      {{- if index .Values "imageCredentials" "ecson-basic-helm-test" "registry" -}}
        {{- if index .Values "imageCredentials" "ecson-basic-helm-test" "registry" "url" -}}
          {{- $registryUrl = index .Values "imageCredentials" "ecson-basic-helm-test" "registry" "url" -}}
        {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" (index .Values "imageCredentials" "ecson-basic-helm-test" "repoPath")) -}}
        {{- $repoPath = index .Values "imageCredentials" "ecson-basic-helm-test" "repoPath" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if index .Values "images"  -}}
    {{- if index .Values "images" "ecson-basic-helm-test" -}}
      {{- if not (kindIs "invalid" (index .Values "images" "ecson-basic-helm-test" "tag")) -}}
        {{- $tag = index .Values "images" "ecson-basic-helm-test" "tag" -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
  {{- end -}}
  {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}
