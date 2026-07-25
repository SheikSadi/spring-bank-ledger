package com.paypay.learn.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LedgerApiApplication {

	static void main(String[] args) {
		SpringApplication.run(LedgerApiApplication.class, args);
	}

}
