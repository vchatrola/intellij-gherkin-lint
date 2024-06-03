package com.vchatrola.util;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    private static void copyResourceToFile(String resourcePath, String targetPath) throws IOException {
        try (InputStream resourceStream = getResourceFileStream(resourcePath)) {
            Path targetFilePath = Paths.get(targetPath);

            // Overwrite existing files by default
            Files.copy(resourceStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            String message = String.format("Resource '%s' copied to file '%s' successfully.", resourcePath, targetPath);
            GherkinLintLogger.info(message);
        } catch (IOException e) {
            String message = String.format("Error copying resource '%s' to file '%s': %s", resourcePath, targetPath, e.getMessage());
            GherkinLintLogger.error(message, e);
            throw e; // Re-throw the exception for caller handling
        }
    }

    public static void copySampleFile(String directoryPath) {
        if (StringUtils.isBlank(directoryPath)) {
            GherkinLintLogger.error("Please specify a directory for the sample file.");
            JOptionPane.showMessageDialog(null, "Please specify a directory", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String targetFilePath = buildTargetFilePath(directoryPath);
            ResourceUtil.copyResourceToFile("gherkinlint-validation-rules-sample.json", targetFilePath);
            GherkinLintLogger.info("Sample file copied successfully to: " + targetFilePath);
            JOptionPane.showMessageDialog(null, "Sample file copied successfully", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            GherkinLintLogger.error("Failed to copy sample file: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to copy sample file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String buildTargetFilePath(String directoryPath) {
        return directoryPath + "/gherkinlint-validation-rules-sample.json";
    }


}


