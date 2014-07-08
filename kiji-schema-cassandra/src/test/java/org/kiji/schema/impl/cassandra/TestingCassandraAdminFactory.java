/**
 * (c) Copyright 2014 WibiData, Inc.
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

package org.kiji.schema.impl.cassandra;

import java.io.IOException;

import com.datastax.driver.core.Session;
import com.google.common.base.Preconditions;

import org.kiji.annotations.ApiAudience;
import org.kiji.schema.KijiURI;

/** Factory for CassandraAdmin that creates concrete CassandraAdmin instances. */
@ApiAudience.Private
public final class TestingCassandraAdminFactory implements CassandraAdminFactory {
  /** Singleton. */
  //private static final CassandraAdminFactory defaultFactory = new TestingCassandraAdminFactory();
  // TODO: Is this style okay for a Singleton that needs to be passed a parameter?
  private static TestingCassandraAdminFactory defaultFactory = null;

  /** @return an instance of the default factory. */
  public static CassandraAdminFactory get(Session session) {
    if (defaultFactory == null) {
      defaultFactory = new TestingCassandraAdminFactory(session);
    } else {
      assert(defaultFactory.mSession == session);
    }
    return defaultFactory;
  }

  /** @return an instance of the default factory. */
  public static CassandraAdminFactory get() {
    return defaultFactory;
  }

  /** Session to use for all admins generated by this factory. */
  protected Session mSession = null;

  private TestingCassandraAdminFactory(Session session) {
    this.mSession = session;
  }

  /** {@inheritDoc} */
  @Override
  public CassandraAdmin create(KijiURI uri) throws IOException {
    Preconditions.checkNotNull(mSession);
    return TestingCassandraAdmin.makeFromKijiURI(mSession, uri);
  }
}