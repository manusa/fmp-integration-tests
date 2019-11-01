/*
 * HelloWorldITCase.java
 *
 * Created on 2019-10-31, 8:54
 */
package com.marcnuri.fmpintegrationtests.springboot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.startsWith;

import com.marcnuri.fmpintegrationtests.docker.DockerUtils;
import com.marcnuri.fmpintegrationtests.maven.MavenUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.File;
import java.util.Collections;
import java.util.Optional;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-31.
 */
@TestMethodOrder(OrderAnnotation.class)
class HelloWorldITCase {

  private static final String PROJECT_HELLO_WORLD = "spring-boot/hello-world";

  private KubernetesClient kubernetesClient;

  @BeforeEach
  void setUp() {
    kubernetesClient = new DefaultKubernetesClient();
  }

  @AfterEach
  void tearDown() {
    kubernetesClient = null;
  }

  @Test
  @Order(1)
  void fabric8Build_zeroConf_shouldCreateImage() throws Exception {
    // When
    final InvocationResult invocationResult = MavenUtils.execute(mavenRequest("fabric8:build"));
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    final String[] dockerImages = DockerUtils.dockerImages()
        .replace("\r", "")
        .split("\n");
    assertThat(dockerImages, arrayWithSize(greaterThanOrEqualTo(1)));
    final String[] mostRecentImage = dockerImages[0].split("\t");
    assertThat(mostRecentImage[0], equalTo("local/hello-world"));
    assertThat(mostRecentImage[1], equalTo("latest"));
    assertThat(mostRecentImage[3], containsString("second"));
  }

  @Test
  @Order(2)
  void fabric8Resource_zeroConf_shouldCreateResources() throws Exception {
    // When
    final InvocationResult invocationResult = MavenUtils.execute(mavenRequest("fabric8:resource"));
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    final File metaInfDirectory = new File("../spring-boot/hello-world/target/classes/META-INF");
    assertThat(metaInfDirectory.exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/kubernetes.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/kubernetes/hello-world-deployment.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/kubernetes/hello-world-service.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift/hello-world-deploymentconfig.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift/hello-world-route.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift/hello-world-service.yml"). exists(), equalTo(true));
  }

  @Test
  @Order(3)
  void fabric8Apply_zeroConf_shouldApplyResources() throws Exception {
    // When
    final InvocationResult invocationResult = MavenUtils.execute(mavenRequest("fabric8:apply"));
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    final Optional<Pod> pod = kubernetesClient.pods().list().getItems().stream()
        .filter(p -> p.getMetadata().getName().startsWith("hello-world"))
        .findFirst();
    assertThat(pod.isPresent(), equalTo(true));
    assertThat(pod.get().getMetadata().getName(), startsWith("hello-world"));
    assertThat(pod.get().getMetadata().getLabels(), hasEntry("app", "hello-world"));
    assertThat(pod.get().getMetadata().getLabels(), hasEntry("provider", "fabric8"));
    final ServiceList serviceList = kubernetesClient.services().list();
  }

  private static InvocationRequest mavenRequest(String goal) {
    final InvocationRequest invocationRequest = new DefaultInvocationRequest();
    invocationRequest.setBaseDirectory(new File("../"));
    invocationRequest.setProjects(Collections.singletonList(PROJECT_HELLO_WORLD));
    invocationRequest.setGoals(Collections.singletonList(goal));
    return invocationRequest;
  }
}
