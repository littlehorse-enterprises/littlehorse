FROM amazoncorretto:18

RUN yum install -y procps

COPY ./build/lhctl /lhctl
COPY app/build/libs/app-all.jar /littleHorse.jar
