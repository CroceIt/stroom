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

import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.Map;

public interface LmdbDb {

    String getDbName();

    Map<String, String> getDbInfo();

    long getEntryCount(Txn<ByteBuffer> txn);

    long getEntryCount();

    void logDatabaseContents(Txn<ByteBuffer> txn);

    void logDatabaseContents();

    void logRawDatabaseContents(Txn<ByteBuffer> txn);

    void logRawDatabaseContents();
}
