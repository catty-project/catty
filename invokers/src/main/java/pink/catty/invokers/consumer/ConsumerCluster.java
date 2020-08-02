package pink.catty.invokers.consumer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.CattyException;
import pink.catty.core.extension.spi.Cluster;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.invoker.AbstractConsumer;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ConsumerMeta;

public class ConsumerCluster extends AbstractConsumer {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerCluster.class);

  private static final String FAILED_INVOKER = "$FAILED_INVOKER$";

  private final Cluster cluster;
  private final LoadBalance loadBalance;
  private final ConsumerMeta consumerMeta;

  public ConsumerCluster(ConsumerMeta consumerMeta, Cluster cluster, LoadBalance loadBalance) {
    super(null);
    this.consumerMeta = consumerMeta;
    this.cluster = cluster;
    this.loadBalance = loadBalance;
  }

  @Override
  public ConsumerMeta getMeta() {
    return this.consumerMeta;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response invoke(Request request) {
    Object failedInvokers = request.getAttribute(FAILED_INVOKER);
    if (failedInvokers == null) {
      failedInvokers = new HashSet<Consumer>();
      request.addAttribute(FAILED_INVOKER, failedInvokers);
    }

    List<Consumer> candidates = new LinkedList<>();
    for (Consumer consumer : cluster.listConsumer()) {
      if (!((HashSet<Consumer>) failedInvokers).contains(consumer)) {
        candidates.add(consumer);
      }
    }

    // check if there is no candidate.
    if (candidates.size() == 0) {
      logger.error("ConsumerCluster, no valid endpoint. meta: {}", getMeta());
      throw new CattyException(
          "ConsumerCluster, no valid endpoint. meta: " + getMeta());
    }

    // select one consumer from candidates.
    Consumer consumer = loadBalance.select(candidates);

    Response response;
    try {
      response = consumer.invoke(request);
    } catch (RuntimeException e) {
      ((HashSet<Consumer>) failedInvokers).add(consumer);
      response = cluster.onError(this, consumer, request, e);
    }

    return response;
  }
}
