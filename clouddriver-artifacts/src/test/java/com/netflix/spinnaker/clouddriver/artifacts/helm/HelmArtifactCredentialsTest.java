/*
 * Copyright 2019 Google, Inc.
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

package com.netflix.spinnaker.clouddriver.artifacts.helm;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import com.netflix.spinnaker.kork.web.exceptions.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Function;
import okhttp3.OkHttpClient;
import org.apache.commons.io.Charsets;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import ru.lanwen.wiremock.ext.WiremockResolver;

@ExtendWith({WiremockResolver.class, TempDirectory.class})
class HelmArtifactCredentialsTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final OkHttpClient okHttpClient = new OkHttpClient();

  private final String REPOSITORY = "my-repository";
  private final String CHART_PATH = "/my-chart/data.tgz";
  private final String CHART_NAME = "my-chart";
  private final String CHART_VERSION = "1.0.0";
  private final String FILE_CONTENTS = "file contents";

  @Test
  void downloadWithBasicAuth(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
    HelmArtifactAccount account =
        HelmArtifactAccount.builder()
            .repository(server.baseUrl() + "/" + REPOSITORY)
            .name("my-helm-account")
            .username("user")
            .password("passw0rd")
            .build();

    runTestCase(server, account, m -> m.withBasicAuth("user", "passw0rd"));
  }

  @Test
  void downloadWithBasicAuthFromFile(
      @TempDirectory.TempDir Path tempDir, @WiremockResolver.Wiremock WireMockServer server)
      throws IOException {
    Path authFile = tempDir.resolve("auth-file");
    Files.write(authFile, "someuser:somepassw0rd!".getBytes());

    HelmArtifactAccount account =
        HelmArtifactAccount.builder()
            .repository(server.baseUrl() + "/" + REPOSITORY)
            .name("my-helm-account")
            .usernamePasswordFile(authFile.toAbsolutePath().toString())
            .build();

    runTestCase(server, account, m -> m.withBasicAuth("someuser", "somepassw0rd!"));
  }

  @Test
  void downloadWithNoAuth(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
    HelmArtifactAccount account =
        HelmArtifactAccount.builder()
            .repository(server.baseUrl() + "/" + REPOSITORY)
            .name("my-helm-account")
            .build();

    runTestCase(server, account, m -> m.withHeader("Authorization", absent()));
  }

  @Test
  void getArtifactNamesWithFailure(@WiremockResolver.Wiremock WireMockServer server)
      throws IOException {
    HelmArtifactAccount account =
        HelmArtifactAccount.builder()
            .repository(server.baseUrl() + "/" + REPOSITORY)
            .name("my-helm-account")
            .build();

    runGetArtifactNamesWithFailureTestCase(
        server, account, m -> m.withHeader("Authorization", absent()));
  }

  private void runGetArtifactNamesWithFailureTestCase(
      WireMockServer server,
      HelmArtifactAccount account,
      Function<MappingBuilder, MappingBuilder> expectedAuth) {
    HelmArtifactCredentials credentials = new HelmArtifactCredentials(account, okHttpClient);

    final String indexPath = "/" + REPOSITORY + "/index.yaml";

    server.stubFor(
        expectedAuth.apply(
            any(urlPathEqualTo(indexPath))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))));

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(credentials::getArtifactNames)
        .has(
            new Condition<>(
                e -> e.getCause() != null && e.getCause().getCause() != null, "innerException"));
    assertThat(server.findUnmatchedRequests().getRequests()).isEmpty();
  }

  private void runTestCase(
      WireMockServer server,
      HelmArtifactAccount account,
      Function<MappingBuilder, MappingBuilder> expectedAuth)
      throws IOException {
    HelmArtifactCredentials credentials = new HelmArtifactCredentials(account, okHttpClient);

    Artifact artifact =
        Artifact.builder().name(CHART_NAME).version(CHART_VERSION).type("helm/chart").build();

    prepareServer(server, expectedAuth);

    assertThat(credentials.download(artifact))
        .hasSameContentAs(new ByteArrayInputStream(FILE_CONTENTS.getBytes(Charsets.UTF_8)));
    assertThat(server.findUnmatchedRequests().getRequests()).isEmpty();
  }

  private void prepareServer(
      WireMockServer server, Function<MappingBuilder, MappingBuilder> withAuth) throws IOException {
    final String indexPath = "/" + REPOSITORY + "/index.yaml";
    IndexConfig indexConfig = getIndexConfig(server.baseUrl());

    server.stubFor(
        withAuth.apply(
            any(urlPathEqualTo(indexPath))
                .willReturn(aResponse().withBody(objectMapper.writeValueAsString(indexConfig)))));

    server.stubFor(
        withAuth.apply(
            any(urlPathEqualTo(CHART_PATH)).willReturn(aResponse().withBody(FILE_CONTENTS))));
  }

  private IndexConfig getIndexConfig(String baseUrl) {
    EntryConfig entryConfig = new EntryConfig();
    entryConfig.setName(CHART_NAME);
    entryConfig.setVersion(CHART_VERSION);
    entryConfig.setUrls(Collections.singletonList(baseUrl + CHART_PATH));

    IndexConfig indexConfig = new IndexConfig();
    indexConfig.setEntries(
        Collections.singletonMap("my-chart", Collections.singletonList(entryConfig)));

    return indexConfig;
  }
}
