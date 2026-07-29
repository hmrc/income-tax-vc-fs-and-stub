
# income-tax-vc-fs-and-stub

This is a service to override feature switches and stubs the APIs called by View and Change services.


## Feature Switches

Feature switches are available in staging here:
https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/view/test-only/feature-switch

The new feature switches are used to switch on the new microservices created from the main View and Change journey.

When enabled, the journey routes requests to the newly developed services instead of the existing View and Change integrations.

### Switches:

#### Business Details Frontend Switch

Causes the view and change journey to route requests to the new Business Details service instead of the existing View and Change integration.

#### Charge History Switch

Shows the adjustment history section on the Charge Summary Page

#### Credits/Refunds Repayments

Shows Credits/Refunds and Repayments section on the Charge Summary Page

#### Display Business Start Date

Shows the business start date on the Business Details/ Income Source Page

#### Financials Frontend

Causes the view and change journey to route requests to the new Financials service instead of the existing View and Change integration.

#### Idempotency Key for Create an Income Source

Ensures create-income-source requests are idempotent by attaching an idempotency key, so retries do not create duplicate income sources.

#### ITSA Submission Integration

Changes return-submission flow selection by routing users/calls to the integrated ITSA submission path where enabled.

#### Mortgage evidence

Enables the Proof of Your Income page and shows a mortgage evidence link on the Tax Years page.

#### No Income Sources Redirect

If the user has no income sources, they are redirected to NoIncomeSourcesController

#### Obligations Frontend

Causes the view and change journey to route requests to the new Obligations service instead of the existing View and Change integration.

#### Opt Out

Enables the opt-out journey, allowing users to access and complete the MTD opt-out flow where the feature is turned on.

#### Overseas Business Address

Enables the international (non-UK) business address journey when adding/changing a business address.

#### Payment History Refunds

Enables refund-related functionality within the Payment History journey

#### Penalties and Appeals

Enables penalties-and-appeals related journey elements (especially tasks for overdue penalties) and navigation to penalty views

#### Penalties Backend

Causes the view and change journey to route requests to the new Penalties service instead of the existing View and Change integration.

#### Post Finalisation Amendments R18

Enables the post-finalisation amendments journey, allowing users to make amendments to their tax return after it has been finalised.

#### Recent Activity

Enables the recent activity section on the Home page, showing users a summary of their recent tax-related activities.

#### Returns Frontend

Causes the view and change journey to route requests to the new Returns service instead of the existing View and Change integration.

#### Revenue Amendments

Seems to be unused at the moment.

#### Self Serve Time To Pay R17

Enables the Self Serve Time To Pay journey from View and Change, allowing users to start the Time To Pay flow when the feature is turned on.

#### Sign Up

Enables the sign-up/opt-in journey, allowing users to access and complete the MTD quarterly reporting sign-up flow where the feature is turned on.

#### Submit Claim to Adjust to NRS

Causes the Claim to Adjust POA submission journey to publish submission evidence to NRS, creating a non-repudiation audit record for submitted claims.

#### Triggered Migration

Enables the Triggered Migration journey, requiring eligible users to migrate to MTD as part of their View and Change flow.

#### Read From Mongo
Controls whether stubbed API responses are served from MongoDB-backed data rather than static default responses.

When enabled, the service looks up a matching request in MongoDB and returns the stored status and response body, if no matching record is found, the service falls back to its default stub response.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").