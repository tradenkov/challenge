package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.TransferMoneyException;
import lombok.Getter;
import lombok.Setter;

@Service
public class TransferService {
	@Getter
	@Setter
	@Autowired
	private AccountsService accountsService;

	@Getter
	@Setter
	private NotificationService notificationService;

	public TransferService() {
	}

	public void transferMoney(@Valid Transfer transfer) throws TransferMoneyException {
		transferMoney(transfer.getFromAccountId(), transfer.getToAccountId(), transfer.getAmount());
	}

	public void transferMoney(String fromAccountId, String toAccountId, BigDecimal amount)
			throws TransferMoneyException {
		Account fromAccount = accountsService.getAccount(fromAccountId);
		Account toAccount = accountsService.getAccount(toAccountId);
		if(fromAccount==null) {
			throw new TransferMoneyException(MessageFormat
					.format("Invalid source account {0}", fromAccountId));
		}
		if(toAccount==null) {
			throw new TransferMoneyException(MessageFormat
					.format("Invalid target account {0}", toAccountId));
		}
		Account firstLock = fromAccountId.compareTo(toAccountId) >= 0 ? fromAccount : toAccount;
		Account secondLock = fromAccountId.compareTo(toAccountId) >= 0 ? toAccount : fromAccount;
		synchronized (firstLock) {
			synchronized (secondLock) {
				if (fromAccount.getBalance().compareTo(amount) < 0) {
					throw new TransferMoneyException(MessageFormat
							.format("Account {0} balance is not sufficient to send {1} euros.", fromAccountId, amount));
				}
				BigDecimal fromInitialBalance = fromAccount.getBalance();
				BigDecimal toInitialBalance = toAccount.getBalance();
				try {
					fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
					toAccount.setBalance(toAccount.getBalance().add(amount));
					notificationService.notifyAboutTransfer(fromAccount,
							MessageFormat.format("You sent {0} euros to {1}", amount, toAccountId));
					notificationService.notifyAboutTransfer(toAccount,
							MessageFormat.format("You received {0} euros from {1}", amount, toAccountId));

				} catch (Exception e) {
					fromAccount.setBalance(fromInitialBalance);
					toAccount.setBalance(toInitialBalance);
					throw new TransferMoneyException(
							MessageFormat.format("Money transfer between {0} and {1} of {2} euros failed.",
									fromAccountId, toAccountId, amount),
							e);
				}
			}
		}
	}
}
