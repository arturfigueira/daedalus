package com.daedalus.core.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class DataMapping {
    protected final String name;
    protected final ElasticDataType type;

    //TODO Can have another property to indicate a different json name, from source data
}
