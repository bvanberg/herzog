## Run it 
From the project root `herzog/api`
`./gradlew clean build shadow; java -jar root.jar server dev.yaml`

## Test it
`http://localhost:8080/identification?name=herzog`

### Test posting of photo

From the project root `herzog/api`
`curl -v --header "Content-Type: application/octet-stream" --request POST --data-binary "@coffee-mug.jpg" localhost:8080/identification?fileId=1234567890`

Note the response headers contain the total bytes of the image along with the `fileId` sent in. This is just an example of how we might post an image. multipart may or may not be better.
