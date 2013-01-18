/**
 * (c) Copyright 2012 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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

package org.kiji.schema.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.NavigableSet;

import org.junit.Test;

import org.kiji.schema.Kiji;
import org.kiji.schema.KijiAdmin;
import org.kiji.schema.KijiClientTest;
import org.kiji.schema.KijiDataRequest;
import org.kiji.schema.KijiRowData;
import org.kiji.schema.KijiRowScanner;
import org.kiji.schema.KijiTable;
import org.kiji.schema.KijiTableReader;
import org.kiji.schema.KijiTableReader.KijiScannerOptions;
import org.kiji.schema.KijiTableWriter;
import org.kiji.schema.layout.KijiTableLayout;
import org.kiji.schema.layout.KijiTableLayouts;

/** Tests the StripValueRowFilter. */
public class TestStripValueRowFilter extends KijiClientTest {
  /** Verifies that values has been stripped if the StripValueRowFilter has been applied. */
  @Test
  public void testStripValuesFilter() throws Exception {
    final Kiji kiji = getKiji();
    final KijiAdmin admin = kiji.getAdmin();

    final KijiTableLayout fooLayout =
        new KijiTableLayout(KijiTableLayouts.getLayout(KijiTableLayouts.FOO_TEST), null);
    admin.createTable("foo", fooLayout, false);

    final KijiTable table = kiji.openTable("foo");
    {
      final KijiTableWriter writer = table.openTableWriter();
      writer.put(table.getEntityId("me"), "info", "name", 1L, "me");
      writer.put(table.getEntityId("me"), "info", "name", 2L, "me-too");
      writer.close();
    }


    final KijiTableReader reader = table.openTableReader();
    final KijiDataRequest dataRequest = new KijiDataRequest()
        .addColumn(new KijiDataRequest.Column("info", "name").withMaxVersions(2));
    final KijiRowFilter rowFilter = new StripValueRowFilter();
    final KijiScannerOptions scannerOptions =
        new KijiScannerOptions()
        .setKijiRowFilter(rowFilter);
    final KijiRowScanner scanner =
        reader.getScanner(dataRequest, scannerOptions);

    for (KijiRowData row : scanner) {
      final NavigableSet<String> qualifiers = row.getQualifiers("info");
      assertEquals(1, qualifiers.size());
      assertTrue(qualifiers.contains("name"));

      // Ensure that we can use getTimestamps() to count.
      assertEquals(2, row.getTimestamps("info", "name").size());
      try {
        // Cell value is stripped, hence IOException on the wrong schema hash:
        row.getMostRecentValue("info", "name");
        fail("row.getMostRecentValue() did not throw IOException.");
      } catch (IOException ioe) {
        assertTrue(ioe.getMessage(),
            ioe.getMessage().contains(
                "Schema with hash 00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00 "
                + "not found in schema table."));
      }
    }

    scanner.close();
    reader.close();
  }
}
