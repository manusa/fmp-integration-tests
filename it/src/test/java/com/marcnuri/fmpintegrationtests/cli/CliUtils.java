/*
 * CliUtils.java
 *
 * Created on 2019-10-31, 13:33
 */
package com.marcnuri.fmpintegrationtests.cli;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-31.
 */
public class CliUtils {

  private static final String PROP_OS_NAME = "os.name";

  private CliUtils() {
  }

  public static CliResult runCommand(String command) throws IOException, InterruptedException {
    final String[] processCommand;
    if (isWindows()) {
      processCommand = new String[]{"cmd", "/c", command};
    } else {
      processCommand = new String[]{"sh", "-c", command};
    }
    final Process process = new ProcessBuilder()
        .redirectErrorStream(true)
        .command(processCommand)
        .start();
    final Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A");
    final String output = scanner.hasNext() ? scanner.next() : "";
    final int exitCode = process.waitFor();
    return new CliResult(exitCode, output);
  }

  private static boolean isWindows() {
    return System.getProperty(PROP_OS_NAME).toLowerCase().contains("win");
  }

  public static final class CliResult {

    private final int exitCode;
    private final String output;

    private CliResult(int exitCode, String output) {
      this.exitCode = exitCode;
      this.output = output;
    }

    public int getExitCode() {
      return exitCode;
    }

    public String getOutput() {
      return output;
    }
  }
}
