package io.catty.core.utils;

import io.catty.core.DefaultResponse;
import io.catty.core.Response;
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
