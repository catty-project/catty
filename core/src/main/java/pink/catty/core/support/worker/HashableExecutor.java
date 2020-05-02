/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.support.worker;

import io.netty.util.concurrent.EventExecutorGroup;

/**
 * HashableExecutor is a extension of EventExecutorGroup.
 * HashableExecutor is able to dispatch tasks by hash number.
 * At the same time, HashableExecutor could be used as generic executor while it has
 * extended EventExecutorGroup.
 *
 * HashableExecutor is needed because if every requests just be submitted randomly to a generic
 * executor such as ThreadPollExecutor, then the a series of requests can't be able to be executed
 * by the origin order even if they are transformed by the same TCP link. And in some cases, this
 * will cause severe problem.
 *
 * To fix this problem, we introduce HashableExecutor.
 *
 * When you need keep order for a series of requests, you can invoke {@link
 * HashableExecutor#submit(int, Runnable)} and pass a same hash number as the first argument of
 * those tasks, as result, those requests will be executed by submitting order.
 *
 * If you use the hash feature to keep order, the requests will be executed by a same thread. In
 * some cases, it could cause performance problem.
 *
 * @see io.netty.util.concurrent.MultithreadEventExecutorGroup
 * @see HashLoopGroup
 */
public interface HashableExecutor extends EventExecutorGroup {

  void submit(int hash, Runnable task);

}
