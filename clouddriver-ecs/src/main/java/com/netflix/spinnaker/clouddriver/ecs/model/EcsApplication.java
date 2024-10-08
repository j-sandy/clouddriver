/*
 * Copyright 2017 Lookout, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.clouddriver.ecs.model;

import com.netflix.spinnaker.clouddriver.model.Application;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class EcsApplication implements Application {

  private String name;
  Map<String, String> attributes;
  Map<String, Set<String>> clusterNames;
  Map<String, Set<String>> clusterNameMetadata;

  public EcsApplication(
      String name,
      Map<String, String> attributes,
      Map<String, Set<String>> clusterNames,
      Map<String, Set<String>> getClusterNameMetadata) {
    this.name = name;
    this.attributes = attributes;
    this.clusterNames = clusterNames;
    this.clusterNameMetadata = getClusterNameMetadata;
  }
}
