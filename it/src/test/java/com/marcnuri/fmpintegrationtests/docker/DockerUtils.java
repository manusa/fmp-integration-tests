/*
 * DockerUtils.java
 *
 * Created on 2019-10-31, 13:21
 */
package com.marcnuri.fmpintegrationtests.docker;

import com.marcnuri.fmpintegrationtests.cli.CliUtils;
import com.marcnuri.fmpintegrationtests.cli.CliUtils.CliResult;
import java.io.IOException;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-31.
 *
 * Could be done using Docker Client included in docker-maven-plugin (or any other).
 * Current approach (use of CLI) is preferred as it's completely independent from FMP.
 */
public class DockerUtils {

  private DockerUtils() {
  }

  public static String dockerImages() throws IOException, InterruptedException {
    final CliResult result = CliUtils.runCommand(
        "docker -l error images --format=\"{{.Repository}}\\t{{.Tag}}\\t{{.ID}}\\t{{.CreatedSince}}\"");
    if (result.getExitCode() != 0) {
      throw new IOException(String.format("Docker: %s", result.getOutput()));
    }
    return result.getOutput();
  }

}
