package samples;

import samples.TransferState.Transfer;
import samples.BankAccount.BankTransaction.TransactionType;
import kalix.javasdk.client.ComponentClient;
import kalix.javasdk.workflow.Workflow;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Date;

import static samples.TransferState.TransferStatus.*;


// tag::class[]
@TypeId("transfer") 
@Id("transferId") 
@RequestMapping("/transfer/{transferId}") 
public class TransferWorkflow extends Workflow<TransferState> { 
  // end::class[]

  // tag::start[]
  public record TransferWithdraw(String fromAccount, int amount) { 
  }

  // end::start[]

  // tag::definition[]
  public record TransferDeposit(String toAccount, int amount) {
  }

  // end::definition[]

  private static final Logger logger = LoggerFactory.getLogger(TransferWorkflow.class);

  final private ComponentClient componentClient;

  public TransferWorkflow(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  // tag::definition[]
  @Override
  public WorkflowDef<TransferState> definition() {
    Step withdraw =
      step("withdraw") 
        .call(TransferWithdraw.class, cmd -> {
          return componentClient.forEventSourcedEntity(cmd.fromAccount)
            .call(BankAccountEntity::Withdraw)
            .params(new BankAccount.BankTransaction(new Date(), currentState().transfer().amount(), TransactionType.Withdraw));
        }) 
        .andThen(String.class, __ -> {
          TransferDeposit depositInput = new TransferDeposit(currentState().transfer().toAccount(), currentState().transfer().amount());
          return effects()
            .updateState(currentState().withStatus(WITHDRAW_SUCCEED))
            .transitionTo("deposit", depositInput); // <3>
        });

    Step deposit =
      step("deposit") 
        .call(TransferDeposit.class, cmd -> {
          return componentClient.forEventSourcedEntity(cmd.toAccount)
            .call(BankAccountEntity::Deposit)
            .params(new BankAccount.BankTransaction(new Date(), currentState().transfer().amount(), TransactionType.Depost));
        }) 
        .andThen(String.class, __ -> {
          return effects()
            .updateState(currentState().withStatus(COMPLETED))
            .end(); 
        });

    return workflow() // <6>
      .addStep(withdraw)
      .addStep(deposit);
  }
  // end::definition[]

  // tag::start[]
  @PutMapping
  public Effect<Message> startTransfer(@RequestBody Transfer transfer) {
    if (currentState() != null) {
      return effects().error("transfer already started");
    } else if (transfer.amount() <= 0) {
      return effects().error("transfer amount should be greater than zero");
    } else {
      logger.info("Running: " + transfer);
      TransferState initialState = new TransferState(commandContext().workflowId(), transfer);
      TransferWithdraw withdrawInput = new TransferWithdraw(transfer.fromAccount(), transfer.amount());
      return effects()
        .updateState(initialState)
        .transitionTo("withdraw", withdrawInput)
        .thenReply(new Message("transfer started"));
    }
  }
  // end::start[]

  // tag::get-transfer[]
  @GetMapping // <1>
  public Effect<TransferState> getTransferState() {
    if (currentState() == null) {
      return effects().error("transfer not started");
    } else {
      return effects().reply(currentState()); // <2>
    }
  }
  // end::get-transfer[]

}
