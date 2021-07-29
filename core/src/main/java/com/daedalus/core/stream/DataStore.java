package com.daedalus.core.stream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface DataStore {
    Future<StoreResult> store(String identifier, List<Map<String, Object>> data);
    Future<StoreResult> store(List<Map<String, Object>> data);
}
