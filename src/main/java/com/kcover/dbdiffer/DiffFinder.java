package com.kcover.dbdiffer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class DiffFinder{

    private JdbcTemplate oldDb;
    private JdbcTemplate newDb;

    public DiffFinder(JdbcTemplate oldDb, JdbcTemplate newDb){
        this.oldDb = oldDb;
        this.newDb = newDb;
    }

    public String createMissingEntriesReport(){
        long oldDbEntryCount = 0;
        long newDbEntryCount = 0;
        long oldDbPageNum = 0;
        long newDbPageNum = 0;
        return "";
    }


}