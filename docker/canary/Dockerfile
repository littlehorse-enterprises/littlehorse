FROM amazoncorretto:24-alpine
WORKDIR /lh
COPY ./docker/canary/docker-entrypoint.sh /lh
COPY ./canary/build/install/canary/lib /lh/lib
COPY ./canary/build/install/canary/bin /lh/bin

RUN apk add --no-cache libstdc++ 
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["canary"]
