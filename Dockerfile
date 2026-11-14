FROM maven:3.9.9-eclipse-temurin-17 AS builder

LABEL maintainer="https://github.com/OndraZizka/kbake"

ENV KBAKE_HOME=/opt/kbake
ARG KBAKE_VERSION=5.0.0-rc1

WORKDIR /usr/src/kbake
COPY . .

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /root/.m2 \
    && printf '<toolchains><toolchain><type>jdk</type><provides><version>17</version></provides><configuration><jdkHome>%s</jdkHome></configuration></toolchain></toolchains>\n' "$JAVA_HOME" > /root/.m2/toolchains.xml \
    && mvn -B -DskipTests -pl kbake-dist -am verify \
    && mkdir -p "$KBAKE_HOME" /tmp/kbake-dist \
    && unzip -q "kbake-dist/target/kbake-${KBAKE_VERSION}-bin.zip" -d /tmp/kbake-dist \
    && cp -a "/tmp/kbake-dist/kbake-${KBAKE_VERSION}/." "$KBAKE_HOME/" \
    && rm -rf /tmp/kbake-dist /usr/src/kbake

FROM eclipse-temurin:17-jre-alpine

ENV KBAKE_USER=kbake
ENV KBAKE_HOME=/opt/kbake
ENV PATH=${KBAKE_HOME}/bin:$PATH
ENV TZ=UTC

RUN apk --no-cache update && \
    apk --no-cache upgrade && \
    apk add --update tzdata && \
    rm -rf /var/cache/apk/*

RUN echo ${TZ} > /etc/timezone

RUN adduser -D -u 1000 -g "" ${KBAKE_USER} ${KBAKE_USER}

USER ${KBAKE_USER}

COPY --from=builder /opt/kbake /opt/kbake

WORKDIR /mnt/site

VOLUME ["/mnt/site"]

ENTRYPOINT ["kbake"]
CMD ["-b"]

EXPOSE 8820
