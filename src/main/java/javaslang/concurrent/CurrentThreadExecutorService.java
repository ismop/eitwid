package javaslang.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CurrentThreadExecutorService implements ExecutorService {

	private boolean shutdown = false;

	private static class SimpleFuture<V> implements Future<V> {

		private V result;

		public SimpleFuture(V result) {
			this.result = result;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return result;
		}

		@Override
		public V get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return result;
		}
	}

	@Override
	public void execute(Runnable command) {
		command.run();
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		return new ArrayList<>();
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public boolean isTerminated() {
		return shutdown;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return true;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		T result = null;

		try {
			result = task.call();
		} catch (Exception e) {}

		return new SimpleFuture<>(result);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return new SimpleFuture<>(result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return new SimpleFuture<>(null);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		List<Future<T>> result = new ArrayList<>();
		tasks.forEach(task -> {
					try {
						result.add(new SimpleFuture<>(task.call()));
					} catch (Exception e) {}
				});

		return result;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
			TimeUnit unit) throws InterruptedException {
		return invokeAll(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		try {
			return tasks.iterator().next().call();
		} catch (Exception e) {}

		return null;
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return invokeAny(tasks);
	}

}
