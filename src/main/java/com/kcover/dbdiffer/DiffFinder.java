package com.kcover.dbdiffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiffFinder{
    private static Logger LOGGER = LoggerFactory.getLogger(DiffFinder.class);

    private JdbcTemplate oldDb;
    private JdbcTemplate newDb;

    private DiffFinder(JdbcTemplate oldDb, JdbcTemplate newDb){
        this.oldDb = oldDb;
        this.newDb = newDb;
    }

    public static void writeMissingAccountsToFile(JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, File outputFile) {
        if(pageSize < 1){
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }

        long oldAccountCount = oldDbTemplate.queryForObject("SELECT COUNT(id) FROM accounts", Integer.class);
        LOGGER.debug("accounts found in old DB: {}", oldAccountCount);

        try(FileWriter fileWriter = new FileWriter(outputFile)) {
            fileWriter.write("MISSING ACCOUNTS:\n");

            long index = 0;
            while (index < oldAccountCount) {
                //get {pageSize} IDs from oldDb
                long sizeOfCurrentPage = pageSize;
                if (index + pageSize > oldAccountCount) {
                    sizeOfCurrentPage = oldAccountCount - index;
                }
                String idQuery = String.format("SELECT * FROM accounts LIMIT %d OFFSET %d", sizeOfCurrentPage, index);
                List<Account> oldAccounts = oldDbTemplate.queryForList(idQuery).stream().map(Account::fromMap)
                        .collect(Collectors.toList());

                List<String> matchingNewAccountIds =
                        fetchAccountsWithMatchingIds(oldAccounts, newDbTemplate)
                        .map(NewDbAccount::getId)
                        .collect(Collectors.toList());

                List<Account> missingAccounts = oldAccounts.stream()
                        .filter(oldAccount -> !matchingNewAccountIds.contains(oldAccount.getId()))
                        .collect(Collectors.toList());
                writeAccounts(missingAccounts, fileWriter);

                //increment missing accounts and index.
                index += sizeOfCurrentPage;
            }
        } catch (IOException e) {
            throw new RuntimeException("IOError occured while finding missing accounts.", e);
        }

    }




    public static void writeCorruptedAccountsToFile(JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, File outputFile) {
        if(pageSize < 1){
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }

        long oldAccountCount = oldDbTemplate.queryForObject("SELECT COUNT(id) FROM accounts", Integer.class);
        LOGGER.debug("accounts found in old DB: {}", oldAccountCount);

        try(FileWriter fileWriter = new FileWriter(outputFile)) {
            fileWriter.write("CORRUPTED ACCOUNTS:\n");

            long index = 0;
            while (index < oldAccountCount) {
                //get {pageSize} accounts from oldDb
                long sizeOfCurrentPage = pageSize;
                if (index + pageSize > oldAccountCount) {
                    sizeOfCurrentPage = oldAccountCount - index;
                }
                String idQuery = String.format("SELECT * FROM accounts LIMIT %d OFFSET %d", sizeOfCurrentPage, index);
                List<Account> oldAccounts = oldDbTemplate.queryForList(idQuery).stream().map(Account::fromMap)
                        .collect(Collectors.toList());

                List<NewDbAccount> existingAccountsInNewDb =
                        fetchAccountsWithMatchingIds(oldAccounts, newDbTemplate)
                                .filter(newDbAccount -> !oldAccounts.contains(newDbAccount.toAccount()))
                        .collect(Collectors.toList());
                writeAccounts(existingAccountsInNewDb, fileWriter);

                index += sizeOfCurrentPage;
            }
        } catch (IOException e) {
            throw new RuntimeException("IOError occured while finding corrupted accounts.", e);
        }

    }

    private static Stream<NewDbAccount> fetchAccountsWithMatchingIds(List<Account> oldAccounts, JdbcTemplate newDbTemplate) {
        //query newDB for matchingIDs
        StringBuilder matchingIdsQueryBuilder = new StringBuilder();
        matchingIdsQueryBuilder.append("SELECT * FROM accounts WHERE id IN (");
        for (Account account :
                oldAccounts) {
            matchingIdsQueryBuilder.append("'").append(account.getId()).append("', ");
        }
        //replace last comma with end paren
        matchingIdsQueryBuilder.replace(matchingIdsQueryBuilder.lastIndexOf(", "), matchingIdsQueryBuilder.capacity(), ") ");

        String matchingIdsQuery = matchingIdsQueryBuilder.toString();
            LOGGER.debug("MatchingIdsQuery: " + matchingIdsQuery);
        return newDbTemplate.queryForList(matchingIdsQuery).stream().map(NewDbAccount::fromMap);
    }

    private static <T extends Account> void writeAccounts(List<T> accounts, FileWriter fileWriter) throws IOException {
        Iterator<T> accountIterator = accounts.iterator();
        while (accountIterator.hasNext()) {
            T account = accountIterator.next();
            fileWriter.write(account.toSqlValue());

            if(accountIterator.hasNext()){
                fileWriter.write(",");
            }

            fileWriter.write("\n");
        }
    }
}