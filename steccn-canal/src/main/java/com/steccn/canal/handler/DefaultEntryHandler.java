package com.steccn.canal.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.Map;

@CanalTable(value = "all")
@Component
@Slf4j
public class DefaultEntryHandler implements EntryHandler<Map<String, String>> {
    @Override
    public void insert(Map<String, String> map) {
//        log.info("insert message  {}", map);
    }

    @Override
    public void update(Map<String, String> before, Map<String, String> after) {
//        log.info("update before {} ", before);
//        log.info("update after {}", after);
    }

    @Override
    public void delete(Map<String, String> map) {
//        log.info("delete  {}", map);
    }
}