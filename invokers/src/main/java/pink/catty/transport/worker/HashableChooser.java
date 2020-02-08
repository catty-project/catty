package pink.catty.transport.worker;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;

public interface HashableChooser extends EventExecutorChooserFactory.EventExecutorChooser {

  EventExecutor next(int hash);

}
