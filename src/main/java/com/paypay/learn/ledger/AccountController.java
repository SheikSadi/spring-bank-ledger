package com.paypay.learn.ledger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PutMapping("/accounts/{id}")
    ResponseEntity<?> updateAccount(
        @PathVariable String id,
        @Valid @RequestBody UpdateAccountRequest request
    ) {
        Account acc = service.updateAccount(id, request);
        return ResponseEntity.ok().body(AccountResponse.from(acc));
    }

    @GetMapping("/accounts/{id}")
    ResponseEntity<?> getAccountById(@PathVariable String id) {
        Optional<Account> acc = service.getAccountById(id);
        return acc
            .map(AccountResponse::from)
            .map(ResponseEntity::ok)
            .orElse(
                ResponseEntity.notFound().build()
            );
    }

    @DeleteMapping("/accounts/{id}")
    ResponseEntity<?> deleteAccount(@PathVariable String id) {
        return service.deleteAccount(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    @GetMapping("/accounts")
    ResponseEntity<List<AccountResponse>> getAccounts(
        @RequestParam Optional<String> currency
    ) {
        List<Account> accounts;
        if (currency.isEmpty()) {
            accounts = service.getAccounts();
        } else {
            accounts = service.getAccounts(currency.get());
        }

        // Alternative code
        // List<Account> accounts = currency
        //     .map(service::getAccounts)
        //     .orElseGet(service::getAccounts);

        List<AccountResponse> responses = accounts.stream()
            .map(AccountResponse::from)
            .toList();

        return ResponseEntity.ok().body(responses);
    }

    @PostMapping("/accounts")
    ResponseEntity<AccountResponse> createAccount(
        @Valid @RequestBody CreateAccountRequest req
    ) {
        Account acc = service.createAccount(req);
        URI location = URI.create("/accounts/" + acc.id());
        return ResponseEntity.created(location).body(AccountResponse.from(acc));
    }

    @PostMapping("/accounts/{id}/debit")
    ResponseEntity<?> debitAccount(
        @PathVariable String id,
        @Valid @RequestBody DebitAccountRequest request
    ) {
        Account updated = service.debitAccount(id, request.amount());
        return ResponseEntity.ok().body(AccountResponse.from(updated));
    }
}
