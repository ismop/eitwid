package pl.ismop.web.client.dap;

import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.concurrent.Future;

public class TryFutures {

	private static final Seq<String> parentId = List.of("id");

	private static class Aggregate {

		private String id;

		private List<String> childrenIds;

		public List<String> getChildrenIds() {
			return childrenIds;
		}
	}

	static Future<Seq<Aggregate>> getAggregates(Seq<String> ids) {
		return Future.successful(List.empty());
	}

	static Future<Seq<Aggregate>> collectAggregates(Future<Seq<Aggregate>> aggregatesFuture) {
		return aggregatesFuture.flatMap(aggregates -> {
			Seq<String> allChildrenIds = aggregates.flatMap(
					aggregate -> aggregate.getChildrenIds());

			if (allChildrenIds.isEmpty()) {
				return aggregatesFuture;
			} else {
				return getAggregates(allChildrenIds).flatMap(
						result -> result.appendAll(aggregatesFuture.get()));
			}
		});
	}

	public static void main(String[] args) {
		Future<Seq<Aggregate>> allAggregatesFuture = collectAggregates(getAggregates(parentId));
		allAggregatesFuture.onComplete(aggregates -> aggregates.forEach(System.out::println));
	}
}
