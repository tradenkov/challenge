package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransfersControllerTest {
	private MockMvc mockMvc;
	@Autowired
	private AccountsService accountsService;
	@Autowired
	private TransferService transferService;
	@Autowired
	private WebApplicationContext webApplicationContext;
	private NotificationService notificationService;

	@Before
	public void prepareMockMvc() throws Exception {
		mockMvc = webAppContextSetup(webApplicationContext).build();
		notificationService = Mockito.mock(NotificationService.class);
		transferService.setNotificationService(notificationService);
		
		// Clear Accounts
		accountsService.getAccountsRepository().clearAccounts();
		// Fill Initial Accounts
		Account account = new Account("Id-1");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		account = new Account("Id-2");
		account.setBalance(new BigDecimal(800));
		this.accountsService.createAccount(account);
	}

	@Test
	public void simpleTransfer() throws Exception {
		this.mockMvc
				.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-1\",\"toAccountId\":\"Id-2\",\"amount\":600}"))
				.andExpect(status().isCreated());
		Collection<Invocation> invocations = Mockito.mockingDetails(notificationService).getInvocations();
		assertThat(accountsService.getAccount("Id-1").getBalance().equals(BigDecimal.valueOf(400)));
		assertThat(accountsService.getAccount("Id-2").getBalance().equals(BigDecimal.valueOf(1400)));
		assertThat(invocations.size() == 2);
	}
	
	@Test
	public void badIdTransfer() throws Exception {
		this.mockMvc
				.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-3\",\"toAccountId\":\"Id-2\",\"amount\":600}"))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void notPositiveAmount() throws Exception {
		this.mockMvc
				.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-1\",\"toAccountId\":\"Id-2\",\"amount\":-600}"))
				.andExpect(status().isBadRequest());
	}

}
