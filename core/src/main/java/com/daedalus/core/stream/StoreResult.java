package com.daedalus.core.stream;

import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
public class StoreResult {
    protected boolean stored = true;
    @Getter protected Throwable error = null;

    public static StoreResult success(){
        return new StoreResult();
    }

    public static StoreResult failure(final Throwable reason){
        return new StoreResult(false, reason);
    }
}
