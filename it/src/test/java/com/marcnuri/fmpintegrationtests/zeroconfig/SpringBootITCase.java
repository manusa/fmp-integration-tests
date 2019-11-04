/*
 * SpringBootITCase.java
 *
 * Created on 2019-10-31, 8:54
 */
package com.marcnuri.fmpintegrationtests.zeroconfig;

import static com.marcnuri.fmpintegrationtests.Tags.KUBERENETES;
import static com.marcnuri.fmpintegrationtests.Tags.OPEN_SHIFT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import com.marcnuri.fmpintegrationtests.docker.DockerUtils;
import com.marcnuri.fmpintegrationtests.maven.MavenUtils;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-31.
 */
@TestMethodOrder(OrderAnnotation.class)
class SpringBootITCase {

  private static final String PROJECT_ZERO_CONFIG = "zero-config/spring-boot";

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
    assertThat(mostRecentImage[0], endsWith("/zero-config-spring-boot"));
    assertThat(mostRecentImage[1], equalTo("latest"));
    assertThat(mostRecentImage[3], containsString("second"));
  }

  @Test
  @Order(2)
  @Tag(KUBERENETES)
  void fabric8Build_zeroConf_shouldCreateImageForKubernetes() throws Exception {
    final String[] dockerImages = DockerUtils.dockerImages()
        .replace("\r", "")
        .split("\n");
    final String[] mostRecentImage = dockerImages[0].split("\t");
    assertThat(mostRecentImage[0], equalTo("fmp-integration-tests/zero-config-spring-boot"));
  }

  @Test
  @Order(3)
  @Tag(OPEN_SHIFT)
  void fabric8Build_zeroConf_shouldCreateImageForOpenshift() throws Exception {
    final String[] dockerImages = DockerUtils.dockerImages()
        .replace("\r", "")
        .split("\n");
    final String[] mostRecentImage = dockerImages[0].split("\t");
    // TODO REMOVE and add assertions
    System.out.printf("\nTODO: %s", mostRecentImage[0]);
  }

  @Test
  @Order(4)
  void fabric8Resource_zeroConf_shouldCreateResources() throws Exception {
    // When
    final InvocationResult invocationResult = MavenUtils.execute(mavenRequest("fabric8:resource"));
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    final File metaInfDirectory = new File(
        String.format("../%s/target/classes/META-INF", PROJECT_ZERO_CONFIG));
    assertThat(metaInfDirectory.exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/kubernetes.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/kubernetes/zero-config-spring-boot-deployment.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/kubernetes/zero-config-spring-boot-service.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift/zero-config-spring-boot-deploymentconfig.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift/zero-config-spring-boot-route.yml"). exists(), equalTo(true));
    assertThat(new File(metaInfDirectory, "fabric8/openshift/zero-config-spring-boot-service.yml"). exists(), equalTo(true));
  }

  @Test
  @Order(5)
  void fabric8Apply_zeroConf_shouldApplyResources() throws Exception {
    // When
    final InvocationResult invocationResult = MavenUtils.execute(mavenRequest("fabric8:apply"));
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    final Optional<Pod> pod = kubernetesClient.pods().list().getItems().stream()
        .filter(p -> p.getMetadata().getName().startsWith("zero-config-spring-boot"))
        .findFirst();
    assertThat(pod.isPresent(), equalTo(true));
    assertThat(pod.get().getMetadata().getLabels(), hasEntry("app", "zero-config-spring-boot"));
    assertThat(pod.get().getMetadata().getLabels(), hasEntry("provider", "fabric8"));
    final Optional<Service> service = kubernetesClient.services().list().getItems().stream()
        .filter(s -> s.getMetadata().getName().startsWith("zero-config-spring-boot"))
        .findFirst();
    assertThat(service.isPresent(), equalTo(true));
    assertThat(service.get().getMetadata().getLabels(), hasEntry("expose", "true"));
    assertThat(service.get().getMetadata().getLabels(), hasEntry("app", "zero-config-spring-boot"));
    assertThat(service.get().getMetadata().getLabels(), hasEntry("provider", "fabric8"));
    assertThat(service.get().getMetadata().getLabels(), hasEntry("group", "com.marcnuri.fmp-integration-tests"));
    assertThat(service.get().getSpec().getSelector(), hasEntry("app", "zero-config-spring-boot"));
    assertThat(service.get().getSpec().getSelector(), hasEntry("provider", "fabric8"));
    assertThat(service.get().getSpec().getSelector(), hasEntry("group", "com.marcnuri.fmp-integration-tests"));
    assertThat(service.get().getSpec().getPorts(), hasSize(1));
    final ServicePort servicePort = service.get().getSpec().getPorts().iterator().next() ;
    assertThat(servicePort.getName(), equalTo("http"));
    assertThat(servicePort.getPort(), equalTo(8080));
    assertThat(servicePort.getNodePort(), nullValue());
  }

  @Test
  @Order(6)
  @Tag(KUBERENETES)
  void fabric8Apply_zeroConf_shouldApplyResourcesForKubernetes() {
    // When
    // #fabric8Apply_zeroConf_shouldApplyResources asserts complete
    // Then
    final Optional<Deployment> deployment = kubernetesClient.apps().deployments().list().getItems().stream()
        .filter(d -> d.getMetadata().getName().startsWith("zero-config-spring-boot"))
        .findFirst();
    assertThat(deployment.isPresent(), equalTo(true));
    assertThat(deployment.get().getMetadata().getLabels(), hasEntry("app", "zero-config-spring-boot"));
    assertThat(deployment.get().getMetadata().getLabels(), hasEntry("provider", "fabric8"));
    assertThat(deployment.get().getMetadata().getLabels(), hasEntry("group", "com.marcnuri.fmp-integration-tests"));
    final DeploymentSpec deploymentSpec = deployment.get().getSpec();
    assertThat(deploymentSpec.getReplicas(), equalTo(1));
    assertThat(deploymentSpec.getSelector().getMatchLabels(),  hasEntry("app", "zero-config-spring-boot"));
    assertThat(deploymentSpec.getSelector().getMatchLabels(), hasEntry("provider", "fabric8"));
    assertThat(deploymentSpec.getSelector().getMatchLabels(), hasEntry("group", "com.marcnuri.fmp-integration-tests"));
    final PodTemplateSpec ptSpec = deploymentSpec.getTemplate();
    assertThat(ptSpec.getMetadata().getLabels(), hasEntry("app", "zero-config-spring-boot"));
    assertThat(ptSpec.getMetadata().getLabels(), hasEntry("provider", "fabric8"));
    assertThat(ptSpec.getMetadata().getLabels(), hasEntry("group", "com.marcnuri.fmp-integration-tests"));
    assertThat(ptSpec.getSpec().getContainers(), hasSize(1));
    final Container ptContainer = ptSpec.getSpec().getContainers().iterator().next();
    assertThat(ptContainer.getImage(), equalTo("fmp-integration-tests/zero-config-spring-boot:latest"));
    assertThat(ptContainer.getName(), equalTo("spring-boot"));
    assertThat(ptContainer.getPorts(), hasSize(3));
    assertThat(ptContainer.getPorts(), hasItems(allOf(
        hasProperty("name", equalTo("http")),
        hasProperty("containerPort", equalTo(8080))
    )));
  }

  @Test
  @Order(7)
  void fabric8Undeploy_zeroConf_shouldDeleteAllAppliedResources() throws Exception {
    // When
    final InvocationResult invocationResult = MavenUtils.execute(mavenRequest("fabric8:undeploy"));
    // Then
    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
    final Optional<Pod> podsExist = kubernetesClient.pods().list().getItems().stream()
        .filter(p -> p.getMetadata().getName().startsWith("zero-config-spring-boot"))
        .findAny();
    if (podsExist.isPresent()) {
      System.out.printf("\nStatus: %s", podsExist.get().getStatus().getContainerStatuses().toString());
      System.out.printf("\nStatus: %s", podsExist.get().getStatus().getConditions().toString());
      System.out.printf("\nStatus phase: %s", podsExist.get().getStatus().getPhase());
      System.out.printf("\nStatus message: %s", podsExist.get().getStatus().getMessage());
      System.out.printf("\nStatus reason: %s", podsExist.get().getStatus().getReason());
    }
    assertThat(podsExist.isPresent(), equalTo(false));
//    assertThat(podsExist, equalTo(false));
    final boolean servicesExist = kubernetesClient.services().list().getItems().stream()
        .anyMatch(s -> s.getMetadata().getName().startsWith("zero-config-spring-boot"));
    assertThat(servicesExist, equalTo(false));
  }

  @Test
  @Order(8)
  @Tag(KUBERENETES)
  void fabric8Undeploy_zeroConf_shouldDeleteAllAppliedResourcesForKubernetes() {
    // When
    // #fabric8Undeploy_zeroConf_shouldDeleteAllAppliedResourcesForKubernetes asserts complete
    // Then
    final boolean deploymentsExists = kubernetesClient.apps().deployments().list().getItems().stream()
        .anyMatch(d -> d.getMetadata().getName().startsWith("zero-config-spring-boot"));
    assertThat(deploymentsExists, equalTo(false));
  }

  private static InvocationRequest mavenRequest(String goal) {
    final InvocationRequest invocationRequest = new DefaultInvocationRequest();
    invocationRequest.setBaseDirectory(new File("../"));
    invocationRequest.setProjects(Collections.singletonList(PROJECT_ZERO_CONFIG));
    invocationRequest.setGoals(Collections.singletonList(goal));
    return invocationRequest;
  }
}
