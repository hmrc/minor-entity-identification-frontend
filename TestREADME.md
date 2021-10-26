# Minor Entity Identification Frontend Test End-Points

## Testing

---

1. [Setting up an Overseas Company Journey](TestREADME.md#get-identify-your-overseas-businesstest-onlycreate-overseas-company-journey)
2. [Setting up a Trust Journey](TestREADME.md#get-identify-your-trusttest-onlycreate-trusts-journey)
3. [Retrieving Journey Data](TestREADME.md#get-minor-entity-identificationtest-onlyretrieve-journeyjourneyid-or-minor-entity-identificationtest-onlyretrieve-journey)


### GET /identify-your-overseas-business/test-only/create-overseas-company-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for an Overseas Company.

1. Continue URL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

    - Used for the `Get help with this page` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

4. Accessibility Statement Link (Required)

    - Shown in the HMRC footer
    - This is currently autofilled but can be changed

### GET /identify-your-trust/test-only/create-trusts-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for a Trust.

1. Continue URL (Required)

   - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

   - Service Name to use throughout the service
   - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

   - Used for the `Get help with this page` link
   - This is currently autofilled but can be changed

4. Sign Out Link (Required)

   - Shown in the HMRC header - typically a link to a feedback questionnaire
   - This is currently autofilled but can be changed

4. Accessibility Statement Link (Required)

   - Shown in the HMRC footer
   - This is currently autofilled but can be changed


### GET /minor-entity-identification/test-only/retrieve-journey/:journeyId or /minor-entity-identification/test-only/retrieve-journey

---
Retrieves all the journey data that is stored against a specific journeyID.

#### Request:
A valid journeyId must be sent in the URI or as a query parameter. Example of using the query parameter:

`test-only/retrieve-journey?journeyId=1234567`

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
    "identifiersMatch": "false",
    "businessVerification": {
      "verificationStatus":"UNCHALLENGED"
    },
    "registration": {
        "registrationStatus":"REGISTRATION_NOT_CALLED",
    }
}
```
