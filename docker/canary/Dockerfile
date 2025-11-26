FROM amazoncorretto:25
WORKDIR /lh
COPY ./docker/canary/docker-entrypoint.sh /lh
COPY ./canary/build/install/canary/lib /lh/lib
COPY ./canary/build/install/canary/bin /lh/bin
RUN ["chmod", "+x", "/lh/bin/canary"]
RUN yum install -y libstdc++ findutils  && \
  yum clean all && \
  rm -rf /var/cache/yum
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["canary"]
