package com.github.daedalus.plugins.json;

import com.github.daedalus.core.stream.DataReader;
import com.github.daedalus.core.stream.DataSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonSource implements DataSource {

    protected Set<String> filePaths;
    protected final Charset charset;
    protected Iterator<String> iterator;

    public JsonSource(final String rootDir, final Charset charset) throws IOException {
        var dir = Optional.ofNullable(rootDir)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Root Dir should not be empty nor null"));

        this.charset = Optional.ofNullable(charset)
                .orElseThrow(()-> new IllegalArgumentException("Charset should not be null"));

        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            this.filePaths = stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(s -> Path.of(rootDir, s).toString())
                    .collect(Collectors.toSet());

            this.iterator = filePaths.iterator();
        }
    }

    public static JsonSource withDefaultCharset(final String rootDir) throws IOException {
        return new JsonSource(rootDir, StandardCharsets.UTF_8);
    }

    @Override
    public DataReader next() {
        return new JsonFileReader(this.iterator.next(), this.charset);
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }
}
