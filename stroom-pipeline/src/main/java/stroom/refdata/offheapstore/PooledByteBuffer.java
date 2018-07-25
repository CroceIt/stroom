/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package stroom.refdata.offheapstore;

import stroom.util.logging.LambdaLogger;

import java.nio.ByteBuffer;

/**
 * Wrapper for a {@link ByteBuffer} obtained from a {@link ByteBufferPool} that can be used
 * with a try with resources block as it implements {@link AutoCloseable}.
 */
public class PooledByteBuffer implements AutoCloseable {

    private final ByteBufferPool byteBufferPool;
    private ByteBuffer byteBuffer;

    PooledByteBuffer(final ByteBufferPool byteBufferPool,
                     final ByteBuffer byteBuffer) {
        this.byteBufferPool = byteBufferPool;
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        if (byteBuffer == null) {
            throw new RuntimeException(LambdaLogger.buildMessage("The byteBuffer has been returned to the pool"));
        }
        return byteBuffer;
    }

    public void release() {
        byteBufferPool.release(byteBuffer);
        byteBuffer = null;
    }

    @Override
    public void close() {
        release();
    }

    @Override
    public String toString() {
        return "PooledByteBuffer{" +
                "byteBuffer=" + ByteBufferUtils.byteBufferInfo(byteBuffer) +
                '}';
    }
}
