package com.vchatrola.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.lang3.StringUtils;

public class ResourceUtil {

  private static InputStream getResourceFileStream(String fileName) throws IOException {
    InputStream stream = ResourceUtil.class.getClassLoader().getResourceAsStream(fileName);
    if (stream == null) {
      String message = String.format("Resource not found: %s", fileName);
      GherkinLintLogger.error(message);
      throw new IOException(message);
    }
    return stream;
  }

  private static void copyResourceToFile(String resourcePath, String targetPath)
      throws IOException {
    try (InputStream resourceStream = getResourceFileStream(resourcePath)) {
      Path targetFilePath = Paths.get(targetPath);

      // Overwrite existing files by default
      Files.copy(resourceStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

      String message = String.format("Resource '%s' copied successfully.", resourcePath);
      GherkinLintLogger.debug(message);
    } catch (IOException e) {
      String message =
          String.format("Error copying resource '%s': %s", resourcePath, e.getMessage());
      GherkinLintLogger.error(message);
      throw e; // Re-throw the exception for caller handling
    }
  }

  public static void copySampleFile(String directoryPath) {
    if (StringUtils.isBlank(directoryPath)) {
      GherkinLintLogger.debug("Sample file copy requested without a directory.");
      notifyUser("GherkinLint", "Please specify a directory", NotificationType.ERROR);
      return;
    }

    try {
      String targetFilePath = buildTargetFilePath(directoryPath);
      ResourceUtil.copyResourceToFile("gherkinlint-rules-sample.json", targetFilePath);
      GherkinLintLogger.info("Sample file copied successfully.");
      notifyUser("GherkinLint", "Sample file copied successfully", NotificationType.INFORMATION);
    } catch (IOException e) {
      GherkinLintLogger.error("Failed to copy sample file.");
      notifyUser(
          "GherkinLint", "Failed to copy sample file: " + e.getMessage(), NotificationType.ERROR);
    }
  }

  private static String buildTargetFilePath(String directoryPath) {
    return directoryPath + "/gherkinlint-rules-sample.json";
  }

  private static void notifyUser(String title, String message, NotificationType type) {
    Project project = getAnyOpenProject();
    NotificationGroupManager.getInstance()
        .getNotificationGroup("GherkinLint")
        .createNotification(title, message, type)
        .notify(project);
  }

  private static Project getAnyOpenProject() {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    if (projects.length > 0) {
      return projects[0];
    }
    return null;
  }
}
