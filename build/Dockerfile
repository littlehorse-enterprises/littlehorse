FROM amazoncorretto:18

COPY app/build/libs/app-all.jar /littleHorse.jar

RUN mkdir /javaInclude

CMD ["java", "-cp", "/javaInclude:/littleHorse.jar", "io.littlehorse.App"]
