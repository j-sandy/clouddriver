/*
 * Copyright 2018 Pivotal, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.cloudfoundry.deploy.ops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.spinnaker.clouddriver.cloudfoundry.client.CloudFoundryApiException;
import com.netflix.spinnaker.clouddriver.cloudfoundry.client.model.v3.ProcessStats;
import com.netflix.spinnaker.clouddriver.cloudfoundry.deploy.description.StopCloudFoundryServerGroupDescription;
import com.netflix.spinnaker.clouddriver.data.task.Task;
import com.netflix.spinnaker.clouddriver.helpers.OperationPoller;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StopCloudFoundryServerGroupAtomicOperationTest
    extends AbstractCloudFoundryAtomicOperationTest {
  private StopCloudFoundryServerGroupDescription desc =
      new StopCloudFoundryServerGroupDescription();

  StopCloudFoundryServerGroupAtomicOperationTest() {
    super();
  }

  @BeforeEach
  void before() {
    desc.setClient(client);
    desc.setServerGroupName("myapp");
  }

  @Test
  void stop() {
    OperationPoller poller = mock(OperationPoller.class);

    //noinspection unchecked
    when(poller.waitForOperation(any(Supplier.class), any(), any(), any(), any(), any()))
        .thenReturn(ProcessStats.State.DOWN);

    StopCloudFoundryServerGroupAtomicOperation op =
        new StopCloudFoundryServerGroupAtomicOperation(poller, desc);

    assertThat(runOperation(op).getHistory())
        .has(status("Stopping 'myapp'"), atIndex(1))
        .has(status("Stopped 'myapp'"), atIndex(2));
  }

  @Test
  void failedToStop() {
    OperationPoller poller = mock(OperationPoller.class);

    //noinspection unchecked
    when(poller.waitForOperation(any(Supplier.class), any(), any(), any(), any(), any()))
        .thenReturn(ProcessStats.State.RUNNING);

    StopCloudFoundryServerGroupAtomicOperation op =
        new StopCloudFoundryServerGroupAtomicOperation(poller, desc);

    Task task = runOperation(op);
    List<Object> resultObjects = task.getResultObjects();
    assertThat(resultObjects.size()).isEqualTo(1);
    Object o = resultObjects.get(0);
    assertThat(o).isInstanceOf(Map.class);
    Object ex = ((Map) o).get("EXCEPTION");
    assertThat(ex).isInstanceOf(CloudFoundryApiException.class);
    assertThat(((CloudFoundryApiException) ex).getMessage())
        .isEqualTo(
            "Cloud Foundry API returned with error(s): Failed to stop 'myapp' which instead is running");
  }

  @Test
  void failedToStopStopping() {
    OperationPoller poller = mock(OperationPoller.class);

    //noinspection unchecked
    when(poller.waitForOperation(any(Supplier.class), any(), any(), any(), any(), any()))
        .thenReturn(ProcessStats.State.STOPPING);

    StopCloudFoundryServerGroupAtomicOperation op =
        new StopCloudFoundryServerGroupAtomicOperation(poller, desc);

    Task task = runOperation(op);
    List<Object> resultObjects = task.getResultObjects();
    assertThat(resultObjects.size()).isEqualTo(1);
    Object o = resultObjects.get(0);
    assertThat(o).isInstanceOf(Map.class);
    Object ex = ((Map) o).get("EXCEPTION");
    assertThat(ex).isInstanceOf(CloudFoundryApiException.class);
    assertThat(((CloudFoundryApiException) ex).getMessage())
        .isEqualTo(
            "Cloud Foundry API returned with error(s): Failed to stop 'myapp' which instead is in graceful shutdown - stopping");
  }
}
