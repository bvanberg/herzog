Project Dependencies and Configuration
---
A few notes on IntelliJ configuration for this project that'll be required for annotations to be processed properly and make IntelliJ happy.

#### Enable Annotation Processing
Preferences --> Build, Execution, Deployment --> Annotation Processors (check Enable annotation processing checkbox).

#### Plugins

Download and Install the Lombok Plugin

## Endpoints

### POST `/identification` -- Accepts a binary payload for processing .

### GET `/identification/photos`

Returns photos in the photo store in a paging manner. Currently it is ghetto paging with a `page` and `pageSize` query parameter available and very little validation of these parameters. 

Example usage: 

`http://localhost:8080/identification/photos`

`http://localhost:8080/identification/photos?page=4`

`http://localhost:8080/identification/photos?page=4&pageSize=3`

## Run it
From the project root `herzog/api`

`./gradlew clean build shadow; java -jar root.jar server dev.yaml`

## Test it
`http://localhost:8080/identification?name=herzog`

### Test posting of photo

From the project root `herzog/api`

`curl -v --header "Content-Type: application/octet-stream" --request POST --data-binary "@coffee-mug.jpg" localhost:8080/identification?fileId=1234567890`

Note the response headers contain the total bytes of the image along with the `fileId` sent in. This is just an example of how we might post an image. multipart may or may not be better.

### Test posting of metadata

`curl -H "Content-Type: application/json" -X POST -d '{"metadata":{"key1":"value1","key2":"value2"}}' http://localhost:8080/identification/photo/metadata`
