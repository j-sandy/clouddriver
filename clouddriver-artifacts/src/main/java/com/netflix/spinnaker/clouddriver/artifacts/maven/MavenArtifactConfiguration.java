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

package com.netflix.spinnaker.clouddriver.artifacts.maven;

import com.netflix.spinnaker.credentials.CredentialsTypeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("artifacts.maven.enabled")
@EnableConfigurationProperties(MavenArtifactProviderProperties.class)
@RequiredArgsConstructor
@Slf4j
class MavenArtifactConfiguration {
  private final MavenArtifactProviderProperties mavenArtifactProviderProperties;

  @Bean
  public CredentialsTypeProperties<MavenArtifactCredentials, MavenArtifactAccount>
      mavenCredentialsProperties(OkHttpClient okHttpClient) {
    return CredentialsTypeProperties.<MavenArtifactCredentials, MavenArtifactAccount>builder()
        .type(MavenArtifactCredentials.CREDENTIALS_TYPE)
        .credentialsClass(MavenArtifactCredentials.class)
        .credentialsDefinitionClass(MavenArtifactAccount.class)
        .defaultCredentialsSource(mavenArtifactProviderProperties::getAccounts)
        .credentialsParser(
            a -> {
              try {
                return new MavenArtifactCredentials(a, okHttpClient);
              } catch (Exception e) {
                log.warn("Failure instantiating maven artifact account {}: ", a, e);
                return null;
              }
            })
        .build();
  }
}
