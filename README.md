# Minor Entity Identification Frontend

This is a Scala/Play frontend to allow Minor Entities to provide their information to HMRC.

### How to run the service

1. Make sure any dependent services are running using the following service-manager
   command `sm2 --start MINOR_ENTITY_IDENTIFICATION_ALL`
2. Stop the frontend in service manager using `sm2 --stop MINOR_ENTITY_IDENTIFICATION_FRONTEND`
3. Run the frontend locally using
   `sbt 'run 9725 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

### How to run a journey 

#### Authority Wizard

1. Navigate to Auth Login Stub http://localhost:9949/auth-login-stub/gg-sign-in
2. Populate the 'Redirect URL' field with the appropriate test journey creation URL. An example for creating an overseas 
   company journey would be:
   http://localhost:9725/identify-your-overseas-business/test-only/create-overseas-company-journey
3. Click on 'Submit'  

#### Journey Config

1. Enter the following values within the 'Enter Minor Entity Identification Journey Config' page:
   - Continue URL:  `/test`
   - Service Name:  `Test Service Name`
2. Click on 'Submit'

## Testing

---
See [TestREADME](TestREADME.md) for more information about test data and endpoints

## End-Points

### POST /minor-entity-identification/api/overseas-company-journey

---
Creates a new journey for an Overseas Company, storing the journeyConfig against the journeyId.

#### Request:

The property labels enables the calling service to define the service name in both english and welsh. If the property is not defined, the
service name in english will default to the value defined by
the property optServiceName and the service name in welsh will default to the 
service default. If "optServiceName" is not defined the service name in english will be the service default
`Entity Validation Service`. The above behaviour will apply also if the property is
present, but one or both of the nested properties "en" and "cy" are not
defined.

The businessVerificationCheck field allows the calling service to bypass the business verification and continue to
register where a successful match is found. The field will default to true if it is not provided.

All other fields must be provided.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out to Business
Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

The Deskpro service identifier is used to set the service name in the beta feedback url.

```
{
    "accessibilityUrl": "/accessibility",
    "businessVerificationCheck" : true,
    "continueUrl" : "/test",
    "deskProServiceId" : "abc",
    "optServiceName" : "Service Name", // deprecated, use labels.en.optServiceName
    "regime" : "VATC",    
    "signOutUrl" : "/sign-out",
    "labels" : {
      "en" : {
        "optServiceName" : "Service name in english"
      },
      "cy" : {
         "optServiceName" : "Service name in welsh"
      }
    }
}
```

### POST /minor-entity-identification/api/trusts-journey

---
Creates a new journey for a Trust, storing the journeyConfig against the journeyId.

#### Request:

The property labels enables the calling service to define the service name in both english and welsh. If the property is not defined, the
service name in english will default to the value defined by
the property optServiceName and the service name in welsh will default to the
service default. If "optServiceName" is not defined the service name in english will be the service default
`Entity Validation Service`. The above behaviour will apply also if the property is
present, but one or both of the nested properties "en" and "cy" are not
defined.

The businessVerificationCheck field allows the calling service to bypass the business verification and continue to
register where a successful match is found. The field will default to true if it is not provided.

All other fields must be provided.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out to Business
Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

The Deskpro service identifier is used to set the service name in the beta feedback url.

```
{
    "accessibilityUrl": "/accessibility",
    "businessVerificationCheck" : true,
    "continueUrl" : "/test",
    "deskProServiceId" : "abc",
    "optServiceName" : "Service Name", // deprecated, use labels.en.optServiceName
    "regime" : "VATC",    
    "signOutUrl" : "/sign-out",
    "labels" : {
      "en" : {
        "optServiceName" : "Service name in english"
      },
      "cy" : {
         "optServiceName" : "Service name in welsh"
      }
    }
}
```

### POST /minor-entity-identification/api/unincorporated-association-journey

---
Creates a new journey for an Unincorporated Association, storing the journeyConfig against the journeyId.

#### Request:

The property labels enables the calling service to define the service name in both english and welsh. If the property is not defined, the
service name in english will default to the value defined by
the property optServiceName and the service name in welsh will default to the
service default. If "optServiceName" is not defined the service name in english will be the service default
`Entity Validation Service`. The above behaviour will apply also if the property is
present, but one or both of the nested properties "en" and "cy" are not
defined.

The businessVerificationCheck field allows the calling service to bypass the business verification and continue to
register where a successful match is found. The field will default to true if it is not provided.

All other fields must be provided.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out to Business
Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

The Deskpro service identifier is used to set the service name in the beta feedback url.

```
{
    "accessibilityUrl": "/accessibility",
    "businessVerificationCheck" : true,
    "continueUrl" : "/test",
    "deskProServiceId" : "abc",
    "optServiceName" : "Service Name", // deprecated, use labels.en.optServiceName
    "regime" : "VATC"    
    "signOutUrl" : "/sign-out",
    "labels" : {
      "en" : {
        "optServiceName" : "Service name in english"
      },
      "cy" : {
         "optServiceName" : "Service name in welsh"
      }
    }
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

Trust with full flow enabled where Registration failed:

```
{
  "sautr": "0000030000",
  "saPostcode": "AA1 1AA",
  "identifiersMatch": true,
  "businessVerification": {
    "verificationStatus": "PASS"
  },
  "registration": {
    "registrationStatus": "REGISTRATION_FAILED",
    "failures": [
      {
        "code": "PARTY_TYPE_MISMATCH",
        "reason": "The remote endpoint has indicated there is Party Type mismatch"
      }
    ]
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

Unincorporated Association where Registration failed:

```
{
  "ctutr": "1234229999",
  "ctPostcode": "AA1 1AA",
  "identifiersMatch": true,
  "businessVerification": {
    "verificationStatus": "PASS"
  },
  "registration": {
    "registrationStatus": "REGISTRATION_FAILED",
    "failures": [
      {
        "code": "PARTY_TYPE_MISMATCH",
        "reason": "The remote endpoint has indicated there is Party Type mismatch"
      }
    ]
  }
}
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
