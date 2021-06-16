package com.kcover.dbdiffer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DiffFinder {
  private static Logger LOGGER = LoggerFactory.getLogger(DiffFinder.class);

  public static void writeDiffReportToFile(
      JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, FileWriter fileWriter) {
    try {
      fileWriter.write("MISSING ACCOUNTS:\n");
      DiffFinder.writeMissingAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
      fileWriter.write("\nCORRUPTED ACCOUNTS:\n");
      DiffFinder.writeCorruptedAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
      fileWriter.write("\nNEW ACCOUNTS:\n");
      DiffFinder.writeNewAccountsToFile(oldDbTemplate, newDbTemplate, pageSize, fileWriter);
    } catch (IOException e) {
      throw new RuntimeException("IO exception occurred while writing report");
    }
  }

  public static void writeMissingAccountsToFile(
      JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, FileWriter fileWriter) {
    // create a function to be performed for each page fetched from the oldDb
    Consumer<List<Map<String, Object>>> pageHandler =
        resultList -> {
          List<Account> oldAccounts =
              resultList.stream().map(Account::fromMap).collect(Collectors.toList());

          List<String> matchingNewAccountIds =
              fetchAccountsWithMatchingIds(oldAccounts, newDbTemplate)
                  .map(NewDbAccount::fromMap)
                  .map(NewDbAccount::getId)
                  .collect(Collectors.toList());

          List<Account> missingAccounts =
              oldAccounts.stream()
                  .filter(oldAccount -> !matchingNewAccountIds.contains(oldAccount.getId()))
                  .collect(Collectors.toList());
          try {
            writeAccounts(missingAccounts, fileWriter);
          } catch (IOException e) {
            throw new RuntimeException("IOError occured while finding missing accounts.", e);
          }
        };
    traverseDbAndPerform(oldDbTemplate, pageSize, pageHandler);
  }

  public static void writeCorruptedAccountsToFile(
      JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, FileWriter fileWriter) {
    // create a function to be performed for each page fetched from the oldDb
    Consumer<List<Map<String, Object>>> pageHandler =
        resultList -> {
          List<Account> oldAccounts =
              resultList.stream().map(Account::fromMap).collect(Collectors.toList());

          List<NewDbAccount> existingAccountsInNewDb =
              fetchAccountsWithMatchingIds(oldAccounts, newDbTemplate)
                  .map(NewDbAccount::fromMap)
                  .filter(newDbAccount -> !oldAccounts.contains(newDbAccount.toAccount()))
                  .collect(Collectors.toList());
          try {
            writeAccounts(existingAccountsInNewDb, fileWriter);
          } catch (IOException e) {
            throw new RuntimeException("IOError occured while finding corrupted accounts.", e);
          }
        };
    traverseDbAndPerform(oldDbTemplate, pageSize, pageHandler);
  }

  public static void writeNewAccountsToFile(
      JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, FileWriter fileWriter) {
    // create a function to be performed for each page fetched from the newDb
    Consumer<List<Map<String, Object>>> pageHandler =
        resultList -> {
          List<NewDbAccount> newDbAccounts =
              resultList.stream().map(NewDbAccount::fromMap).collect(Collectors.toList());

          List<String> matchingOldAccountIds =
              fetchAccountsWithMatchingIds(newDbAccounts, oldDbTemplate)
                  .map(Account::fromMap)
                  .map(Account::getId)
                  .collect(Collectors.toList());

          List<NewDbAccount> newAccountsNotInOldDb =
              newDbAccounts.stream()
                  .filter(newDbAccount -> !matchingOldAccountIds.contains(newDbAccount.getId()))
                  .collect(Collectors.toList());
          try {
            writeAccounts(newAccountsNotInOldDb, fileWriter);
          } catch (IOException e) {
            throw new RuntimeException("IOError occurred while writing out new accounts.", e);
          }
        };
    traverseDbAndPerform(newDbTemplate, pageSize, pageHandler);
  }

  /**
   * Iteratively creates SQL queries that will grab a page of accounts from the DB of the given
   * JdbcTemplate and pass them to a function. Use this to traverse a DB in chunks and perform some
   * function on all the accounts within. Passes the resulting list of maps from the query to the
   * pageHandler. It's then up to the pageHandler to convert the response into something it knows
   * how to use.
   */
  private static void traverseDbAndPerform(
      JdbcTemplate jdbcTemplate, int pageSize, Consumer<List<Map<String, Object>>> pageHandler) {
    if (pageSize < 1) {
      throw new IllegalArgumentException("Page size must be greater than 0.");
    }

    long accountsCount =
        jdbcTemplate.queryForObject("SELECT COUNT(id) FROM accounts", Integer.class);
    LOGGER.debug("accounts found in old DB: {}", accountsCount);

    long index = 0;
    while (index < accountsCount) {
      // get {pageSize} accounts from DB
      long sizeOfCurrentPage = pageSize;
      if (index + pageSize > accountsCount) {
        sizeOfCurrentPage = accountsCount - index;
      }
      String idQuery =
          String.format("SELECT * FROM accounts LIMIT %d OFFSET %d", sizeOfCurrentPage, index);
      pageHandler.accept(jdbcTemplate.queryForList(idQuery));

      index += sizeOfCurrentPage;
    }
  }

  private static Stream<Map<String, Object>> fetchAccountsWithMatchingIds(
      List<? extends Account> srcAccounts, JdbcTemplate targetDbTemplate) {
    // query newDB for matchingIDs
    StringBuilder matchingIdsQueryBuilder = new StringBuilder();
    matchingIdsQueryBuilder.append("SELECT * FROM accounts WHERE id IN (");
    for (Account account : srcAccounts) {
      matchingIdsQueryBuilder.append("'").append(account.getId()).append("', ");
    }
    // replace last comma with end paren
    matchingIdsQueryBuilder.replace(
        matchingIdsQueryBuilder.lastIndexOf(", "), matchingIdsQueryBuilder.capacity(), ") ");

    String matchingIdsQuery = matchingIdsQueryBuilder.toString();
    LOGGER.debug("MatchingIdsQuery: " + matchingIdsQuery);
    return targetDbTemplate.queryForList(matchingIdsQuery).stream();
  }

  private static <T extends Account> void writeAccounts(List<T> accounts, FileWriter fileWriter)
      throws IOException {
    for (T account : accounts) {
      fileWriter.write(account.toSqlValue() + ",\n");
    }
  }
}
