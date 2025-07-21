FROM amazoncorretto:21
WORKDIR /lh
RUN amazon-linux-extras install epel -y && yum install jemalloc -y
ENV LD_PRELOAD=/usr/lib64/libjemalloc.so.1
COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./server/build/install/server/lib /lh/lib
COPY ./server/build/install/server/server /lh/bin
RUN ["chmod", "+x", "/lh/bin/server"]
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
