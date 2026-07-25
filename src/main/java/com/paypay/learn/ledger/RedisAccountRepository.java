package com.paypay.learn.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.ObjectMapper;

@Profile("redis")
@Repository
public class RedisAccountRepository implements AccountRepository {

  private final StringRedisTemplate redisTemplate;
  private final ValueOperations<String, String> valueOperations;
  private final ObjectMapper objectMapper;

  public RedisAccountRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.valueOperations = redisTemplate.opsForValue();
    this.objectMapper = objectMapper;
  }

  private String getKey(Account acc) {
    String id = acc.id();
    return "account:" + id;
  }

  private Account mapJsonToAccount(String jsonStr) {
    if (jsonStr == null) return null;
    try {
      return objectMapper.readValue(jsonStr, Account.class);
    } catch (JacksonIOException e) {
      return null;
    }
  }

  @Override
  public Account save(Account acc) {
    try {
      String jsonStr = objectMapper.writeValueAsString(acc);
      String key = getKey(acc);
      valueOperations.set(key, jsonStr);
      return acc;
    } catch (JacksonIOException e) {
      throw new RuntimeException("Redis write failed:", e);
    }
  };

  @Override
  public Optional<Account> find(String accountId) {
    String key = "account:" + accountId;
    String json = valueOperations.get(key);
    Account account = mapJsonToAccount(json);
    return Optional.ofNullable(account);
  }

  @Override
  public Optional<Account> findWithWriteLock(String accountId) {
    return find(accountId);
  }


  @Override
  public Optional<Account> delete(String accountId) {
    Optional<Account> account = find(accountId);
    String key = account.map(a -> getKey(a)).orElseThrow();

    return account.map(acc -> {
      if (!Boolean.TRUE.equals(
        redisTemplate.delete(key)
      )) {
        throw new RuntimeException("Redis delete failed for accountId: " + accountId);
      }
      return acc;
    });
  };

  @Override
  public List<Account> findAll() {
    List<Account> accounts = new ArrayList<Account>();
    
    ScanOptions options = ScanOptions.scanOptions()
      .match("account:*")
      .count(100) // Batch size per iteration
      .build()
    ;

    // Try with resource
    try (Cursor<String> cursor = redisTemplate.scan(options)) {
      while (cursor.hasNext()) {
        String key = cursor.next();
        String json = valueOperations.get(key);    
        Account account = mapJsonToAccount(json);
        accounts.add(account);
      }
    }

    return accounts;

  };

  @Override
  public List<Account> filterByCurrency(String currency) {
    List<Account> accounts = new ArrayList<Account>();
    
    ScanOptions options = ScanOptions.scanOptions()
      .match("account:*")
      .count(100) // Batch size per iteration
      .build()
    ;

    // Try with resource
    try (Cursor<String> cursor = redisTemplate.scan(options)) {
      while (cursor.hasNext()) {
        String key = cursor.next();
        String json = valueOperations.get(key);    
        Account account = mapJsonToAccount(json);
        if (currency.equals(account.currency())) {
          accounts.add(account);
        }
      }
    }

    return accounts;
  };
}
