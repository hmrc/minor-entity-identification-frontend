# Minor Entity Identification Frontend

This is a Scala/Play frontend to allow Minor Entities to provide their information to HMRC.

### How to run the service

1. Make sure any dependent services are running using the following service-manager
   command `sm --start MINOR_ENTITY_IDENTIFICATION_ALL -r`
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

The businessVerificationCheck field allows the calling service to bypass the business verification and continue to
register where a successful match is found. The field will default to true if it is not provided.

All other fields must be provided.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out to Business
Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

```
{
    "accessibilityUrl": "/accessibility",
    "businessVerificationCheck" : true,
    "continueUrl" : "/test",
    "deskProServiceId" : "abc",
    "optServiceName" : "Service Name",
    "regime" : "VATC",    
    "signOutUrl" : "/sign-out"
}
```

### POST /minor-entity-identification/api/trusts-journey

---
Creates a new journey for a Trust, storing the journeyConfig against the journeyId.

#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.

The businessVerificationCheck field allows the calling service to bypass the business verification and continue to
register where a successful match is found. The field will default to true if it is not provided.

All other fields must be provided.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out to Business
Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

```
{
    "accessibilityUrl": "/accessibility",
    "businessVerificationCheck" : true,
    "continueUrl" : "/test",
    "deskProServiceId" : "abc",
    "optServiceName" : "Service Name",
    "regime" : "VATC",    
    "signOutUrl" : "/sign-out"
}
```

### POST /minor-entity-identification/api/unincorporated-association-journey

---
Creates a new journey for an Unincorporated Association, storing the journeyConfig against the journeyId.

#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.

The businessVerificationCheck field allows the calling service to bypass the business verification and continue to
register where a successful match is found. The field will default to true if it is not provided.

All other fields must be provided.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out to Business
Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

```
{
    "accessibilityUrl": "/accessibility",
    "businessVerificationCheck" : true,
    "continueUrl" : "/test",
    "deskProServiceId" : "abc",
    "optServiceName" : "Service Name",
    "regime" : "VATC"    
    "signOutUrl" : "/sign-out"
}
```

### GET /minor-entity-identification/api/journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.

#### Request:

A valid journeyId must be sent in the URI

#### Response:

Status:

| Expected Response                       | Reason                          |
|-----------------------------------------|---------------------------------|
| ```OK(200)```                           | ```JourneyId exists```          |
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```   |

Example response bodies:

---
Overseas Company:

```
{
    "sautr": "0000030000",
    "identifiersMatch": false,
    "overseas": { 
      "taxIdentifier": "134124532", 
      "country": "AL" 
    } 
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED"
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
        "registrationStatus":"REGISTRATION_NOT_CALLED"
    }
}
```

Trust with full flow enabled:

```
{
    "sautr": "0000030000",
    "saPostcode": "AA1 1AA",
    "identifiersMatch": true,
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED"
    }
}
```

Unincorporated Association:

```
{
    "identifiersMatch": false,
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED"
    }
}
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").