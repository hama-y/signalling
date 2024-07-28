FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2-jdk

# 作業ディレクトリを設定
WORKDIR /app

# アプリケーションのJarファイルをコピー
COPY target/signalling-0.0.1-SNAPSHOT.jar signalling.jar

# アプリケーションを実行
ENTRYPOINT ["java", "-jar", "signalling.jar"]