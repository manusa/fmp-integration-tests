/*
 * HelloWorldITCase.java
 *
 * Created on 2019-10-31, 8:54
 */
package com.marcnuri.fmpintegrationtests.springboot;

import static org.hamcrest.MatcherAssert.assertThat;

import com.marcnuri.fmpintegrationtests.maven.MavenUtils;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.File;
import java.util.Collections;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
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
    invocationRequest.setBatchMode(true);
    invocationRequest.setBaseDirectory(new File("../"));
    invocationRequest.setProjects(Collections.singletonList("spring-boot/hello-world"));
    invocationRequest.setGoals(Collections.singletonList("fabric8:build"));

    final Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File(MavenUtils.getMavenLocation()));
    final InvocationResult invocationResult = invoker.execute(invocationRequest);

    assertThat(invocationResult.getExitCode(), Matchers.equalTo(0));
  }
}
