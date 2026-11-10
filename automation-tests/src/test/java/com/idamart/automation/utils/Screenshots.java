package com.idamart.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Saves a picture of the browser when a test fails: target/screenshots/<Class>_<test>.png */
public final class Screenshots {

    private Screenshots() {
    }

    public static void save(WebDriver driver, String name) {
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }
        try {
            Path folder = Path.of("target", "screenshots");
            Files.createDirectories(folder);
            Path source = camera.getScreenshotAs(OutputType.FILE).toPath();
            Path target = folder.resolve(name + ".png");
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + target.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
