package com.db.awmd.challenge.web;

import java.text.MessageFormat;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.TransferMoneyException;
import com.db.awmd.challenge.service.TransferService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
public class TransfersController {

	private final TransferService transferService;

	@Autowired
	public TransfersController(TransferService transferService) {
		this.transferService = transferService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> transferMoney(@RequestBody @Valid Transfer transfer) {
		log.info(MessageFormat.format("{0} is sending {1} euros to {2}", transfer.getFromAccountId(), transfer.getAmount(),
				transfer.getToAccountId()));
		try {
			this.transferService.transferMoney(transfer);
		} catch (TransferMoneyException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

}
