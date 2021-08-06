package com.daedalus.plugins.json;

import com.daedalus.core.data.Document;
import com.daedalus.core.stream.DataReader;
import com.daedalus.core.stream.DataStreamException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonFileReader implements DataReader {

    protected final String path;
    protected final Charset charset;
    protected Gson gson;

    @Override
    public String getSource() {
        return this.path;
    }

    @Override
    public List<Document> read(final Criteria readerCriteria) throws DataStreamException {
        final var dataNodes = new ArrayList<Document>();
        try(var is = Files.newInputStream(Path.of(this.path));
            var reader = new JsonReader(new InputStreamReader(is, this.charset))) {
            var cursor = 0;
            this.gson = new Gson();
            reader.beginArray();
            while (cursor <= readerCriteria.until() && reader.hasNext()){
                if(cursor >= readerCriteria.startAt()){
                    var dataNode = new Document(this.path, this.readElement(reader));
                    dataNodes.add(dataNode);
                }else{
                   this.ignoreElement(reader);
                }
                cursor++;
            }
        }  catch (IOException e) {
            throw new DataStreamException("Unable to open stream for file at "+this.path, e);
        }
        return dataNodes;
    }

    protected void ignoreElement(JsonReader reader) throws IOException {
        reader.beginObject();
        while(!reader.peek().equals(JsonToken.END_OBJECT) && reader.hasNext()){
            reader.skipValue();
        }
        reader.endObject();
    }

    protected Map<String, Object> readElement(final JsonReader reader){
        return this.gson.fromJson(reader, Map.class);
    }
}
