/*
 * MultiProfileITCase.java
 *
 * Created on 2019-11-06, 7:29
 */
package com.marcnuri.fmpintegrationtests.generators.springboot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.marcnuri.fmpintegrationtests.maven.MavenUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-11-06.
 */
class MultiProfileITCase {

  private static final String PROJECT_MULTI_PROFILE = "generators/spring-boot/multi-profile";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper(new YAMLFactory());
  }

  @Test
  void fabric8Resource_defaultProfile_shouldCreateDefaultResources() throws Exception {
    // When
    final InvocationResult invocationResult = maven("fabric8:resource");
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    assertKubernetesPort(8081);
  }

  @Test
  void fabric8Resource_productionProfile_shouldCreateProductionResources() throws Exception{
    // When
    final InvocationResult invocationResult = maven("fabric8:resource", "Production");
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    assertKubernetesPort(8080);
  }

  @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
  private void assertKubernetesPort(int port) throws Exception {
    final File metaInfDirectory = new File(
        String.format("../%s/target/classes/META-INF", PROJECT_MULTI_PROFILE));
    assertThat(metaInfDirectory.exists(), equalTo(true));
    final Map<String, ?> kubernetesYaml = objectMapper
        .readValue(new File(metaInfDirectory, "fabric8/kubernetes.yml"), Map.class);
    assertThat(kubernetesYaml, hasKey("items"));
    assertThat((List<Map>) kubernetesYaml.get("items"), hasItem(hasEntry("kind", "Service")));
    final Optional<Integer> portEntry = (Optional<Integer>)((List<Map>) kubernetesYaml.get("items")).stream()
        .filter(p -> p.get("kind").equals("Service")).findFirst()
        .map(s -> (Map<String, ?>)s.get("spec"))
        .map(s -> (List<Map<String, ?>>)s.get("ports"))
        .map(ports -> ports.iterator().next())
        .map(p -> p.get("port"));
    assertThat(portEntry.isPresent(), equalTo(true));
    assertThat(portEntry.get(), equalTo(port));
  }

  private static InvocationResult maven(String goal, String... profiles)
      throws IOException, InterruptedException, MavenInvocationException {

    return MavenUtils.execute(i -> {
      i.setBaseDirectory(new File("../"));
      i.setProjects(Collections.singletonList(PROJECT_MULTI_PROFILE));
      i.setGoals(Collections.singletonList(goal));
      i.setProfiles(Stream.of(profiles).collect(Collectors.toList()));
    });
  }
}
