package com.shadrachjabonir;

import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableAutoConfiguration
@RestController
public class DemoPivotalBankserviceApplication {

	@Autowired
	BankDbClient bankDbClient;

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/getAccountDetail", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	Account getAccountDetail(@RequestBody Account account){
		return bankDbClient.getAccount(account.getNumber());
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/updateAccountBalance", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	Account updateAccountBalance(@RequestBody Account account){
		Account willBeUpdated = bankDbClient.getAccount(account.getNumber());
		willBeUpdated.setAmount(account.getAmount());
		System.out.println(willBeUpdated.getAmount());
		willBeUpdated = bankDbClient.updateAccount(willBeUpdated.getId(),willBeUpdated);
		return willBeUpdated;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoPivotalBankserviceApplication.class, args);
	}
}

@FeignClient(value = "bankDb", url = "https://demo-pivotal-bankdb.cfapps.io/")
interface BankDbClient {

	@RequestMapping(method = RequestMethod.GET, value = "/account/search/findByNumber?number={number}")
	Account getAccount(@PathVariable("number") String number);

	@RequestMapping(method = RequestMethod.PUT, value = "/account/{accountId}")
	Account updateAccount(@PathVariable("accountId") long accountId, Account account);
}

class Account implements Serializable {

	private Long id;
	private String name;
	private String number;
	private Double amount;

	public Account() {
	}

	public Account(String name, String number, Double amount) {
		this.name = name;
		this.number = number;
		this.amount = amount;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}