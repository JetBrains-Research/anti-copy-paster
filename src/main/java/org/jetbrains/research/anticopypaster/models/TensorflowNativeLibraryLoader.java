package org.jetbrains.research.anticopypaster.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class fixes loading of native library in case
 * the classloader does not provide package version info
 */
final class TensorflowNativeLibraryLoader {
    private static final String JNI_LIBNAME = "tensorflow_jni";
    private static final String TENSORFLOW_VERSION = "1.15.0";
    private static final AtomicBoolean executed = new AtomicBoolean();

    public static void load() {
        if (!executed.compareAndSet(false, true)) {
            return; // was previously called
        }
        // Native code has been packaged into the .jar file containing this.
        // Extract the JNI library itself
        final String jniLibName = System.mapLibraryName(JNI_LIBNAME);
        final String jniResourceName = makeResourceName(jniLibName);
        final InputStream jniResource =
                TensorflowNativeLibraryLoader.class.getClassLoader().getResourceAsStream(jniResourceName);
        // Extract the JNI's dependency
        final String frameworkLibName =
                getVersionedLibraryName(System.mapLibraryName("tensorflow_framework"));
        final String frameworkResourceName = makeResourceName(frameworkLibName);
        final InputStream frameworkResource =
                TensorflowNativeLibraryLoader.class.getClassLoader().getResourceAsStream(frameworkResourceName);
        // Do not complain if the framework resource wasn't found. This may just mean that we're
        // building with --config=monolithic (in which case it's not needed and not included).
        if (jniResource == null) {
            throw new UnsatisfiedLinkError(
                    String.format(
                            "Cannot find TensorFlow native library for OS: %s, architecture: %s. See "
                                    + "https://github.com/tensorflow/tensorflow/tree/master/tensorflow/java/README.md"
                                    + " for possible solutions (such as building the library from source). Additional"
                                    + " information on attempts to find the native library can be obtained by adding"
                                    + " org.tensorflow.NativeLibrary.DEBUG=1 to the system properties of the JVM.",
                            os(), architecture()));
        }
        try {
            // Create a temporary directory for the extracted resource and its dependencies.
            final File tempPath = Files.createTempDirectory("tensorflow_native_libraries-").toFile();
            // Deletions are in the reverse order of requests, so we need to request that the directory be
            // deleted first, so that it is empty when the request is fulfilled.
            tempPath.deleteOnExit();
            final String tempDirectory = tempPath.getCanonicalPath();
            if (frameworkResource != null) {
                extractResource(frameworkResource, frameworkLibName, tempDirectory);
            }
            System.load(extractResource(jniResource, jniLibName, tempDirectory));
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(
                    String.format(
                            "Unable to extract native library into a temporary file (%s)", e.toString()));
        }
    }

    private static boolean resourceExists(String baseName) {
        return TensorflowNativeLibraryLoader.class.getClassLoader().getResource(makeResourceName(baseName)) != null;
    }

    private static String getVersionedLibraryName(String libFilename) {
        // If the resource exists as an unversioned file, return that.
        if (resourceExists(libFilename)) {
            return libFilename;
        }

        final String versionName = getMajorVersionNumber();

        // If we're on darwin, the versioned libraries look like blah.1.dylib.
        final String darwinSuffix = ".dylib";
        if (libFilename.endsWith(darwinSuffix)) {
            final String prefix = libFilename.substring(0, libFilename.length() - darwinSuffix.length());
            if (versionName != null) {
                final String darwinVersionedLibrary = prefix + "." + versionName + darwinSuffix;
                if (resourceExists(darwinVersionedLibrary)) {
                    return darwinVersionedLibrary;
                }
            } else {
                // If we're here, we're on darwin, but we couldn't figure out the major version number. We
                // already tried the library name without any changes, but let's do one final try for the
                // library with a .so suffix.
                final String darwinSoName = prefix + ".so";
                if (resourceExists(darwinSoName)) {
                    return darwinSoName;
                }
            }
        } else if (libFilename.endsWith(".so")) {
            // Libraries ending in ".so" are versioned like "libfoo.so.1", so try that.
            final String versionedSoName = libFilename + "." + versionName;
            if (versionName != null && resourceExists(versionedSoName)) {
                return versionedSoName;
            }
        }

        // Otherwise, we've got no idea.
        return libFilename;
    }

    /**
     * Returns the major version number of this TensorFlow Java API, or {@code null} if it cannot be
     * determined.
     */
    private static String getMajorVersionNumber() {
        String version = TensorflowNativeLibraryLoader.class.getPackage().getImplementationVersion();
        // expecting a string like 1.14.0, we want to get the first '1'.
        if (version == null) {
            version = TENSORFLOW_VERSION; // classloader does not provide package information
        }
        int dotIndex = version.indexOf('.');
        if (dotIndex == -1) {
            return null; // invalid version string
        }
        String majorVersion = version.substring(0, dotIndex);
        try {
            Integer.parseInt(majorVersion);
            return majorVersion;
        } catch (NumberFormatException unused) {
            return null;
        }
    }

    private static String extractResource(
            InputStream resource, String resourceName, String extractToDirectory) throws IOException {
        final File dst = new File(extractToDirectory, resourceName);
        dst.deleteOnExit();
        final String dstPath = dst.toString();
        Files.copy(resource, dst.toPath());
        return dstPath;
    }

    private static String os() {
        final String p = System.getProperty("os.name").toLowerCase();
        if (p.contains("linux")) {
            return "linux";
        } else if (p.contains("os x") || p.contains("darwin")) {
            return "darwin";
        } else if (p.contains("windows")) {
            return "windows";
        } else {
            return p.replaceAll("\\s", "");
        }
    }

    private static String architecture() {
        final String arch = System.getProperty("os.arch").toLowerCase();
        return (arch.equals("amd64")) ? "x86_64" : arch;
    }

    private static String makeResourceName(String baseName) {
        return "org/tensorflow/native/" + String.format("%s-%s/", os(), architecture()) + baseName;
    }

    private TensorflowNativeLibraryLoader() {}
}
