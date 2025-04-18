FROM amazoncorretto:21.0.6
WORKDIR /lh
RUN amazon-linux-extras install epel -y && yum install jemalloc -y
ENV LD_PRELOAD=/usr/lib64/libjemalloc.so.1
COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./server/build/libs/server-*-all.jar /lh/server.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
