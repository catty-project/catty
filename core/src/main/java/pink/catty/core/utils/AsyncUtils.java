package pink.catty.core.utils;

import pink.catty.core.DefaultResponse;
import pink.catty.core.Response;
import java.util.concurrent.CompletionStage;

public abstract class AsyncUtils {

  public static Response newResponse(CompletionStage<Object> completionStage, long requestId) {
    Response response = new DefaultResponse(requestId);
    completionStage.whenComplete((value, throwable) -> {
      if (throwable == null) {
        response.setValue(value);
      } else {
        response.setValue(throwable);
      }
    });
    return response;
  }

}
