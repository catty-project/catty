package pink.catty.extension.cluster;

import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionType.ClusterType;
import pink.catty.core.extension.spi.AbstractCluster;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.frame.DefaultResponse;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;

@Extension(ClusterType.FAIL_SAFE)
public class FailSafeCluster extends AbstractCluster {

  @Override
  public Response onError(Consumer invoker, Consumer failedConsumer, Request request,
      RuntimeException e) {
    logger.error("FailSafeCluster, meta: {}", failedConsumer.getMeta(), e);
    Response response = new DefaultResponse(request.getRequestId());
    response.setValue(null);
    return response;
  }
}
