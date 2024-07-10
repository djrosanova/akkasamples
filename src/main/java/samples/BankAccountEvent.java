package samples;

import java.util.Date;
import kalix.javasdk.annotations.TypeName;
public sealed interface BankAccountEvent {
    @TypeName("deposit") 
    record Deposit(String accountNumber, Date date, double amount) implements BankAccountEvent {}
    
    @TypeName("withdraw") 
    record Withdraw(String accountNumber, Date date, double amount) implements BankAccountEvent {}
}
