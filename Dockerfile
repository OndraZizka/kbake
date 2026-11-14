FROM maven:3.9.9-eclipse-temurin-17 AS builder

LABEL maintainer="https://jbake.org/community/team.html"

ENV JBAKE_HOME=/opt/jbake
ARG JBAKE_VERSION=5.0.0-rc1

WORKDIR /usr/src/jbake
COPY . .

RUN mkdir -p /root/.m2 \
    && printf '<toolchains><toolchain><type>jdk</type><provides><version>17</version></provides><configuration><jdkHome>%s</jdkHome></configuration></toolchain></toolchains>\n' "$JAVA_HOME" > /root/.m2/toolchains.xml \
    && mvn -B -DskipTests -pl jbake-dist -am verify \
    && mkdir -p "$JBAKE_HOME" /tmp/jbake-dist \
    && unzip -q "jbake-dist/target/jbake-${JBAKE_VERSION}-bin.zip" -d /tmp/jbake-dist \
    && cp -a "/tmp/jbake-dist/jbake-${JBAKE_VERSION}/." "$JBAKE_HOME/" \
    && rm -rf /tmp/jbake-dist /usr/src/jbake

FROM eclipse-temurin:17-jre-alpine

ENV JBAKE_USER=jbake
ENV JBAKE_HOME=/opt/jbake
ENV PATH ${JBAKE_HOME}/bin:$PATH
ENV TZ=UTC

RUN apk --no-cache update && \
    apk --no-cache upgrade && \
    apk add --update tzdata && \
    rm -rf /var/cache/apk/*

RUN echo ${TZ} > /etc/timezone

RUN adduser -D -u 1000 -g "" ${JBAKE_USER} ${JBAKE_USER}

USER ${JBAKE_USER}

COPY --from=builder /opt/jbake /opt/jbake

WORKDIR /mnt/site

VOLUME ["/mnt/site"]

ENTRYPOINT ["jbake"]
CMD ["-b"]

EXPOSE 8820
