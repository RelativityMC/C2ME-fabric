package org.yatopiamc.c2me.common.threading.worldgen;

import org.threadly.concurrent.TaskPriority;
import org.yatopiamc.c2me.common.util.UnsafeUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;


public class PriorityCompletableFuture<T> extends CompletableFuture {

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor, TaskPriority priority) {
        try {
            return asyncSupplyStage((C2MEWorldGenPriorityExecutor) executor, supplier, priority);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <U> CompletableFuture<U> asyncSupplyStage(C2MEWorldGenPriorityExecutor e, Supplier<U> f, TaskPriority priority) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (f == null) throw new NullPointerException();
        CompletableFuture<U> d = new CompletableFuture<U>();
        e.execute(((Runnable) Objects.requireNonNull(UnsafeUtils.reflectedConstructors.computeIfAbsent("java.util.concurrent.CompletableFuture$AsyncSupply", key -> UnsafeUtils.getReflectedConstructor(key, CompletableFuture.class, Supplier.class))).newInstance(d, f)), priority);
        return d;
    }
}

