# Minor Entity Identification Frontend

This is a Scala/Play frontend to allow Minor Entities to provide their information to HMRC.

### How to run the service
1. Make sure any dependent services are running using the following service-manager command `sm --start MINOR_ENTITY_IDENTIFICATION_ALL -r`
2. Stop the frontend in service manager using `sm --stop MINOR_ENTITY_IDENTIFICATION_FRONTEND`
3. Run the frontend locally using
   `sbt 'run 9725 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

## Testing

---
See [TestREADME](TestREADME.md) for more information about test data and endpoints

## End-Points

### POST /minor-entity-identification/api/overseas-company-journey

---
Creates a new journey for an Overseas Company, storing the journeyConfig against the journeyId.
#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.

All other fields must be provided.

```
{
    "continueUrl" : "/test",
    "optServiceName" : "Service Name",
    "deskProServiceId" : "abc",
    "signOutUrl" : "/sign-out",
    "accessibilityUrl": "/accessibility"
}
```

### POST /minor-entity-identification/api/trusts-journey

---
Creates a new journey for a Trust, storing the journeyConfig against the journeyId.
#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.

All other fields must be provided.

```
{
    "continueUrl" : "/test",
    "optServiceName" : "Service Name",
    "deskProServiceId" : "abc",
    "signOutUrl" : "/sign-out",
    "accessibilityUrl": "/accessibility"
}
```
### GET /minor-entity-identification/api/journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.
#### Request:
A valid journeyId must be sent in the URI

#### Response:
Status:

| Expected Response                       | Reason
|-----------------------------------------|------------------------------
| ```OK(200)```                           |  ```JourneyId exists```
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```

Example response bodies:

---
Overseas Company:
```
{
    "sautr": "0000030000",
    "identifiersMatch": false,
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED",
    }
}
```
Trust:
```
{
    "identifiersMatch": false,
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED",
    }
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").