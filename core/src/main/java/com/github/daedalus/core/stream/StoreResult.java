package com.github.daedalus.core.stream;

import lombok.*;

/**
 * This object might be used to indicate if an asynchronous store operation concluded successfully
 * or not.
 * <p>
 * It`s constructor is not available and new instances should be created via {@link #success()} and
 * {@link #failure(Throwable)} static methods.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
public class StoreResult {

  protected boolean stored = true;
  @Getter
  protected Throwable error = null;

  /**
   * Returns a new success instance
   */
  public static StoreResult success() {
    return new StoreResult();
  }

  /**
   * Returns a new failure result, wrapping the error that caused the operation to fail.
   *
   * @param reason the reason for the failure
   */
  public static StoreResult failure(final Throwable reason) {
    return new StoreResult(false, reason);
  }
}
