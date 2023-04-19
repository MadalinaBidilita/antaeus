> :warning: This repository was archived automatically since no ownership was defined :warning:
>
> For details on how to claim stewardship of this repository see:
>
> [How to configure a service in OpsLevel](https://www.notion.so/pleo/How-to-configure-a-service-in-OpsLevel-f6483fcb4fdd4dcc9fc32b7dfe14c262)
>
> To learn more about the automatic process for stewardship which archived this repository see:
>
> [Automatic process for stewardship](https://www.notion.so/pleo/Automatic-process-for-stewardship-43d9def9bc9a4010aba27144ef31e0f2)

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!

------

### My thought process

Here is a list of questions I had and the assumptions I made to come up with a solution:

**Q: When are the invoices created?**

A: I'll assume a separate service / recurring job creates them. Therefore, we could have the following:
 - one scheduled job that creates `PENDING` invoices with the desired due date (1st day of the month in this case)
 - one daily scheduled job that charges all `PENDING` invoices with due_date <= execution_date.

**Q: What is the best time to start the billing scheduler? Should the invoices be charged on the 1st of the month in the client's time zone or in the server's timezone?**

A: I'll assume client timezone here. If I were a customer, I would expect the payment to be charged precisely on the first day and not on last day of previous month.
If pleo operates in Europe there only 3 time zones available
- there is time overlap between 1:00 - 22:00 (Europe/Berlin)
- a scheduled job could start charging the invoices at 2:00 a.m

**Q: What should happen if the balance is insufficient to cover the invoice?**

A: It would be nice to attempt again, let's say 2 more times, on 2nd and 3rd day of the month, notifying the customer. 
If still not successful, the invoice could be marked as OVERDUE and handled with a separate process.

**Q: What should happen if payments fail because of `NetworkException` ?**

A: Payment provider might be down, case eligible for retry.

**Q: What should happen if payments fail because of `CurrencyMismatchException` or `CustomerNotFoundException` ?**
A: These cases could be logged in a history for manual investigation.

### Covered
- Implemented the BillingScheduler and BillingService, assuming invoice are created in advance by a different process, 
with the 1st day of the month as due_date
- Keep a history of all transactions, both successful or not
- If the payment providers fails due to low balance, the charging is attempted 2 more times.

### Things to improve
- Introduce retry mechanism in case of `NetworkException`. 
- If the service is running in multiple instances, the scheduler should:
   - ensure it runs only on one instance Ex: https://www.springcloud.io/post/2022-07/shedlock/#gsc.tab=0
   - or deploy it separately and call an REST API endpoint