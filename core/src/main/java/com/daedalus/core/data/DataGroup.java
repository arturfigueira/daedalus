package com.daedalus.core.data;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
public class DataGroup {
    @Getter
    protected final String id;
    protected final List<DataNode> dataNodes = new ArrayList<>();

    public DataGroup(String id, List<DataNode> dataNodes) {
        this.id = id;
        this.dataNodes.addAll(dataNodes);
    }

    public List<DataNode> getDataNodes() {
        return Collections.unmodifiableList(dataNodes);
    }
}
