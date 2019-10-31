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

import com.marcnuri.fmpintegrationtests.docker.DockerUtils;
import com.marcnuri.fmpintegrationtests.maven.MavenUtils;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.File;
import java.util.Collections;
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
    final InvocationRequest invocationRequest = new DefaultInvocationRequest();
    invocationRequest.setBaseDirectory(new File("../"));
    invocationRequest.setProjects(Collections.singletonList(PROJECT_HELLO_WORLD));
    invocationRequest.setGoals(Collections.singletonList("fabric8:build"));

    final InvocationResult invocationResult = MavenUtils.execute(invocationRequest);

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
}
