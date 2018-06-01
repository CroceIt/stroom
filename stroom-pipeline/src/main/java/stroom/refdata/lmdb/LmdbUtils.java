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

package stroom.refdata.lmdb;

import com.google.common.collect.ImmutableMap;
import org.lmdbjava.CursorIterator;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.EnvInfo;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Stat;
import org.lmdbjava.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.refdata.lmdb.serde.Serde;
import stroom.refdata.offheapstore.ByteArrayUtils;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class of static utility methods for working with lmdbjava
 */
public class LmdbUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LmdbUtils.class);
    private static final LambdaLogger LAMBDA_LOGGER = LambdaLoggerFactory.getLogger(LmdbUtils.class);

    private LmdbUtils() {
        // only static util methods
    }

    /**
     * Do work inside a read txn, returning a result of the work
     */
    public static <T> T getWithReadTxn(final Env<ByteBuffer> env, final Function<Txn<ByteBuffer>, T> work) {
        try (final Txn<ByteBuffer> txn = env.txnRead()) {
            return work.apply(txn);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error performing work in read transaction", e);
        }
    }

    /**
     * Do work inside a read txn then commit
     */
    public static void doWithReadTxn(final Env<ByteBuffer> env, Consumer<Txn<ByteBuffer>> work) {
        try (final Txn<ByteBuffer> txn = env.txnRead()) {
            work.accept(txn);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error performing work in read transaction", e);
        }
    }

    /**
     * Do work inside a write txn then commit, return a result of the work
     */
    public static <T> T getWithWriteTxn(final Env<ByteBuffer> env, Function<Txn<ByteBuffer>, T> work) {
        try (final Txn<ByteBuffer> txn = env.txnWrite()) {
            T result = work.apply(txn);
            txn.commit();
            return result;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error performing work in write transaction", e);
        }
    }

    /**
     * Do work inside a write txn then commit. work should be as short lived as possible
     * to avoid tying up the single write txn for too long
     */
    public static void doWithWriteTxn(final Env<ByteBuffer> env, Consumer<Txn<ByteBuffer>> work) {
        try (final Txn<ByteBuffer> txn = env.txnWrite()) {
            work.accept(txn);
            txn.commit();
        } catch (RuntimeException e) {
            throw new RuntimeException("Error performing work in write transaction", e);
        }
    }

    public static Map<String, String> getDbInfo(final Env<ByteBuffer> env, Dbi<ByteBuffer> db) {
        return LmdbUtils.getWithReadTxn(env, txn -> {
            Stat stat = db.stat(txn);
            return convertStatToMap(stat);
        });
    }

    public static Map<String, String> getEnvInfo(final Env<ByteBuffer> env) {
        return LmdbUtils.getWithReadTxn(env, txn -> {
            Map<String, String> statMap = convertStatToMap(env.stat());
            Map<String, String> envInfo = convertEnvInfoToMap(env.info());

            String dbNames = env.getDbiNames().stream()
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .collect(Collectors.joining(","));

            return ImmutableMap.<String, String>builder()
                    .putAll(statMap)
                    .putAll(envInfo)
                    .put("maxKeySize", Integer.toString(env.getMaxKeySize()))
                    .put("dbNames", dbNames)
                    .build();
        });
    }

    public static ImmutableMap<String, String> convertStatToMap(final Stat stat) {
        return ImmutableMap.<String, String>builder()
                .put("pageSize", Integer.toString(stat.pageSize))
                .put("branchPages", Long.toString(stat.branchPages))
                .put("depth", Integer.toString(stat.depth))
                .put("entries", Long.toString(stat.entries))
                .put("leafPages", Long.toString(stat.leafPages))
                .put("overFlowPages", Long.toString(stat.overflowPages))
                .build();
    }

    public static ImmutableMap<String, String> convertEnvInfoToMap(final EnvInfo envInfo) {
        return ImmutableMap.<String, String>builder()
                .put("maxReaders", Integer.toString(envInfo.maxReaders))
                .put("numReaders", Integer.toString(envInfo.numReaders))
                .put("lastPageNumber", Long.toString(envInfo.lastPageNumber))
                .put("lastTransactionId", Long.toString(envInfo.lastTransactionId))
                .put("mapAddress", Long.toString(envInfo.mapAddress))
                .put("mapSize", Long.toString(envInfo.mapSize))
                .build();
    }

    public static long getEntryCount(final Env<ByteBuffer> env, final Txn<ByteBuffer> txn, final Dbi<ByteBuffer> dbi) {

        return dbi.stat(txn).entries;
    }

    public static long getEntryCount(final Env<ByteBuffer> env, final Dbi<ByteBuffer> dbi) {

        return LmdbUtils.getWithReadTxn(env, txn ->
                getEntryCount(env, txn, dbi));
    }

    public static void dumpBuffer(final ByteBuffer byteBuffer, final String description) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = byteBuffer.position(); i < byteBuffer.limit(); i++) {
            byte b = byteBuffer.get(i);
            stringBuilder.append(byteToHex(b));
            stringBuilder.append(" ");
        }
        LOGGER.info("{} byteBuffer: {}", description, stringBuilder.toString());
    }

    public static String byteBufferToHex(final ByteBuffer byteBuffer) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = byteBuffer.position(); i < byteBuffer.limit(); i++) {
            byte b = byteBuffer.get(i);
            stringBuilder.append(byteToHex(b));
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * Converts a byte array into a hex representation with a space between each
     * byte e.g 00 00 01 00 05 59 B3
     *
     * @param arr The byte array to convert
     * @return The byte array as a string of hex values separated by a spaces
     */
    public static String byteArrayToHex(final byte[] arr) {
        final StringBuilder sb = new StringBuilder();
        if (arr != null) {
            for (final byte b : arr) {
                sb.append(byteToHex(b));
                sb.append(" ");
            }
        }
        return sb.toString().replaceAll(" $", "");
    }

    public static String byteToHex(final byte b) {
        final byte[] oneByteArr = new byte[1];
        oneByteArr[0] = b;
        return DatatypeConverter.printHexBinary(oneByteArr);

    }

    /**
     * Allocates a new direct {@link ByteBuffer} with a size equal to the max key size for the LMDB environment and
     * serialises the object into that {@link ByteBuffer}
     * @return The newly allocated {@link ByteBuffer}
     */
    public static <T> ByteBuffer buildDbKeyBuffer(final Env<ByteBuffer> lmdbEnv, final T keyObject, Serde<T> keySerde) {
        try {
            return buildDbBuffer(keyObject, keySerde, lmdbEnv.getMaxKeySize());
        } catch (BufferOverflowException e) {
            throw new RuntimeException(LambdaLogger.buildMessage(
                    "The serialised form of keyObject {} is too big for an LMDB key, max bytes is {}",
                    keyObject, lmdbEnv.getMaxKeySize()), e);
        }
    }

    /**
     * Create an empty {@link ByteBuffer} with the appropriate capacity for an LMDB key
     */
    public static ByteBuffer createEmptyKeyBuffer(final Env<ByteBuffer> lmdbEnv) {
        return ByteBuffer.allocateDirect(lmdbEnv.getMaxKeySize());
    }

    /**
     * Allocates a new direct {@link ByteBuffer} (of size bufferSize) and
     * serialises the object into that {@link ByteBuffer}
     * @return The newly allocated {@link ByteBuffer}
     */
    public static <T> ByteBuffer buildDbBuffer(final T object, final Serde<T> serde, final int bufferSize) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        serde.serialize(byteBuffer, object);
        return byteBuffer;
    }

    /**
     * Creates a new direct {@link ByteBuffer} from the input {@link ByteBuffer}.
     * The bytes from position() to limit() will be copied into a newly allocated
     * buffer. The new buffer will be flipped to set its position read for get operations
     */
    public static ByteBuffer copyDirectBuffer(final ByteBuffer input) {
        ByteBuffer output = ByteBuffer.allocateDirect(input.remaining());
        output.put(input);
        output.flip();
        return output;
    }

    /**
     * Dumps all entries in the database to a single logger entry with one line per database entry.
     * This could potentially return thousands of rows so is only intended for small scale use in
     * testing. Entries are returned in the order they are held in the DB, e.g. a-z (unless the DB
     * is configured with reverse keys). The keyToStringFunc and valueToStringFunc functions are
     * used to convert the keys/values into string form for output to the logger.
     */
    public static void logDatabaseContents(final Env<ByteBuffer> env,
                                           final Dbi<ByteBuffer> dbi,
                                           final Function<ByteBuffer, String> keyToStringFunc,
                                           final Function<ByteBuffer, String> valueToStringFunc) {

        final StringBuilder stringBuilder = new StringBuilder();
        doWithReadTxn(env, txn -> {
            stringBuilder.append(LambdaLogger.buildMessage("Dumping {} entries for database [{}]",
                    getEntryCount(env, txn, dbi), new String(dbi.getName())));

            // loop over all DB entries
            try (CursorIterator<ByteBuffer> cursorIterator = dbi.iterate(txn, KeyRange.all())) {
                while (cursorIterator.hasNext()) {
                    final CursorIterator.KeyVal<ByteBuffer> keyVal = cursorIterator.next();
                    stringBuilder.append(LambdaLogger.buildMessage("\n  key: [{}] - value [{}]",
                            keyToStringFunc.apply(keyVal.key()),
                            valueToStringFunc.apply(keyVal.val())));
                }
            }
        });
        LOGGER.debug(stringBuilder.toString());

    }

    /**
     * Dumps all entries in the database to a single logger entry with one line per database entry.
     * This could potentially return thousands of rows so is only intended for small scale use in
     * testing. Entries are returned in the order they are held in the DB, e.g. a-z (unless the DB
     * is configured with reverse keys). The keys/values are output as hex representations of the
     * byte values.
     */
    public static void logRawDatabaseContents(final Env<ByteBuffer> env, final Dbi<ByteBuffer> dbi) {
        logDatabaseContents(env, dbi, ByteArrayUtils::byteBufferToHex, ByteArrayUtils::byteBufferToHex);
    }
}
