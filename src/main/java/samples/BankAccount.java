package samples;

import java.util.Date;
import java.util.List;
import samples.BankAccount.BankTransaction.TransactionType;

public record BankAccount(String accountNumber, List<BankTransaction> transactions) {
    public record BankTransaction(Date date, double amount, TransactionType type){
        public enum TransactionType{
            Depost,
            Withdraw
        }
    }
    public BankAccount onDeposit(BankAccountEvent.Deposit deposit)
    {
        this.transactions.add(new BankAccount.BankTransaction(deposit.date(), deposit.amount(), TransactionType.Depost));
        return new BankAccount(deposit.accountNumber(), this.transactions);
    }
    public BankAccount onWithdraw(BankAccountEvent.Withdraw withdraw)
    {
        this.transactions.add(new BankAccount.BankTransaction(withdraw.date(), withdraw.amount(), TransactionType.Withdraw));
        return new BankAccount(withdraw.accountNumber(), this.transactions);
    }
}

