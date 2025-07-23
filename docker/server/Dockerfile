FROM amazoncorretto:21-alpine3.22
ENV LHS_STATE_DIR=/lh-data
ENV LD_PRELOAD=/usr/lib64/libjemalloc.so.1
RUN adduser -h /home/lh -D --shell /bin/bash appuser
RUN apk add --no-cache jemalloc bash
WORKDIR /lh
RUN mkdir /lh-data
COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./server/build/install/server/lib /lh/lib
COPY ./server/build/install/server/server /lh/bin
RUN ["chmod", "+x", "/lh/bin/server"]
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]