/*
 * Copyright 2017 Crown Copyright
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
 */

package stroom.search.extraction;

import stroom.dashboard.expression.v1.Val;
import stroom.index.shared.IndexConstants;
import stroom.meta.shared.Meta;
import stroom.meta.api.MetaService;
import stroom.search.coprocessor.Values;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class StreamMapCreator {
    private static final LambdaLogger LOGGER = LambdaLoggerFactory.getLogger(StreamMapCreator.class);

    private final MetaService metaService;

    private final int streamIdIndex;
    private final int eventIdIndex;

    private Map<Long, Optional<Object>> fiteredStreamCache;
    private ExtractionException error;

    StreamMapCreator(final String[] storedFields,
                     final MetaService metaService) {
        this.metaService = metaService;

        // First get the index in the stored data of the stream and event id fields.
        streamIdIndex = getFieldIndex(storedFields, IndexConstants.STREAM_ID);
        eventIdIndex = getFieldIndex(storedFields, IndexConstants.EVENT_ID);
    }

    private int getFieldIndex(final String[] storedFields, final String fieldName) {
        int index = -1;

        for (int i = 0; i < storedFields.length && index == -1; i++) {
            final String storedField = storedFields[i];
            if (storedField.equals(fieldName)) {
                index = i;
            }
        }

        if (index == -1 && error == null) {
            error = new ExtractionException("The " + fieldName + " has not been stored in this index");
        }

        return index;
    }

    void addEvent(final Map<Long, List<Event>> storedDataMap, final Val[] storedData) {
        if (error != null) {
            throw error;
        } else {
            final long longStreamId = getLong(storedData, streamIdIndex);
            final long longEventId = getLong(storedData, eventIdIndex);
            final Values data = getData(longStreamId, longEventId, storedData);

            final Event event = new Event(longStreamId, longEventId, data);
            storedDataMap.compute(longStreamId, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(event);
                return v;
            });
        }
    }

    private Values getData(final long longStreamId, final long longEventId, final Val[] storedData) {
        if (longStreamId != -1 && longEventId != -1) {
            // Create a map to cache stream lookups. If we have cached more than a million streams then discard the map and start again to avoid using too much memory.
            if (fiteredStreamCache == null || fiteredStreamCache.size() > 1000000) {
                fiteredStreamCache = new HashMap<>();
            }

            final Optional<Object> optional = fiteredStreamCache.computeIfAbsent(longStreamId, k -> {
                try {
                    // See if we can load the stream. We might get a StreamPermissionException if we aren't allowed to read from this stream.
                    return Optional.ofNullable(metaService.getMeta(k));
//                } catch (final StreamPermissionException e) {
//                    LOGGER.debug(e::getMessage, e);
//                    return Optional.of(e);
                } catch (final RuntimeException e) {
                    LOGGER.error(e::getMessage, e);
                    return Optional.of(e);
                }
            });

            if (!optional.isPresent()) {
                //Meta record not found - stream deleted.
                return new Values(null);
            }

            final Object cached = optional.get();
            if (cached instanceof Throwable) {
                final Throwable t = (Throwable) cached;
                throw new ExtractionException(t.getMessage(), t);
            } else if (cached instanceof Meta) {
                return new Values(storedData);
            }
            throw new ExtractionException("Unexpected cached type " + cached.getClass().getSimpleName());
        }

        throw new ExtractionException("No event id supplied");
    }

    private long getLong(final Val[] storedData, final int index) {
        try {
            if (index >= 0 && storedData.length > index) {
                final Val value = storedData[index];
                return value.toLong();
            }
        } catch (final Exception e) {
            // Ignore
        }

        return -1;
    }
}
