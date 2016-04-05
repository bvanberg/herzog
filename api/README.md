Project Dependencies and Configuration
---
A few notes on IntelliJ configuration for this project that'll be required for annotations to be processed properly and make IntelliJ happy.

#### Enable Annotation Processing
Preferences --> Build, Execution, Deployment --> Annotation Processors (check Enable annotation processing checkbox).

#### Plugins

Download and Install the Lombok Plugin

## Run it
From the project root `herzog/api`

`./gradlew clean build shadow; java -jar root.jar server dev.yaml`

## Deploy it
The API currently run on elastic beanstalk. This is controlled by two files; Procfile and Buildfile. beanstalk uses Buildfile to build the application JAR file from a source bundle containing the API source. beanstalk uses Procfile to execute the application. 

1. Create the source bundle `herzog/api (master)$ git archive --format=zip HEAD > photo-id.zip`
2. Deploy the source bundle from the AWS Elastic Beanstalk console `https://console.aws.amazon.com/elasticbeanstalk/home?region=us-east-1#/applications` 
3. Select the environment you wish to deploy. (photo-id)
4. Select `Upload and Deploy` and increment the version label (photo-id-nn)
5. Select `Deploy`

## Endpoints

### GET `/identification/photos`

Returns photos in the photo store in a paging manner. Currently it is ghetto paging with a `page` and `pageSize` query parameter available and very little validation of these parameters. 

Example usage: 

`http://localhost:8080/identification/photos`

`http://localhost:8080/identification/photos?page=4`

`http://localhost:8080/identification/photos?page=4&pageSize=3`

### GET `/identification/photo/url`

Example usage: 

`curl http://localhost:8080/identification/photo/url`

Example response:

    {
        presignedUrl: "https://herzog-photos.s3.amazonaws.com/8caaccfd-37eb-4807-a2ad-2514d197b037?AWSAccessKeyId=AKIAI6HUYJLEME6L44TA&Expires=1458801265&Signature=BVbhmZ6A%2FfxneDmZXzgL1F3LuZg%3D",
        key: "8caaccfd-37eb-4807-a2ad-2514d197b037"
    }

### GET `/identification/photo/metadata`

Example usage: 

`curl http://localhost:8080/identification/photo/metadata`

Example response: 

    {
        metadata: {
            key1: "value1",
            key2: "value2"
        },
        photoKeys: [
            "key1",
            "key2"
        ],
        userId: "user"
    }

### POST `/identification/photo/metadata`

Example usage: 

`curl -v -H "Content-Type: application/json" -X POST -d '{"metadata":{"key1":"value1","key2":"value2"},"photoKeys":["key1","key2"],"userId":"user"}' http://localhost:8080/identification/photo/metadata`
