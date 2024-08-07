/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.controller.api;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.pinot.controller.helix.ControllerTest;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.config.table.TableType;
import org.apache.pinot.spi.utils.builder.TableConfigBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 * Tests for the file upload restlet.
 */
public class PinotFileUploadTest {
  private static final ControllerTest TEST_INSTANCE = ControllerTest.getInstance();
  private static final String TABLE_NAME = "fileTable";

  @BeforeClass
  public void setUp()
      throws Exception {
    TEST_INSTANCE.setupSharedStateAndValidate();

    // Create schema
    TEST_INSTANCE.addDummySchema(TABLE_NAME);
    // Adding table
    TableConfig tableConfig = new TableConfigBuilder(TableType.OFFLINE).setTableName(TABLE_NAME)
        .setSegmentAssignmentStrategy("RandomAssignmentStrategy").setNumReplicas(2).build();
    TEST_INSTANCE.getHelixResourceManager().addTable(tableConfig);
  }

  @Test
  public void testUploadBogusData()
      throws Exception {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(TEST_INSTANCE.getControllerRequestURLBuilder().forDataFileUpload());
      HttpEntity entity = new StringEntity("blah");
      httpPost.setEntity(entity);
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        int statusCode = response.getCode();

        assertTrue(statusCode >= 400 && statusCode < 500, "Status code = " + statusCode);
      }
    }
  }

  @AfterClass
  public void tearDown() {
    TEST_INSTANCE.cleanup();
  }
}
