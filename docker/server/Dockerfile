FROM amazoncorretto:21-alpine3.22
RUN adduser -h /home/lh -D --shell /bin/bash appuser
RUN apk add --no-cache jemalloc bash
WORKDIR /lh
ENV LD_PRELOAD=/usr/lib64/libjemalloc.so.1
COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./server/build/install/server/lib /lh/lib
COPY ./server/build/install/server/server /lh/bin
RUN ["chmod", "+x", "/lh/bin/server"]
USER appuser
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
