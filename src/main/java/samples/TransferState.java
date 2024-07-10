package samples;

public record TransferState(String transferId, Transfer transfer, TransferStatus status) {

  public record Transfer(String fromAccount, String toAccount, int amount) {
  }

  public enum TransferStatus {
    STARTED, WITHDRAW_FAILED, WITHDRAW_SUCCEED, DEPOSIT_FAILED, COMPLETED, COMPENSATION_COMPLETED, WAITING_FOR_ACCEPTATION, TRANSFER_ACCEPTATION_TIMED_OUT, REQUIRES_MANUAL_INTERVENTION
  }

  public TransferState(String transferId, Transfer transfer) {
    this(transferId, transfer, TransferStatus.STARTED);
  }

  public TransferState withStatus(TransferStatus newStatus) {
    return new TransferState(transferId, transfer, newStatus);
  }
}
