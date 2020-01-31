package io.catty.core.utils;

import io.catty.core.DefaultResponse;
import io.catty.core.Response;
import io.catty.core.Response.ResponseEntity;
import io.catty.core.Response.ResponseStatus;
import java.util.concurrent.CompletionStage;

public abstract class AsyncUtils {

  public static Response newResponse(CompletionStage<Object> completionStage,
      long requestId) {
    Response response = new DefaultResponse(requestId);
    completionStage.whenComplete((value, throwable) -> {
      if (value instanceof ResponseEntity) {
        response.setResponseEntity((ResponseEntity) value);
      } else {
        if (throwable == null) {
          response.setResponseEntity(ResponseEntity.Of(ResponseStatus.OK, value));
        } else {
          // todo:
        }
      }
    });
    return response;
  }

}
