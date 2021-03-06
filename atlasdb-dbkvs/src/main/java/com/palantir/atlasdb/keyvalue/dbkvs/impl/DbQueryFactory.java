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
package com.palantir.atlasdb.keyvalue.dbkvs.impl;

import java.util.Collection;
import java.util.Map;

import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.keyvalue.api.ColumnSelection;
import com.palantir.atlasdb.keyvalue.api.RangeRequest;

public interface DbQueryFactory {
    FullQuery getLatestRowQuery(byte[] row, long ts, ColumnSelection columns, boolean includeValue);
    FullQuery getLatestRowsQuery(Iterable<byte[]> rows, long ts, ColumnSelection columns, boolean includeValue);
    FullQuery getLatestRowsQuery(Collection<Map.Entry<byte[], Long>> rows, ColumnSelection columns, boolean includeValue);

    FullQuery getAllRowQuery(byte[] row, long ts, ColumnSelection columns, boolean includeValue);
    FullQuery getAllRowsQuery(Iterable<byte[]> rows, long ts, ColumnSelection columns, boolean includeValue);
    FullQuery getAllRowsQuery(Collection<Map.Entry<byte[], Long>> rows, ColumnSelection columns, boolean includeValue);

    FullQuery getLatestCellQuery(Cell cell, long ts, boolean includeValue);
    FullQuery getLatestCellsQuery(Iterable<Cell> cells, long ts, boolean includeValue);
    FullQuery getLatestCellsQuery(Collection<Map.Entry<Cell, Long>> cells, boolean includeValue);

    FullQuery getAllCellQuery(Cell cell, long ts, boolean includeValue);
    FullQuery getAllCellsQuery(Iterable<Cell> cells, long ts, boolean includeValue);
    FullQuery getAllCellsQuery(Collection<Map.Entry<Cell, Long>> cells, boolean includeValue);

    FullQuery getRangeQuery(RangeRequest range, long ts, int maxRows);
    boolean hasOverflowValues();
    Collection<FullQuery> getOverflowQueries(Collection<OverflowValue> overflowIds);
}
