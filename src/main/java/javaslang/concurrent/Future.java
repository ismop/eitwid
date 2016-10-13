/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.concurrent;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Value;
import javaslang.collection.Iterator;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import javaslang.control.Try.CheckedFunction;
import javaslang.control.Try.CheckedPredicate;
import javaslang.control.Try.CheckedRunnable;
import javaslang.control.Try.CheckedSupplier;

/**
 * A Future is a computation result that becomes available at some point. All operations provided are non-blocking.
 * <p>
 * The underlying {@code ExecutorService} is used to execute asynchronous handlers, e.g. via
 * {@code onComplete(...)}.
 * <p>
 * A Future has two states: pending and completed.
 * <ul>
 * <li>Pending: The computation is ongoing. Only a pending future may be completed or cancelled.</li>
 * <li>Completed: The computation finished successfully with a result, failed with an exception or was cancelled.</li>
 * </ul>
 * Callbacks may be registered on a Future at each point of time. These actions are performed as soon as the Future
 * is completed. An action which is registered on a completed Future is immediately performed. The action may run on
 * a separate Thread, depending on the underlying ExecutorService. Actions which are registered on a cancelled
 * Future are performed with the failed result.
 *
 * @param <T> Type of the computation result.
 * @author Daniel Dietrich
 * @since 2.0.0
 */
public interface Future<T> extends Value<T> {

    /**
     * The default executor service is {@link Executors#newCachedThreadPool()}.
     * Please note that it may prevent the VM from shutdown.
     */
    ExecutorService DEFAULT_EXECUTOR_SERVICE = new CurrentThreadExecutorService();

    /**
     * Creates a failed {@code Future} with the given {@code exception}, backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param exception The reason why it failed.
     * @param <T>       The value type of a successful result.
     * @return A failed {@code Future}.
     * @throws NullPointerException if exception is null
     */
    static <T> Future<T> failed(Throwable exception) {
        Objects.requireNonNull(exception, "exception is null");
        return failed(DEFAULT_EXECUTOR_SERVICE, exception);
    }

    /**
     * Creates a failed {@code Future} with the given {@code exception}, backed by the given {@link ExecutorService}.
     *
     * @param executorService An executor service.
     * @param exception       The reason why it failed.
     * @param <T>             The value type of a successful result.
     * @return A failed {@code Future}.
     * @throws NullPointerException if executorService or exception is null
     */
    static <T> Future<T> failed(ExecutorService executorService, Throwable exception) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(exception, "exception is null");
        return Promise.<T> failed(executorService, exception).future();
    }

    /**
     * Returns a {@code Future} that eventually succeeds with the first result of the given {@code Future}s which
     * matches the given {@code predicate}. If no result matches, the {@code Future} will contain {@link Option.None}.
     * <p>
     * The returned {@code Future} is backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param futures   An iterable of futures.
     * @param predicate A predicate that tests successful future results.
     * @param <T>       Result type of the futures.
     * @return A Future of an {@link Option} of the first result of the given {@code futures} that satisfies the given {@code predicate}.
     * @throws NullPointerException if one of the arguments is null
     */
    static <T> Future<Option<T>> find(Iterable<? extends Future<? extends T>> futures, Predicate<? super T> predicate) {
        return find(DEFAULT_EXECUTOR_SERVICE, futures, predicate);
    }

    /**
     * Returns a {@code Future} that eventually succeeds with the first result of the given {@code Future}s which
     * matches the given {@code predicate}. If no result matches, the {@code Future} will contain {@link Option.None}.
     * <p>
     * The returned {@code Future} is backed by the given {@link ExecutorService}.
     *
     * @param executorService An executor service.
     * @param futures         An iterable of futures.
     * @param predicate       A predicate that tests successful future results.
     * @param <T>             Result type of the futures.
     * @return A Future of an {@link Option} of the first result of the given {@code futures} that satisfies the given {@code predicate}.
     * @throws NullPointerException if one of the arguments is null
     */
    static <T> Future<Option<T>> find(ExecutorService executorService, Iterable<? extends Future<? extends T>> futures, Predicate<? super T> predicate) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(futures, "futures is null");
        Objects.requireNonNull(predicate, "predicate is null");
        final Promise<Option<T>> promise = Promise.make(executorService);
        final List<Future<? extends T>> list = List.ofAll(futures);
        if (list.isEmpty()) {
            promise.success(Option.none());
        } else {
            final AtomicInteger count = new AtomicInteger(list.length());
            list.forEach(future -> future.onComplete(result -> {
                synchronized (count) {
                    // if the promise is already completed we already found our result and there is nothing more to do.
                    if (!promise.isCompleted()) {
                        // when there are no more results we return a None
                        final boolean wasLast = count.decrementAndGet() == 0;
                        // when result is a Failure or predicate is false then we check in onFailure for finish
                        result.filter(predicate)
                                .onSuccess(value -> promise.trySuccess(Option.some(value)))
                                .onFailure(ignored -> {
                                    if (wasLast) {
                                        promise.trySuccess(Option.none());
                                    }
                                });
                    }
                }
            }));
        }
        return promise.future();
    }

    /**
     * Returns a new {@code Future} that will contain the result of the first of the given futures that is completed,
     * backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param futures An iterable of futures.
     * @param <T>     The result type.
     * @return A new {@code Future}.
     * @throws NullPointerException if futures is null
     */
    static <T> Future<T> firstCompletedOf(Iterable<? extends Future<? extends T>> futures) {
        return firstCompletedOf(DEFAULT_EXECUTOR_SERVICE, futures);
    }

    /**
     * Returns a new {@code Future} that will contain the result of the first of the given futures that is completed,
     * backed by the given {@link ExecutorService}.
     *
     * @param executorService An executor service.
     * @param futures         An iterable of futures.
     * @param <T>             The result type.
     * @return A new {@code Future}.
     * @throws NullPointerException if executorService or futures is null
     */
    static <T> Future<T> firstCompletedOf(ExecutorService executorService, Iterable<? extends Future<? extends T>> futures) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(futures, "futures is null");
        final Promise<T> promise = Promise.make(executorService);
        final Consumer<Try<? extends T>> completeFirst = promise::tryComplete;
        futures.forEach(future -> future.onComplete(completeFirst));
        return promise.future();
    }

    /**
     * Returns a Future which contains the result of the fold of the given future values. If any future or the fold
     * fail, the result is a failure.
     * <p>
     * The resulting {@code Future} is backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param futures An iterable of futures.
     * @param zero    The zero element of the fold.
     * @param f       The fold operation.
     * @param <T>     The result type of the given {@code Futures}.
     * @param <U>     The fold result type.
     * @return A new {@code Future} that will contain the fold result.
     * @throws NullPointerException if futures or f is null.
     */
    static <T, U> Future<U> fold(Iterable<? extends Future<? extends T>> futures, U zero, BiFunction<? super U, ? super T, ? extends U> f) {
        return fold(DEFAULT_EXECUTOR_SERVICE, futures, zero, f);
    }

    /**
     * Returns a Future which contains the result of the fold of the given future values. If any future or the fold
     * fail, the result is a failure.
     * <p>
     * The resulting {@code Future} is backed by the given {@link ExecutorService}.
     *
     * @param executorService An {@code ExecutorService}.
     * @param futures         An iterable of futures.
     * @param zero            The zero element of the fold.
     * @param f               The fold operation.
     * @param <T>             The result type of the given {@code Futures}.
     * @param <U>             The fold result type.
     * @return A new {@code Future} that will contain the fold result.
     * @throws NullPointerException if executorService, futures or f is null.
     */
    static <T, U> Future<U> fold(ExecutorService executorService, Iterable<? extends Future<? extends T>> futures, U zero, BiFunction<? super U, ? super T, ? extends U> f) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(futures, "futures is null");
        Objects.requireNonNull(f, "f is null");
        if (!futures.iterator().hasNext()) {
            return successful(executorService, zero);
        } else {
            return sequence(executorService, futures).map(seq -> seq.foldLeft(zero, f));
        }
    }

    /**
     * Creates a {@code Future} with the given java.util.concurrent.Future, backed by the {@link #DEFAULT_EXECUTOR_SERVICE}
     *
     * @param future A {@link java.util.concurrent.Future}
     * @param <T>    Result type of the Future
     * @return A new {@code Future} wrapping the result of the Java future
     * @throws NullPointerException if future is null
     */
    static <T> Future<T> fromJavaFuture(java.util.concurrent.Future<T> future) {
        Objects.requireNonNull(future, "future is null");
        return Future.of(DEFAULT_EXECUTOR_SERVICE, future::get);
    }

    /**
     * Creates a {@code Future} with the given java.util.concurrent.Future, backed by given {@link ExecutorService}
     *
     * @param executorService An {@link ExecutorService}
     * @param future          A {@link java.util.concurrent.Future}
     * @param <T>             Result type of the Future
     * @return A new {@code Future} wrapping the result of the Java future
     * @throws NullPointerException if executorService or future is null
     */
    static <T> Future<T> fromJavaFuture(ExecutorService executorService, java.util.concurrent.Future<T> future) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(future, "future is null");
        return Future.of(executorService, future::get);
    }

    /**
     * Creates a {@code Future} from a {@link Try}, backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param result The result.
     * @param <T>    The value type of a successful result.
     * @return A completed {@code Future} which contains either a {@code Success} or a {@code Failure}.
     * @throws NullPointerException if result is null
     */
    static <T> Future<T> fromTry(Try<? extends T> result) {
        return fromTry(DEFAULT_EXECUTOR_SERVICE, result);
    }

    /**
     * Creates a {@code Future} from a {@link Try}, backed by the given {@link ExecutorService}.
     *
     * @param executorService An {@code ExecutorService}.
     * @param result          The result.
     * @param <T>             The value type of a successful result.
     * @return A completed {@code Future} which contains either a {@code Success} or a {@code Failure}.
     * @throws NullPointerException if executorService or result is null
     */
    static <T> Future<T> fromTry(ExecutorService executorService, Try<? extends T> result) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(result, "result is null");
        return Promise.<T> fromTry(executorService, result).future();
    }

    /**
     * Narrows a widened {@code Future<? extends T>} to {@code Future<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param future A {@code Future}.
     * @param <T>    Component type of the {@code Future}.
     * @return the given {@code future} instance as narrowed type {@code Future<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Future<T> narrow(Future<? extends T> future) {
        return (Future<T>) future;
    }

    /**
     * Starts an asynchronous computation, backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param computation A computation.
     * @param <T>         Type of the computation result.
     * @return A new Future instance.
     * @throws NullPointerException if computation is null.
     */
    static <T> Future<T> of(CheckedSupplier<? extends T> computation) {
        return Future.of(DEFAULT_EXECUTOR_SERVICE, computation);
    }

    /**
     * Starts an asynchronous computation, backed by the given {@link ExecutorService}.
     *
     * @param executorService An executor service.
     * @param computation     A computation.
     * @param <T>             Type of the computation result.
     * @return A new Future instance.
     * @throws NullPointerException if one of executorService or computation is null.
     */
    static <T> Future<T> of(ExecutorService executorService, CheckedSupplier<? extends T> computation) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(computation, "computation is null");
        final FutureImpl<T> future = new FutureImpl<>(executorService);
        future.run(computation);
        return future;
    }

    /**
     * Returns a Future which contains the reduce result of the given future values. The zero is the result of the
     * first future that completes. If any future or the reduce operation fail, the result is a failure.
     * <p>
     * The resulting {@code Future} is backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param futures An iterable of futures.
     * @param f       The reduce operation.
     * @param <T>     The result type of the given {@code Futures}.
     * @return A new {@code Future} that will contain the reduce result.
     * @throws NullPointerException if executorService, futures or f is null.
     */
    static <T> Future<T> reduce(Iterable<? extends Future<? extends T>> futures, BiFunction<? super T, ? super T, ? extends T> f) {
        return reduce(DEFAULT_EXECUTOR_SERVICE, futures, f);
    }

    /**
     * Returns a Future which contains the reduce result of the given future values. The zero is the result of the
     * first future that completes. If any future or the reduce operation fail, the result is a failure.
     * <p>
     * The resulting {@code Future} is backed by the given {@link ExecutorService}.
     *
     * @param executorService An {@code ExecutorService}.
     * @param futures         An iterable of futures.
     * @param f               The reduce operation.
     * @param <T>             The result type of the given {@code Futures}.
     * @return A new {@code Future} that will contain the reduce result.
     * @throws NullPointerException if executorService, futures or f is null.
     */
    static <T> Future<T> reduce(ExecutorService executorService, Iterable<? extends Future<? extends T>> futures, BiFunction<? super T, ? super T, ? extends T> f) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(futures, "futures is null");
        Objects.requireNonNull(f, "f is null");
        if (!futures.iterator().hasNext()) {
            throw new NoSuchElementException("Future.reduce on empty futures");
        } else {
            return Future.<T> sequence(futures).map(seq -> seq.reduceLeft(f));
        }
    }

    /**
     * Runs an asynchronous computation, backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param unit A unit of work.
     * @return A new Future instance which results in nothing.
     * @throws NullPointerException if unit is null.
     */
    static Future<Void> run(CheckedRunnable unit) {
        return run(DEFAULT_EXECUTOR_SERVICE, unit);
    }

    /**
     * Starts an asynchronous computation, backed by the given {@link ExecutorService}.
     *
     * @param executorService An executor service.
     * @param unit            A unit of work.
     * @return A new Future instance which results in nothing.
     * @throws NullPointerException if one of executorService or unit is null.
     */
    static Future<Void> run(ExecutorService executorService, CheckedRunnable unit) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(unit, "unit is null");
        return Future.of(executorService, () -> {
            unit.run();
            return null;
        });
    }

    /**
     * Reduces many {@code Future}s into a single {@code Future} by transforming an
     * {@code Iterable<Future<? extends T>>} into a {@code Future<Seq<T>>}.
     * <p>
     * The resulting {@code Future} is backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * <ul>
     * <li>
     * If all of the given Futures succeed, sequence() succeeds too:
     * <pre><code>// = Future(Success(Seq(1, 2)))
     * sequence(
     *     List.of(
     *         Future.of(() -&gt; 1),
     *         Future.of(() -&gt; 2)
     *     )
     * );</code></pre>
     * </li>
     * <li>
     * If a given Future fails, sequence() fails too:
     * <pre><code>// = Future(Failure(Error)))
     * sequence(
     *     List.of(
     *         Future.of(() -&gt; 1),
     *         Future.of(() -&gt; { throw new Error(); }
     *     )
     * );</code></pre>
     * </li>
     * </ul>
     *
     * @param futures An {@code Iterable} of {@code Future}s.
     * @param <T>     Result type of the futures.
     * @return A {@code Future} of a {@link Seq} of results.
     * @throws NullPointerException if futures is null.
     */
    static <T> Future<Seq<T>> sequence(Iterable<? extends Future<? extends T>> futures) {
        return sequence(DEFAULT_EXECUTOR_SERVICE, futures);
    }

    /**
     * Reduces many {@code Future}s into a single {@code Future} by transforming an
     * {@code Iterable<Future<? extends T>>} into a {@code Future<Seq<T>>}.
     * <p>
     * The resulting {@code Future} is backed by the given {@link ExecutorService}.
     *
     * @param executorService An {@code ExecutorService}.
     * @param futures         An {@code Iterable} of {@code Future}s.
     * @param <T>             Result type of the futures.
     * @return A {@code Future} of a {@link Seq} of results.
     * @throws NullPointerException if executorService or futures is null.
     */
    static <T> Future<Seq<T>> sequence(ExecutorService executorService, Iterable<? extends Future<? extends T>> futures) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(futures, "futures is null");
        final Future<Seq<T>> zero = successful(executorService, Stream.empty());
        final BiFunction<Future<Seq<T>>, Future<? extends T>, Future<Seq<T>>> f =
                (result, future) -> result.flatMap(seq -> future.map(seq::append));
        return Iterator.ofAll(futures).foldLeft(zero, f);
    }

    /**
     * Creates a succeeded {@code Future}, backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param result The result.
     * @param <T>    The value type of a successful result.
     * @return A succeeded {@code Future}.
     */
    static <T> Future<T> successful(T result) {
        return successful(DEFAULT_EXECUTOR_SERVICE, result);
    }

    /**
     * Creates a succeeded {@code Future}, backed by the given {@link ExecutorService}.
     *
     * @param executorService An {@code ExecutorService}.
     * @param result          The result.
     * @param <T>             The value type of a successful result.
     * @return A succeeded {@code Future}.
     * @throws NullPointerException if executorService is null
     */
    static <T> Future<T> successful(ExecutorService executorService, T result) {
        Objects.requireNonNull(executorService, "executorService is null");
        return Promise.successful(executorService, result).future();
    }

    /**
     * Maps the values of an iterable in parallel to a sequence of mapped values into a single {@code Future} by
     * transforming an {@code Iterable<? extends T>} into a {@code Future<Seq<U>>}.
     * <p>
     * The resulting {@code Future} is backed by the {@link #DEFAULT_EXECUTOR_SERVICE}.
     *
     * @param values An {@code Iterable} of {@code Future}s.
     * @param mapper A mapper of values to Futures
     * @param <T>    The type of the given values.
     * @param <U>    The mapped value type.
     * @return A {@code Future} of a {@link Seq} of results.
     * @throws NullPointerException if values or f is null.
     */
    static <T, U> Future<Seq<U>> traverse(Iterable<? extends T> values, Function<? super T, ? extends Future<? extends U>> mapper) {
        return traverse(DEFAULT_EXECUTOR_SERVICE, values, mapper);
    }

    /**
     * Maps the values of an iterable in parallel to a sequence of mapped values into a single {@code Future} by
     * transforming an {@code Iterable<? extends T>} into a {@code Future<Seq<U>>}.
     * <p>
     * The resulting {@code Future} is backed by the given {@link ExecutorService}.
     *
     * @param executorService An {@code ExecutorService}.
     * @param values          An {@code Iterable} of values.
     * @param mapper          A mapper of values to Futures
     * @param <T>             The type of the given values.
     * @param <U>             The mapped value type.
     * @return A {@code Future} of a {@link Seq} of results.
     * @throws NullPointerException if executorService, values or f is null.
     */
    static <T, U> Future<Seq<U>> traverse(ExecutorService executorService, Iterable<? extends T> values, Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(values, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return sequence(Iterator.ofAll(values).map(mapper));
    }

    // -- non-static Future API

    /**
     * Support for chaining of callbacks that are guaranteed to be executed in a specific order.
     * <p>
     * An exception, which occurs when performing the given {@code action}, is not propagated to the outside.
     * In other words, subsequent actions are performed based on the value of the original Future.
     * <p>
     * Example:
     * <pre><code>
     * // prints Success(1)
     * Future.of(() -&gt; 1)
     *       .andThen(t -&gt; { throw new Error(""); })
     *       .andThen(System.out::println);
     * </code></pre>
     *
     * @param action A side-effecting action.
     * @return A new Future that contains this result and which is completed after the given action was performed.
     * @throws NullPointerException if action is null
     */
    default Future<T> andThen(Consumer<? super Try<T>> action) {
        Objects.requireNonNull(action, "action is null");
        final Promise<T> promise = Promise.make(executorService());
        onComplete(t -> {
            Try.run(() -> action.accept(t));
            promise.complete(t);
        });
        return promise.future();
    }

    /**
     * Blocks the current Thread until this Future completed or returns immediately if this Future is already completed.
     *
     * @return this {@code Future} instance
     */
    Future<T> await();

    /**
     * Cancels the Future. A running thread is interrupted.
     * <p>
     * If the Future was successfully cancelled, the result is a {@code Failure(CancellationException)}.
     *
     * @return {@code false}, if this {@code Future} is already completed or could not be cancelled, otherwise {@code true}.
     */
    default boolean cancel() {
        return cancel(true);
    }

    /**
     * Cancels the Future. A pending Future may be interrupted, depending on the underlying ExecutionService.
     * <p>
     * If the Future was successfully cancelled, the result is a {@code Failure(CancellationException)}.
     *
     * @param mayInterruptIfRunning {@code true} if a running thread should be interrupted, otherwise a running thread
     *                              is allowed to complete its computation.
     * @return {@code false}, if this {@code Future} is already completed or could not be cancelled, otherwise {@code true}.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Returns the {@link ExecutorService} used by this {@code Future}.
     *
     * @return The underlying {@code ExecutorService}.
     */
    ExecutorService executorService();

    /**
     * A projection that inverses the result of this Future.
     * <p>
     * If this Future succeeds, the failed projection returns a failure containing a {@code NoSuchElementException}.
     * <p>
     * If this Future fails, the failed projection returns a success containing the exception.
     *
     * @return A new Future which contains an exception at a point of time.
     */
    default Future<Throwable> failed() {
        final Promise<Throwable> promise = Promise.make(executorService());
        onComplete(result -> {
            if (result.isFailure()) {
                promise.success(result.getCause());
            } else {
                promise.failure(new NoSuchElementException("Future.failed completed without a throwable"));
            }
        });
        return promise.future();
    }

    /**
     * Returns a Future that returns the result of this Future, if it is a success. If the value of this Future is a
     * failure, the result of {@code that} Future is returned, if that is a success. If both Futures fail, the failure
     * of this Future is returned.
     * <p>
     * Example:
     * <pre><code>
     * Future&lt;Integer&gt; future = Future.of(() -&gt; { throw new Error(); });
     * Future&lt;Integer&gt; that = Future.of(() -&gt; 1);
     * Future&lt;Integer&gt; result = future.fallbackTo(that);
     *
     * // prints Some(1)
     * result.onComplete(System.out::println);
     * </code></pre>
     *
     * @param that A fallback future computation
     * @return A new Future
     * @throws NullPointerException if that is null
     */
    default Future<T> fallbackTo(Future<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        final Promise<T> promise = Promise.make(executorService());
        onComplete(t -> {
            if (t.isSuccess()) {
                promise.complete(t);
            } else {
                that.onComplete(alt -> {
                    if (alt.isSuccess()) {
                        promise.complete(alt);
                    } else {
                        promise.complete(t);
                    }
                });
            }
        });
        return promise.future();
    }

    /**
     * Shortcut for {@code filterTry(predicate::test}.
     *
     * @param predicate A predicate
     * @return A new {@code Future}
     * @throws NullPointerException if {@code predicate} is null
     */
    default Future<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filterTry(predicate::test);
    }

    /**
     * Filters the result of this {@code Future} by calling {@link Try#filterTry(CheckedPredicate)}.
     *
     * @param predicate A checked predicate
     * @return A new {@code Future}
     * @throws NullPointerException if {@code predicate} is null
     */
    default Future<T> filterTry(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final Promise<T> promise = Promise.make(executorService());
        onComplete(result -> promise.complete(result.filterTry(predicate)));
        return promise.future();
    }

    /**
     * Returns the underlying exception of this Future, syntactic sugar for {@code future.getValue().map(Try::getCause)}.
     *
     * @return None if the Future is not completed yet. Returns Some(Throwable) if the Future was completed with a failure.
     * @throws UnsupportedOperationException if the Future was successfully completed with a value
     */
    default Option<Throwable> getCause() {
        return getValue().map(Try::getCause);
    }

    /**
     * Returns the value of the Future.
     *
     * @return {@code None}, if the Future is not yet completed or was cancelled, otherwise {@code Some(Try)}.
     */
    Option<Try<T>> getValue();

    /**
     * Checks if this Future is completed, i.e. has a value.
     *
     * @return true, if the computation successfully finished, failed or was cancelled, false otherwise.
     */
    boolean isCompleted();

    /**
     * Checks if this Future completed with a success.
     *
     * @return true, if this Future completed and is a Success, false otherwise.
     */
    default boolean isSuccess() {
        return getValue().map(Try::isSuccess).getOrElse(false);
    }

    /**
     * Checks if this Future completed with a failure.
     *
     * @return true, if this Future completed and is a Failure, false otherwise.
     */
    default boolean isFailure() {
        return getValue().map(Try::isFailure).getOrElse(false);
    }

    /**
     * Performs the action once the Future is complete.
     *
     * @param action An action to be performed when this future is complete.
     * @return this Future
     * @throws NullPointerException if {@code action} is null.
     */
    Future<T> onComplete(Consumer<? super Try<T>> action);

    /**
     * Performs the action once the Future is complete and the result is a {@link Try.Failure}. Please note that the
     * future is also a failure when it was cancelled.
     *
     * @param action An action to be performed when this future failed.
     * @return this Future
     * @throws NullPointerException if {@code action} is null.
     */
    default Future<T> onFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action is null");
        return onComplete(result -> result.onFailure(action));
    }

    /**
     * Performs the action once the Future is complete and the result is a {@link Try.Success}.
     *
     * @param action An action to be performed when this future succeeded.
     * @return this Future
     * @throws NullPointerException if {@code action} is null.
     */
    default Future<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        return onComplete(result -> result.onSuccess(action));
    }

    /**
     * Handles a failure of this Future by returning another result.
     * <p>
     * Example:
     * <pre><code>
     * // = "oh!"
     * Future.of(() -&gt; new Error("oh!")).recover(Throwable::getMessage);
     * </code></pre>
     *
     * @param f A function which takes the exception of a failure and returns a new value.
     * @return A new Future.
     * @throws NullPointerException if {@code f} is null
     */
    default Future<T> recover(Function<? super Throwable, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return transformValue(t -> t.recover(f::apply));
    }

    /**
     * Handles a failure of this Future by returning the result of another Future.
     * <p>
     * Example:
     * <pre><code>
     * // = "oh!"
     * Future.of(() -&gt; { throw new Error("oh!"); }).recoverWith(x -&gt; Future.of(x::getMessage));
     * </code></pre>
     *
     * @param f A function which takes the exception of a failure and returns a new future.
     * @return A new Future.
     * @throws NullPointerException if {@code f} is null
     */
    default Future<T> recoverWith(Function<? super Throwable, ? extends Future<? extends T>> f) {
        Objects.requireNonNull(f, "f is null");
        final Promise<T> promise = Promise.make(executorService());
        onComplete(t -> {
            if (t.isFailure()) {
                Try.run(() -> f.apply(t.getCause()).onComplete(promise::complete)).onFailure(promise::failure);
            } else {
                promise.complete(t);
            }
        });
        return promise.future();
    }

    /**
     * Transforms this {@code Future}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    default <U> U transform(Function<? super Future<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    /**
     * Transforms the value of this {@code Future}, whether it is a success or a failure.
     *
     * @param f   A transformation
     * @param <U> Generic type of transformation {@code Try} result
     * @return A {@code Future} of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    default <U> Future<U> transformValue(Function<? super Try<T>, ? extends Try<? extends U>> f) {
        Objects.requireNonNull(f, "f is null");
        final Promise<U> promise = Promise.make(executorService());
        onComplete(t -> {
            Try.run(() -> promise.complete(f.apply(t))).onFailure(promise::failure);
        });
        return promise.future();
    }

    /**
     * Returns a tuple of this and that Future result.
     * <p>
     * If this Future failed the result contains this failure. Otherwise the result contains that failure or
     * a tuple of both successful Future results.
     *
     * @param that Another Future
     * @param <U>  Result type of {@code that}
     * @return A new Future that returns both Future results.
     * @throws NullPointerException if {@code that} is null
     */
    default <U> Future<Tuple2<T, U>> zip(Future<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        return zipWith(that, Tuple::of);
    }

    /**
     * Returns a this and that Future result combined using a given combinator function.
     * <p>
     * If this Future failed the result contains this failure. Otherwise the result contains that failure or
     * a combination of both successful Future results.
     *
     * @param that Another Future
     * @param combinator The combinator function
     * @param <U>  Result type of {@code that}
     * @param <R>  Result type of {@code f}
     * @return A new Future that returns both Future results.
     * @throws NullPointerException if {@code that} is null
     */
    @SuppressWarnings("unchecked")
    default <U, R> Future<R> zipWith(Future<? extends U> that, BiFunction<? super T, ? super U, ? extends R> combinator) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(combinator, "combinator is null");
        final Promise<R> promise = Promise.make(executorService());
        onComplete(res1 -> {
            if (res1.isFailure()) {
                promise.complete((Try.Failure<R>) res1);
            } else {
                that.onComplete(res2 -> {
                    final Try<R> result = res1.flatMap(t -> res2.map(u -> combinator.apply(t, u)));
                    promise.complete(result);
                });
            }
        });
        return promise.future();
    }

    // -- Value & Monad implementation

    default <U> Future<U> flatMap(Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return flatMapTry(mapper::apply);
    }

    default <U> Future<U> flatMapTry(CheckedFunction<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final Promise<U> promise = Promise.make(executorService());
        onComplete((Try<T> result) -> result.mapTry(mapper)
                .onSuccess(promise::completeWith)
                .onFailure(promise::failure)
        );
        return promise.future();
    }

    /**
     * Performs the given {@code action} asynchronously hence this Future result becomes available.
     * The {@code action} is not performed, if the result is a failure.
     *
     * @param action A {@code Consumer}
     */
    @Override
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        onComplete(result -> result.forEach(action));
    }

    /**
     * Returns the value of the future. Waits for the result if necessary.
     *
     * @return The value of this future.
     * @throws NoSuchElementException if the computation unexpectedly failed or was interrupted.
     */
    // DEV-NOTE: A NoSuchElementException is thrown instead of the exception of the underlying Failure in order to
    //           be conform to Value#get
    @Override
    default T get() {
        // is empty will block until result is available
        if (isEmpty()) {
            throw new NoSuchElementException("get on failed future");
        } else {
            return getValue().get().get();
        }
    }

    /**
     * Checks, if this future has a value.
     *
     * @return true, if this future succeeded with a value, false otherwise.
     */
    @Override
    default boolean isEmpty() {
        // does not need to be synchronized, wait() has to check the completed state again
        if (!isCompleted()) {
            await();
        }
        return getValue().get().isEmpty();
    }

    /**
     * A {@code Future} is single-valued.
     *
     * @return {@code true}
     */
    @Override
    default boolean isSingleValued() {
        return true;
    }

    @Override
    default Iterator<T> iterator() {
        return isEmpty() ? Iterator.empty() : Iterator.of(get());
    }

    @Override
    default <U> Future<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return transformValue(t -> t.map(mapper::apply));
    }

    default <U> Future<U> mapTry(CheckedFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return transformValue(t -> t.mapTry(mapper::apply));
    }

    default Future<T> orElse(Future<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        final Promise<T> promise = Promise.make(executorService());
        onComplete(result -> {
            if (result.isSuccess()) {
                promise.complete(result);
            } else {
                other.onComplete(promise::complete);
            }
        });
        return promise.future();
    }

    default Future<T> orElse(Supplier<? extends Future<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        final Promise<T> promise = Promise.make(executorService());
        onComplete(result -> {
            if (result.isSuccess()) {
                promise.complete(result);
            } else {
                supplier.get().onComplete(promise::complete);
            }
        });
        return promise.future();
    }

    @Override
    default Future<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        onSuccess(action::accept);
        return this;
    }

    @Override
    default String stringPrefix() {
        return "Future";
    }
}
