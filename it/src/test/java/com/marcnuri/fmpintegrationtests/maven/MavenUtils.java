/*
 * MavenUtils.java
 *
 * Created on 2019-10-31, 10:23
 */
package com.marcnuri.fmpintegrationtests.maven;

import com.marcnuri.fmpintegrationtests.cli.CliUtils;
import com.marcnuri.fmpintegrationtests.cli.CliUtils.CliResult;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-31.
 */
public class MavenUtils {

  private static String mavenLocation;

  private MavenUtils() {
  }

  public static InvocationResult execute(InvocationRequestCustomizer irc)
      throws IOException, InterruptedException, MavenInvocationException {

    final InvocationRequest invocationRequest = new DefaultInvocationRequest();
    irc.customize(invocationRequest);
    return execute(invocationRequest);
  }

  public static InvocationResult execute(InvocationRequest invocationRequest)
      throws IOException, InterruptedException, MavenInvocationException {

    invocationRequest.setBatchMode(true);
    final Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File(getMavenLocation()));
    return invoker.execute(invocationRequest);
  }

  private static String getMavenLocation() throws IOException, InterruptedException {
    if (mavenLocation == null) {
      final CliResult mavenVersion = CliUtils.runCommand(".." + File.separatorChar + "mvnw -v");
      if (mavenVersion.getExitCode() != 0){
        throw new IOException(String.format("Maven: [%s]", mavenVersion.getOutput()));
      }
      final String mavenVersionResult = mavenVersion.getOutput();
      final Pattern mavenHomePattern = Pattern.compile("Maven home:([^\\n\\r]+)", Pattern.MULTILINE);
      final Matcher mavenHomeMatcher =
          mavenHomePattern.matcher(mavenVersionResult);
      if (!mavenHomeMatcher.find()) {
        throw new IOException(String.format("Maven: Incompatible version [%s]", mavenVersionResult));
      }
      mavenLocation = mavenHomeMatcher.group(1).trim();
    }
    return mavenLocation;
  }

  @FunctionalInterface
  public interface InvocationRequestCustomizer {

    void customize(InvocationRequest invocationRequest);
  }

}
