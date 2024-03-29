# Minor Entity Identification Frontend Test End-Points

## Testing

---

1. [Feature Switches](TestREADME.md#get-test-onlyfeature-switches)
2. [Setting up an Overseas Company Journey](TestREADME.md#get-identify-your-overseas-businesstest-onlycreate-overseas-company-journey)
3. [Setting up a Trust Journey](TestREADME.md#get-identify-your-trusttest-onlycreate-trusts-journey)
4. [Setting up a Unincorporated Association Journey](TestREADME.md#get-identify-your-unincorporated-associationtest-onlycreate-unincorporated-association-journey)
5. [Retrieving Journey Data](TestREADME.md#retrieve-journey-data)
6. [Using the Trust Known Facts Stub](TestREADME.md#using-the-trusts-known-facts-stub)

### GET test-only/feature-switches

---

Accessible with any of the 3 base urls:
- /identify-your-trust/test-only/feature-switches
- /identify-your-overseas-business/test-only/feature-switches
- /identify-your-unincorporated-association/test-only/feature-switches

Shows all feature switches:
1. Minor Entity Identification Frontend

   - Enable full trust journey (this is set to true in production)
     - Enables the full trust journey, without this there are no question pages and the user is redirected back to the calling service
   - Use stub for Trusts further verification flow (see [here](TestREADME.md#using-the-trusts-known-facts-stub))
   - Use stub for Business Verification flow
     - Bypasses the whole Business Verification flow. Always returns BusinessVerification Status as PASS
   - Enable full Unincorporated association journey (this is set to true in production)
     - Enables the full UA journey, without this there are no question pages and the user is redirected back to the calling service

2. Minor Entity Identification (see Minor Entity Identification TestREADME for more info)

   - Use stub for submissions to Registration
   - Use stub for Get CT Reference

### GET /identify-your-overseas-business/test-only/create-overseas-company-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for an
Overseas Company.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out
to Business Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

1. Continue URL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

    - Used for the `Beta feedback` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Business verification checkbox

    - Used for skipping further verification checks carried out currently by Business Verification (SI)
    - This is currently autofilled but can be changed

6. Regime (Required)

    - This is the Tax Regime Identifier
    - It is passed down to the Registration API
    - This is currently defaulted to VATC but accepted values are PPT or VATC

7. Accessibility Statement Link (Required)

    - Shown in the HMRC footer
    - This is currently autofilled but can be changed

8. Welsh translation for Service Name (Optional)

   - Welsh language translation for service name

### GET /identify-your-trust/test-only/create-trusts-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for a
Trust.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out
to Business Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

1. Continue URL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

    - Used for the `Beta feedback` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Business verification checkbox

    - Used for skipping further verification checks carried out currently by Business Verification (SI)
    - This is currently autofilled but can be changed

6. Regime (Required)

    - This is the Tax Regime Identifier
    - It is passed down to the Registration API
    - This is currently defaulted to VATC but accepted values are PPT or VATC

7. Accessibility Statement Link (Required)

    - Shown in the HMRC footer
    - This is currently autofilled but can be changed

8. Welsh translation for Service Name (Optional)

   - Welsh language translation for service name

### GET /identify-your-unincorporated-association/test-only/create-unincorporated-association-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for an
Unincorporated Association.

All URLs provided must be relative, apart from locally where localhost is allowed. If you need to call out
to Business Verification (rather than stub it) all non-relative urls will cause the handover to Business Verification to fail.

1. Continue URL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

    - Used for the `Beta feedback` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Business verification checkbox

    - Used for skipping further verification checks carried out currently by Business Verification (SI)
    - This is currently autofilled but can be changed

6. Regime (Required)

    - This is the Tax Regime Identifier
    - It is passed down to the Registration API
    - This is currently defaulted to VATC but accepted values are PPT or VATC

7. Accessibility Statement URL (Required)

    - Shown in the footer - a link to the accessibility statement for the calling service
    - This is currently autofilled but can be changed

8. Welsh translation for Service Name (Optional)

   - Welsh language translation for service name

### Retrieve journey Data

---

#### Retrieve journey data from an Overseas Company journey:

- GET /identify-your-overseas-business/test-only/retrieve-journey/:journeyId or
- GET /identify-your-overseas-business/test-only/retrieve-journey

#### Retrieve journey data from a Trust journey:

- GET /identify-your-trust/test-only/retrieve-journey/:journeyId or
- GET /identify-your-trust/test-only/retrieve-journey

#### Retrieve journey data from an Unincorporated Association journey:

- GET /identify-your-unincorporated-association/test-only/retrieve-journey/:journeyId or
- GET /identify-your-unincorporated-association/test-only/retrieve-journey

---
Retrieves all the journey data that is stored against a specific journeyID.

#### Request:

A valid journeyId must be sent in the URI or as a query parameter. Example of using the query parameter:

`test-only/retrieve-journey?journeyId=1234567`

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
        "registrationStatus":"REGISTRATION_NOT_CALLED",
    }
}
```

Trust:

```
{
    "identifiersMatch": "false",
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED",
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
        "registrationStatus":"REGISTRATION_NOT_CALLED",
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
    "identifiersMatch": "false",
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED",
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

### Using the Trusts Known Facts stub

GET /identify-your-trust/test-only/trusts/:sautr/refresh

___

Stubs the call to the Trusts Microservice to retrieve the known facts associated with the sautr provided.

The Use stub for Trusts further verification flow feature switch will need to be enabled to use this stub.

#### Request:

No body is required for this request but a sautr must be sent in the url

#### Response:

| Sautr                  | Response        | Postcode returned  | Is Abroad       |
|------------------------|-----------------|--------------------|-----------------|
| ```1234567891```       | ```Ok```        | ```None```         | ```True```      |
| ```1234567892```       | ```Not Found``` | ```-```            | ```-```         |
| ```Any other sautr```  | ```Ok```        | ```AA1 1AA```      | ```False```     |

