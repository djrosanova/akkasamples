package samples;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.*;
import samples.BankAccount.BankTransaction.TransactionType;
import kalix.javasdk.annotations.*;
import kalix.javasdk.eventsourcedentity.*;
@Id("accountNumber") 
@TypeId("bankAccount") 
@RequestMapping("/bankAccount/{accountNumber}") 
public class BankAccountEntity extends EventSourcedEntity<BankAccount, BankAccountEvent> {
  final private String accountNumber;
  
  public BankAccountEntity(EventSourcedEntityContext entityContext) {
    this.accountNumber = entityContext.entityId();
  }
  //YES: I need this
  @Override
  public BankAccount emptyState() {
    return new BankAccount(accountNumber, new ArrayList<>());
  }
  
  @PostMapping("/deposit")
  public Effect<String> Deposit(@RequestBody BankAccount.BankTransaction transaction) {
      var event = new BankAccountEvent.Deposit(accountNumber, transaction.date(), transaction.amount());
      return effects()
          .emitEvent(event)
          .thenReply(newState -> "OK");
  }
  @PostMapping("/withdraw")
  public Effect<String> Withdraw(@RequestBody BankAccount.BankTransaction transaction) {
    
    if(currentBalance() < transaction.amount())
    {
      //insufficent funds
      return effects().error("Insufficient balance");
    }
    else
    {
      var event = new BankAccountEvent.Withdraw(accountNumber, transaction.date(), transaction.amount());
      return effects()
          .emitEvent(event)
          .thenReply(newState -> "OK");
    }
  }

  @GetMapping()
  public Effect<BankAccount> getAccount() {
    return effects().reply(currentState());
  }

  @GetMapping("/balance")
  public Effect<Double> getBalance() {
    return effects().reply(currentBalance()
    );
  }

  private double currentBalance() {
    return currentState().transactions().stream().filter(
      t -> t.type() == TransactionType.Depost).mapToDouble(BankAccount.BankTransaction::amount).sum() -
    currentState().transactions().stream().filter(t -> t.type() == TransactionType.Withdraw)
    .mapToDouble(BankAccount.BankTransaction::amount).sum();
  }

  @EventHandler
  public BankAccount onDeposit(BankAccountEvent.Deposit addTransaction) {
    return currentState().onDeposit(addTransaction);
  }

  @EventHandler
  public BankAccount onWithdraw(BankAccountEvent.Withdraw addTransaction) {
    return currentState().onWithdraw(addTransaction);
  }
}
