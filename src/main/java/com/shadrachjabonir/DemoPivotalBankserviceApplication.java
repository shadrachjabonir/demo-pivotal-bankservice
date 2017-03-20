package com.shadrachjabonir;

import com.netflix.discovery.converters.Auto;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableCircuitBreaker
@RestController
public class DemoPivotalBankserviceApplication {

//	@Value("${app.version}")
//	String version;

	@Autowired
	BankService bankService;

//	@CrossOrigin(origins = "*")
//	@RequestMapping(value = "/getVersion",method = RequestMethod.GET)
//	String getVersion(){
//		return version;
//	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/getAccountDetail", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	Account getAccountDetail(@RequestBody Account account){
		return bankService.getAccount(account.getNumber());
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/updateAccountBalance", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	Account updateAccountBalance(@RequestBody Account account){
		Account willBeUpdated = bankService.getAccount(account.getNumber());
		willBeUpdated.setAmount(account.getAmount());
		System.out.println(willBeUpdated.getAmount());
		willBeUpdated = bankService.updateAccount(willBeUpdated.getId(),willBeUpdated);
		return willBeUpdated;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoPivotalBankserviceApplication.class, args);
	}
}

@FeignClient(value = "bankDb", url = "https://demo-pivotal-bankdb.cfapps.io")
interface BankDbClient {

	@RequestMapping(method = RequestMethod.GET, value = "/account/search/findByNumber?number={number}")
	Account getAccount(@PathVariable("number") String number);

	@HystrixCommand(fallbackMethod = "updateAccountFailed")
	@RequestMapping(method = RequestMethod.PUT, value = "/account/{accountId}")
	Account updateAccount(@PathVariable("accountId") long accountId, Account account);
}

@Service
class BankService{

	@Autowired
	BankDbClient bankDbClient;

	@HystrixCommand(fallbackMethod = "getAccountFailed")
	public Account getAccount(String number) {
		return bankDbClient.getAccount(number);
	}

	@HystrixCommand(fallbackMethod = "updateAccountFailed")
	public Account updateAccount(long accountId, Account account) {
		return bankDbClient.updateAccount(accountId,account);
	}

	Account failedAccount(){
		Account account = new Account();
		account.setId(0L);
		account.setName("Failed");
		account.setNumber("0000");
		account.setAmount(0D);
		return account;
	}

	Account getAccountFailed(String test) {
		return failedAccount();
	}

	Account updateAccountFailed(long accountId, Account account) {
		return failedAccount();
	}
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