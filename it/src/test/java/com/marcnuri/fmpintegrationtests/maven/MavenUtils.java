/*
 * MavenUtils.java
 *
 * Created on 2019-10-31, 10:23
 */
package com.marcnuri.fmpintegrationtests.maven;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-31.
 */
public class MavenUtils {

  private static final String PROP_OS_NAME = "os.name";

  private static String mavenLocation;

  private MavenUtils() {
  }

  public static String getMavenLocation() throws IOException, InterruptedException {
    if (mavenLocation == null) {
      final String[] mavenVersionCommand;
      if (isWindows()) {
        mavenVersionCommand = new String[]{"cmd", "/c", "mvn", "-v"};
      } else {
        mavenVersionCommand = new String[]{"sh", "-c", "mvn -v"};
      }
      final Process mavenVersionProcess = Runtime.getRuntime().exec(mavenVersionCommand);
      if (mavenVersionProcess.waitFor() != 0) {
        final Scanner scanner = new Scanner(mavenVersionProcess.getErrorStream()).useDelimiter("\\A");
        throw new IOException(String.format("Maven: Not found [%s]", scanner.hasNext() ? scanner.next(): ""));
      }
      final Scanner scanner = new Scanner(mavenVersionProcess.getInputStream()).useDelimiter("\\A");
      final String mavenVersionResult = scanner.hasNext() ? scanner.next() : "";
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

  private static boolean isWindows() {
     return System.getProperty(PROP_OS_NAME).toLowerCase().contains("win");
  }

}
