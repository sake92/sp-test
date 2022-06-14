# snowplow-test

## Running the service

First you need to have a Postgres instance running on port 54321:
```sh
docker run --name snowplow-pg -p 54321:5432 -e POSTGRES_PASSWORD=snowplow -e POSTGRES_USER=snowplow -d postgres:14.1
```

Then run the service:
```sh
sbt run
```

## Sample requests

You can import the `snowplowtechtest.postman_collection.json` file into Postman and try it out.
