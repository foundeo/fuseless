# FuseLess

![Build Status](https://github.com/foundeo/fuseless/actions/workflows/ci.yml/badge.svg)


FuseLess is a set of tools and code for running CFML applications on serverless computing platforms (such as AWS Lambda).

## Getting Started

Checkout the [FuseLess Template](https://github.com/foundeo/fuseless-template) to get started.

## Local Development

Install Docker, Java, Gradle and AWS Sam CLI, then run `test.sh` 

### Using FuseLess for non API Gateway Events

You can also use FuseLess to process other lambda events besides those generated from API Gateway by using the `EventLambdaHandler` class. By default this class will attempt to invoke the CFML method `fuselessEvent(eventPayload, fuselessContext)` in `Application.cfc` 


	FuselessExampleEvent:
    Type: AWS::Serverless::Function
    Properties:
      Handler: com.foundeo.fuseless.EventLambdaHandler::handleRequest
      CodeUri: ./build/distributions/test.zip
      Runtime: java8
      Timeout: 100
      MemorySize: 512


The `eventPayload` will typically be a JSON string that you can then parse and work with. You can test generating events with `sam local generate-event` for example:

	sam local generate-event s3 put > /tmp/test-event.json

Now you can use sam local to invoke the event for testing: 

	sam local invoke FuselessExampleEvent --event /tmp/test-event.json 

By default Fuseless will attempt to invoke the function `fuselessEvent`   

## Support, Questions, Issues

[Contact Foundeo Inc.](https://foundeo.com/consulting/coldfusion/) 