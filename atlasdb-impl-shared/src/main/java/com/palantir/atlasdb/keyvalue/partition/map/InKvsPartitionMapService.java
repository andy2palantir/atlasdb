/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.partition.map;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.palantir.atlasdb.AtlasDbConstants;
import com.palantir.atlasdb.encoding.PtBytes;
import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.keyvalue.api.KeyValueService;
import com.palantir.atlasdb.keyvalue.api.Value;
import com.palantir.atlasdb.keyvalue.impl.InMemoryKeyValueService;
import com.palantir.atlasdb.keyvalue.partition.api.DynamicPartitionMap;
import com.palantir.common.base.Throwables;

/**
 * Only instances of {@code DynamicPartitionMapImpl} are supported by this implementation!
 *
 * @author htarasiuk
 *
 */
public final class InKvsPartitionMapService implements PartitionMapService {

    private final KeyValueService storage;

    public static final String PARTITION_MAP_TABLE = AtlasDbConstants.PARTITION_MAP_TABLE;
    public static final Cell PARTITION_MAP_CELL  = Cell.create(PtBytes.toBytes("map"), PtBytes.toBytes("map"));

    private InKvsPartitionMapService(DynamicPartitionMapImpl partitionMap, KeyValueService storage) {
    	this.storage = storage;
    	storage.createTable(PARTITION_MAP_TABLE, AtlasDbConstants.GENERIC_TABLE_METADATA);
    	if (partitionMap != null) {
    	    updateMap(partitionMap);
    	}
    }

    private InKvsPartitionMapService(KeyValueService storage) {
        this(null, storage);
    }

    /**
     * This can be used if the partition map has been previously stored to {@code storage}
     * or if you want to first create an empty service.
     *
     * @param storage
     * @return
     */
    public static InKvsPartitionMapService create(KeyValueService storage) {
        return new InKvsPartitionMapService(storage);
    }

    /**
     * This will put the {@code partitionMap} to {@code storage} overwriting any previous
     * values stored in the {@link #PARTITION_MAP_TABLE}.
     *
     * @param storage
     * @param partitionMap
     * @return
     */
    public static InKvsPartitionMapService create(KeyValueService storage, DynamicPartitionMapImpl partitionMap) {
        Preconditions.checkNotNull(partitionMap);
        return new InKvsPartitionMapService(partitionMap, storage);
    }

    /**
     * This will use a newly created InMemoryKeyValueService to store the map.
     *
     * @param partitionMap
     * @return
     */
    public static InKvsPartitionMapService createInMemory(DynamicPartitionMapImpl partitionMap) {
        Preconditions.checkNotNull(partitionMap);
        return new InKvsPartitionMapService(partitionMap, new InMemoryKeyValueService(false));
    }

    /**
     * This will use a newly created InMemoryKeyValueService to store the map.
     *
     * @return
     */
    public static InKvsPartitionMapService createEmptyInMemory() {
        return new InKvsPartitionMapService(new InMemoryKeyValueService(false));
    }

    @Override
    public synchronized DynamicPartitionMapImpl getMap() {
        Map<Cell, Value> result = storage.get(PARTITION_MAP_TABLE, ImmutableMap.of(PARTITION_MAP_CELL, Long.MAX_VALUE));
        if (result.isEmpty()) {
            return null;
        }
        Value value = result.values().iterator().next();
        try {
            DynamicPartitionMap map = new ObjectMapper().readValue(value.getContents(), DynamicPartitionMap.class);
            return (DynamicPartitionMapImpl) map;
        } catch (IOException e) {
            throw Throwables.throwUncheckedException(e);
        }
    }

    @Override
    public synchronized long getMapVersion() {
        DynamicPartitionMapImpl map = getMap();
        if (map == null) {
            return -1;
        }
        return map.getVersion();
    }

    /**
     * Only instances of {@code DynamicPartitionMapImpl} are supported by this implementation.
     *
     * @param partitionMap Must be instance of {@code DynamicPartitionMapImpl}.
     */
    @Override
    public synchronized void updateMap(DynamicPartitionMap partitionMap) {
        if (!(partitionMap instanceof DynamicPartitionMapImpl)) {
            throw new IllegalArgumentException("Only instances of DynamicPartitionMapImpl are supported!");
        }

        DynamicPartitionMapImpl dpmi = (DynamicPartitionMapImpl) partitionMap;
        try {
            byte[] bytes = new ObjectMapper().writeValueAsBytes(dpmi);
            storage.put(PARTITION_MAP_TABLE, ImmutableMap.of(PARTITION_MAP_CELL, bytes), dpmi.getVersion());
        } catch (JsonProcessingException e) {
            throw Throwables.throwUncheckedException(e);
        }
    }

    /**
     * TODO: This can be done more efficient if needed.
     */
    @Override
    public synchronized long updateMapIfNewer(DynamicPartitionMap partitionMap) {
        final long originalVersion = getMapVersion();

        if (partitionMap.getVersion() > originalVersion) {
            updateMap(partitionMap);
        }

        return originalVersion;
    }

}
