package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.TransferMoneyException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private TransferService transferService;

	private NotificationService notificationService;

	@Before
	public void init() {
		accountsService.getAccountsRepository().clearAccounts();
		notificationService = Mockito.mock(NotificationService.class);
		transferService.setNotificationService(notificationService);

		Account account = new Account("Id-1");
		account.setBalance(new BigDecimal(1000));
		accountsService.createAccount(account);

		account = new Account("Id-2");
		account.setBalance(new BigDecimal(800));
		accountsService.createAccount(account);
	}

	@Test
	public void transferMoney() throws TransferMoneyException {
		transferService.transferMoney(new Transfer("Id-1", "Id-2", BigDecimal.valueOf(600)));
		Collection<Invocation> invocations = Mockito.mockingDetails(notificationService).getInvocations();
		assertThat(accountsService.getAccount("Id-1").getBalance().equals(BigDecimal.valueOf(400)));
		assertThat(accountsService.getAccount("Id-2").getBalance().equals(BigDecimal.valueOf(1400)));
		assertThat(invocations.size() == 2);
	}

	@Test
	public void notSufficientAmount() throws TransferMoneyException {
		try {
			transferService.transferMoney(new Transfer("Id-1", "Id-2", BigDecimal.valueOf(1100)));
			fail("Should have failed because of not sufficient amount");
		} catch (TransferMoneyException ex) {
		}
		assertThat(accountsService.getAccount("Id-1").getBalance().equals(BigDecimal.valueOf(1000)));
		assertThat(accountsService.getAccount("Id-2").getBalance().equals(BigDecimal.valueOf(800)));
	}

	@Test
	public void cannotNotify() throws TransferMoneyException {
		try {
			Mockito.doThrow(new IllegalArgumentException()).when(notificationService)
					.notifyAboutTransfer(Matchers.anyObject(), Matchers.anyString());
			transferService.transferMoney(new Transfer("Id-1", "Id-2", BigDecimal.valueOf(600)));
			fail("Should have failed because of failed notification service.");
		} catch (Exception ex) {
		}
		assertThat(accountsService.getAccount("Id-1").getBalance().equals(BigDecimal.valueOf(1000)));
		assertThat(accountsService.getAccount("Id-2").getBalance().equals(BigDecimal.valueOf(800)));
	}
	
	@Test
	public void badId() throws TransferMoneyException {
		try {
			transferService.transferMoney(new Transfer("Id-3", "Id-2", BigDecimal.valueOf(-600)));
			fail("Invalid Ids.");
		} catch (Exception ex) {
		}
		assertThat(accountsService.getAccount("Id-1").getBalance().equals(BigDecimal.valueOf(1000)));
		assertThat(accountsService.getAccount("Id-2").getBalance().equals(BigDecimal.valueOf(800)));
	}

}
