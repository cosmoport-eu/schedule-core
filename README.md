# Cosmoport Core


[API](http://localhost:8081/swagger-ui/index.html) (Available when app is running)

## Default settings

- Host: **127.0.0.1** (localhost)
- Port: **8081**

## Run

To run the application you should execute the following script:

```text
gradlew bootRun
```

## Distribute

For the portable app distribution:

```text
gradlew build
```

The `build/libs/cosmocore.jar` will be the resulting file.  
Put it in the same folder the `core0.db` file and run it with `java -jar cosmocore.jar`.
