/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.consul.api.v1

import com.jakewharton.retrofit.Ok3Client
import com.netflix.spinnaker.clouddriver.consul.config.ConsulConfig
import com.netflix.spinnaker.clouddriver.consul.config.ConsulProperties
import com.netflix.spinnaker.kork.retrofit.exceptions.SpinnakerRetrofitErrorHandler
import okhttp3.OkHttpClient
import retrofit.RestAdapter
import retrofit.converter.JacksonConverter

class Consul<T> {
  T api
  String endpoint
  Long timeout

  Consul(ConsulConfig config, Class<T> type) {
    this(config.agentEndpoint, config.agentPort, ConsulProperties.DEFAULT_TIMEOUT_MILLIS, type)
  }

  Consul(String endpoint, Integer port, Long timeout, Class<T> type) {
    this.endpoint = "http://${endpoint}:${port}"
    this.timeout = timeout
    this.api = new RestAdapter.Builder()
      .setEndpoint(this.endpoint)
      .setClient(new Ok3Client(new OkHttpClient()))
      .setConverter(new JacksonConverter())
      .setLogLevel(RestAdapter.LogLevel.NONE)
      .setErrorHandler(SpinnakerRetrofitErrorHandler.getInstance())
      .build()
      .create(type)
  }
}
