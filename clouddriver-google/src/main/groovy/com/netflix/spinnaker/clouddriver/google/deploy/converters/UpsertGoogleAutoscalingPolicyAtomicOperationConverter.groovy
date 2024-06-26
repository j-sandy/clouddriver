/*
 * Copyright 2016 Google, Inc.
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

package com.netflix.spinnaker.clouddriver.google.deploy.converters

import com.netflix.spinnaker.cats.cache.Cache
import com.netflix.spinnaker.clouddriver.google.GoogleOperation
import com.netflix.spinnaker.clouddriver.google.deploy.GoogleOperationPoller
import com.netflix.spinnaker.clouddriver.google.deploy.description.UpsertGoogleAutoscalingPolicyDescription
import com.netflix.spinnaker.clouddriver.google.deploy.ops.UpsertGoogleAutoscalingPolicyAtomicOperation
import com.netflix.spinnaker.clouddriver.google.provider.view.GoogleClusterProvider
import com.netflix.spinnaker.clouddriver.google.security.GoogleNamedAccountCredentials
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperationsRegistry
import com.netflix.spinnaker.clouddriver.orchestration.OrchestrationProcessor
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsConverter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@GoogleOperation(AtomicOperations.UPSERT_SCALING_POLICY)
@Component("upsertGoogleScalingPolicyDescription")
class UpsertGoogleAutoscalingPolicyAtomicOperationConverter extends AbstractAtomicOperationsCredentialsConverter<GoogleNamedAccountCredentials>{

  @Autowired
  GoogleClusterProvider googleClusterProvider

  @Autowired
  GoogleOperationPoller googleOperationPoller

  @Autowired
  AtomicOperationsRegistry atomicOperationsRegistry

  @Autowired
  OrchestrationProcessor orchestrationProcessor

  @Autowired
  Cache cacheView

  @Autowired
  ObjectMapper objectMapper

  @Override
  AtomicOperation convertOperation(Map input) {
    new UpsertGoogleAutoscalingPolicyAtomicOperation(convertDescription(input), googleClusterProvider, googleOperationPoller, atomicOperationsRegistry, orchestrationProcessor, cacheView, objectMapper)
  }

  @Override
  UpsertGoogleAutoscalingPolicyDescription convertDescription(Map input) {
    GoogleAtomicOperationConverterHelper.convertDescription(input, this, UpsertGoogleAutoscalingPolicyDescription)
  }
}
