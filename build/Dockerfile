FROM amazoncorretto:18

RUN yum install -y procps

COPY build/lhctl /
COPY app/build/libs/app-all.jar /littleHorse.jar
COPY build/entrypoints/* /

RUN mkdir /javaInclude

CMD ["java", "-cp", "/javaInclude:/littleHorse.jar", "io.littlehorse.App"]
