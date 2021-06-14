package com.kcover.dbdiffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiffFinder{
    private static Logger LOGGER = LoggerFactory.getLogger(DiffFinder.class);

    private JdbcTemplate oldDb;
    private JdbcTemplate newDb;

    private DiffFinder(JdbcTemplate oldDb, JdbcTemplate newDb){
        this.oldDb = oldDb;
        this.newDb = newDb;
    }

    //TODO: lower logger levels
    public static void writeMissingRecordsToFile(JdbcTemplate oldDbTemplate, JdbcTemplate newDbTemplate, int pageSize, File outputFile) {
        LOGGER.info("Starting findMissingRecords.");
        if(pageSize < 1){
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }

        long oldRecordCount = oldDbTemplate.queryForObject("SELECT COUNT(id) FROM accounts", Integer.class);
        LOGGER.info("records found in old DB: {}", oldRecordCount);

        //Start Missing Accounts file
        try(FileWriter fileWriter = new FileWriter(outputFile)) {
            fileWriter.write("MISSING ACCOUNTS:\n");

            long index = 0;
            while (index < oldRecordCount) {
                //get {pageSize} IDs from oldDb
                long sizeOfCurrentPage = pageSize;
                if (index + pageSize > oldRecordCount) {
                    sizeOfCurrentPage = oldRecordCount - index;
                }
                String idQuery = String.format("SELECT * FROM accounts LIMIT %d OFFSET %d", sizeOfCurrentPage, index);
                List<Account> oldRecords = oldDbTemplate.queryForList(idQuery).stream().map(Account::fromMap)
                        .collect(Collectors.toList());

                //query newDB for matchingIDs
                StringBuilder matchingIdsQueryBuilder = new StringBuilder();
                matchingIdsQueryBuilder.append("SELECT id FROM accounts WHERE id IN (");
                for (Account account :
                        oldRecords) {
                    matchingIdsQueryBuilder.append("'").append(account.getId()).append("', ");
                }
                //replace last comma with end paren
                matchingIdsQueryBuilder.replace(matchingIdsQueryBuilder.lastIndexOf(", "), matchingIdsQueryBuilder.capacity(), ") ");

                String matchingIdsQuery = matchingIdsQueryBuilder.toString();
                LOGGER.info("MatchingIdsQuery: " + matchingIdsQuery);
                List<String> newRecordIds = newDbTemplate.queryForList(matchingIdsQuery, String.class);

                List<Account> missingRecords =
                        oldRecords
                                .stream()
                                .filter(account -> !newRecordIds.contains(account.getId()))
                                .collect(Collectors.toList());
                LOGGER.info("Missing Record Ids: " + missingRecords);

                Iterator<Account> accountIterator = missingRecords.iterator();
                while (accountIterator.hasNext()) {
                    Account missingRecord = accountIterator.next();
                    fileWriter.write(String.format("('%s', '%s', '%s')", missingRecord.getId(), missingRecord.getName(), missingRecord.getEmail()));

                    if(accountIterator.hasNext()){
                        fileWriter.write(",");
                    }

                    fileWriter.write("\n");
                }

                //increment missing Records and index.
                index += sizeOfCurrentPage;
            }
        } catch (IOException e) {
            throw new RuntimeException("IOError occured while finding missing accounts.", e);
        }

    }



}