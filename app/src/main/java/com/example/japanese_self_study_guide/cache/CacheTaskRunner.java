package com.example.japanese_self_study_guide.cache;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CacheTaskRunner {

    private static final ExecutorService IO = Executors.newFixedThreadPool(4);

    private CacheTaskRunner() {}

    public static <T> Task<T> call(Callable<T> callable) {
        TaskCompletionSource<T> source = new TaskCompletionSource<>();
        IO.execute(() -> {
            try {
                source.setResult(callable.call());
            } catch (Exception e) {
                source.setException(e);
            }
        });
        return source.getTask();
    }

    public static void run(Runnable runnable) {
        IO.execute(runnable);
    }
}
