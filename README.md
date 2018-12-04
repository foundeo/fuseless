# FuseLess

[![Build Status](https://travis-ci.org/foundeo/fuseless.svg?branch=master)](https://travis-ci.org/foundeo/fuseless)


FuseLess is a set of tools and code for running CFML applications on serverless computing platforms (such as AWS Lambda).

## Getting Started

Checkout the [FuseLess Template](https://github.com/foundeo/fuseless-template) to get started.

## Local Development

Install Docker, Java, Gradle and AWS Sam CLI, then run `test.sh` 

### Using FuseLess for non API Gateway Events

You can also use FuseLess to process other lambda events besides those generated from API Gateway by using the `handleEventRequest` method in the `StreamLambdaHandler` class.

	FuselessExampleEvent:
    Type: AWS::Serverless::Function
    Properties:
      Handler: com.foundeo.fuseless.StreamLambdaHandler::handleEventRequest
      CodeUri: ./build/distributions/test.zip
      Runtime: java8
      Timeout: 100
      MemorySize: 512

When the `handleEventRequest` method is used it will make the event payload avaliable within CFML by doing:

	getLambdaContext().getEventPayload()

The event payload will typically be a JSON string that you can then parse and work with. You can test generating events with `sam local generate-event` for example:

	sam local generate-event s3 put > /tmp/test-event.json

Now you can use sam local to invoke the event for testing: 

	sam local invoke FuselessExampleEvent --event /tmp/test-event.json 

## Support, Questions, Issues

[Contact Foundeo Inc.](https://foundeo.com/consulting/coldfusion/) 