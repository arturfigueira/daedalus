package com.github.daedalus.plugins.json;

import com.github.daedalus.core.stream.DataStore;
import com.github.daedalus.core.stream.StoreResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class JsonAsyncFileStore implements DataStore {
    private final String storePath;
    private final long timeout;
    private final Gson gson = new Gson();

    public JsonAsyncFileStore(String storePath, final long timeout) {
        this.storePath = Optional.ofNullable(storePath)
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("Store Path can't be null nor empty"));

        if(timeout < 0){
            throw new IllegalArgumentException("Timeout must be greater or equal to zero");
        }

        this.timeout = timeout;
    }

    @Override
    public Future<StoreResult> store(String identifier, List<Map<String, Object>> data) {
        if(isArgumentsInvalid(identifier, data)){
            throw new IllegalArgumentException("filename and data must not be null. filename must not be empty");
        }

        final var path = Path.of(this.storePath, identifier +".json");
        var stringfiedData = gson.toJson(data);
        final var promiseToWrite = new CompletableFuture<StoreResult>();

        try (final var fileChannel = AsynchronousFileChannel.open(path, WRITE, CREATE)){
            final var byteBuffer = fillBuffer(stringfiedData);

            if(timeout > 0){
                promiseToWrite.completeOnTimeout(StoreResult.failure(new TimeoutException()),
                        this.timeout, TimeUnit.MILLISECONDS)
                        .thenRun(exceptionallyClose(fileChannel));
            }

            fileChannel.write(byteBuffer, 0, byteBuffer, completedCallback(promiseToWrite));
            byteBuffer.clear();
        } catch (IOException e) {
            promiseToWrite.complete(StoreResult.failure(e));
        }

        return promiseToWrite;
    }

    private ByteBuffer fillBuffer(final String data){
        final var byteBuffer = ByteBuffer.allocate(data.getBytes().length);
        byteBuffer.put(data.getBytes());
        byteBuffer.flip();
        return byteBuffer;
    }

    private static Runnable exceptionallyClose(AsynchronousFileChannel fileChannel) {
        return () -> {
            if (fileChannel.isOpen()) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    private static CompletionHandler<Integer, ByteBuffer> completedCallback(CompletableFuture<StoreResult> storeProcess) {
        return new CompletionHandler<>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if(!storeProcess.isDone()){
                    storeProcess.complete(StoreResult.success());
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                if(!storeProcess.isDone()){
                    storeProcess.complete(StoreResult.failure(exc));
                }
            }
        };
    }

    private static boolean isArgumentsInvalid(String filename, List<Map<String, Object>> data) {
        return filename == null || filename.isEmpty() || data == null;
    }

    @Override
    public Future<StoreResult> store(List<Map<String, Object>> data) {
        return this.store(UUID.randomUUID().toString(), data);
    }
}
