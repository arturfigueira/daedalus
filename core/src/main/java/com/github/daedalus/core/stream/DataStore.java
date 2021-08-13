package com.github.daedalus.core.stream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * An object that represents a external data storage to backup data.
 * <p>
 * This object contains asynchronous methods, based on java`s {@link Future}, to store the required
 * data. Results of these operations will be sent wrapped into a {@link StoreResult}, which will
 * indicate if the process was successfully completed or the reason for the failure.
 */
public interface DataStore {

  /**
   * Store the data, with an specific identifier, asynchronously
   *
   * @param identifier A string that will identify the data being stored
   * @param data       a list containing the data to be store
   * @return A future that will resolve with the {@link StoreResult}
   */
  Future<StoreResult> store(String identifier, List<Map<String, Object>> data);

  /**
   * Store the given data asynchronously
   *
   * @param data a list containing the data to be store
   * @return A future that will resolve with the {@link StoreResult}
   */
  Future<StoreResult> store(List<Map<String, Object>> data);
}
