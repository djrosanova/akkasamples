# akkasamples
Samples for Akka

# banking
The banking sample demonstrates the basics of event sourced entities in Kalix

There is a PoJo that models a BankAccount. There is a BankAccountEntity which provides deposit, withdraw, balance, and GET operations and is what makes this event sourced. The operations do what the name says. The GET operation returns all the transactions (depoistis and withdrawls). Finally there is a BankAccountEvent to tie the other two together in an event driven manner.

To show the interaction between entity instances there is a TransferWorkflow which transfers funds from one account to another. It uses a TransferState PoJo for state. It also has a very simple Message class used for communication within the workflow. 

When you deploy this to Kalix all state in this sample is stored by Kalix for you. This is durable and stateful, but you never need to write to a database or storage service. When you run this locally the persistience is not enabled in this sample. Doing enabling it doens't require code changes, just deployment changes. 

To understand the Kalix concepts that are the basis for this example, see [Designing services](https://docs.kalix.io/java/development-process.html) in the documentation. 

To understand more about Event Sourced Entities see [Event source entitites](https://docs.kalix.io/java/event-sourced-entities.html)

To understand more about Workflows see [Workflows](https://docs.kalix.io/java/workflows.html)

This project contains the framework to create a Kalix service. To understand more about these components, see [Developing services](https://docs.kalix.io/services/) and check Spring-SDK [official documentation](https://docs.kalix.io/spring/index.html). Examples can be found [here](https://github.com/lightbend/kalix-jvm-sdk/tree/main/samples) in the folders with "spring" in their name.


Use Maven to build your project:

```shell
mvn compile
```


When running a Kalix service locally, we need to have its companion Kalix Runtime running alongside it.

To start your service locally, run:

```shell
mvn kalix:runAll
```

This command will start your Kalix service and a companion Kalix Runtime as configured in [docker-compose.yml](./docker-compose.yml) file.

With both the Kalix Runtime and your service running, once you have defined endpoints they should be available at `http://localhost:9000`.

Now you can test the application out with some commands

See the balance account 1000
`~curl -H "Content-Type: application/json" localhost:9000/bankAccount/1000`

Deposit 50 into account 1000
`curl -XPOST -H "Content-Type: application/json" localhost:9000/bankAccount/1234/deposit -d '{ "accountNumber": "1000", "date": "2024-06-26", "amount": 50 }'`

See the balance account 1000 after the deposit
`curl -H "Content-Type: application/json" localhost:9000/bankAccount/1000`

Withdraw 25 from account 1000
`curl -XPOST -H "Content-Type: application/json" localhost:9000/bankAccount/1234/withdraw -d '{ "accountNumber": "1234", "date": "2024-06-26", "amount": 25 }'`

Transfer 10 from account 1000 to account 1001
`curl -X PUT -H "Content-Type: application/json" localhost:9000/transfer/15 -d '{"fromAccount": "1000", "toAccount": "1001", "amount": 10}'`

See the balance account 1000 after the transfer
`curl -H "Content-Type: application/json" localhost:9000/bankAccount/1000`

See the balance account 1001 after the transfer
`curl -H "Content-Type: application/json" localhost:9000/bankAccount/1001`

To deploy your service to the cloud, install the `kalix` CLI as documented in
[Install Kalix](https://docs.kalix.io/kalix/install-kalix.html)
and configure a Docker Registry to upload your docker image to.

You will need to update the `dockerImage` property in the `pom.xml` and refer to
[Configuring registries](https://docs.kalix.io/projects/container-registries.html)
for more information on how to make your docker image available to Kalix.

Finally, you can use the [Kalix Console](https://console.kalix.io)
to create a project and then deploy your service into the project either by using `mvn deploy kalix:deploy` which
will conveniently package, publish your docker image, and deploy your service to Kalix, or by first packaging and
publishing the docker image through `mvn deploy` and then deploying the image
through the `kalix` CLI.
