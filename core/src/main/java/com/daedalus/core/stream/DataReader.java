package com.daedalus.core.stream;

import com.daedalus.core.data.DataNode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

public interface DataReader {

    @Getter
    @ToString
    class Criteria{
        protected final int page;
        protected final int size;

        public Criteria(int page, int size) {
            if(page < 0 || size <= 0){
                throw new IllegalArgumentException("Invalid criteria. page: "+page+", size: "+size);
            }
            this.page = page;
            this.size = size;
        }

        public int startAt(){
            return page * size;
        }

        public int until(){
            return this.startAt()+ (size-1);
        }

        public Criteria nextPage(){
            return new Criteria(this.page+1, this.size);
        }

        public static Criteria fromBeginning(int size){
            return new Criteria(0, size);
        }
    }

    String getSource() throws DataStreamException;
    List<DataNode> read(final Criteria readerCriteria) throws DataStreamException;
}
