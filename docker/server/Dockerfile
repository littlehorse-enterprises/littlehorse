FROM amazoncorretto:25

WORKDIR /lh
RUN yum install -y jemalloc findutils && \
  yum clean all && \
  rm -rf /var/cache/yum
  
RUN mkdir /lh-data

ENV LHS_STATE_DIR=/lh-data
ENV LD_PRELOAD=/usr/lib64/libjemalloc.so.2

COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./server/build/install/server/lib /lh/lib
COPY ./server/build/install/server/server /lh/bin
RUN ["chmod", "+x", "/lh/bin/server"]

ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
