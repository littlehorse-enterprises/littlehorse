# LittleHorse Canary App

Start a local kafka cluster:

```
../local-dev/setup.sh
```

Start LH Server:

```
../local-dev/do-server.sh
```

Start LH Canary:

```
../local-dev/do-canary.sh
```

Get metrcis:

```
http -b :4023/metrics
```
