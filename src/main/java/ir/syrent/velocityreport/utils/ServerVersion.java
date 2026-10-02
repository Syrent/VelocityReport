package ir.syrent.velocityreport.utils;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for detecting the running Minecraft server version.
 * <p>
 * This implementation is <b>self-contained</b> and does not depend on
 * XSeries' {@code XReflection}. The bundled XSeries (13.3.3) only recognizes
 * the legacy {@code 1.x} versioning scheme, and its static initializer crashes
 * on calendar-versioned servers (e.g. {@code 26.1.2}) with an
 * {@code ExceptionInInitializerError}. Parsing the version here avoids that
 * crash entirely.
 * <p>
 * Two versioning schemes are supported:
 * <ul>
 *     <li><b>Legacy</b> ({@code 1.8} – {@code 1.21.x}): major is always {@code 1}.</li>
 *     <li><b>Calendar versioning</b> ({@code 26.1}, {@code 26.2}, {@code 26.3}, …):
 *     introduced by Mojang in 2026. The major number is the two-digit year.</li>
 * </ul>
 * <p>
 * The legacy single-argument {@link #supports(int)} and {@link #getVersion()}
 * methods are kept for backward compatibility with existing call sites. They
 * treat the passed number as a legacy minor version (major assumed to be 1).
 * For calendar-versioned servers every legacy check returns {@code true},
 * because such servers are newer than every legacy release.
 */
public final class ServerVersion {

    private ServerVersion() {}

    /**
     * Parsed major version number.
     * <ul>
     *     <li>Legacy: always {@code 1}.</li>
     *     <li>Calendar versioning: the year component, e.g. {@code 26}.</li>
     * </ul>
     */
    public static final int MAJOR_NUMBER;

    /**
     * Parsed minor version number.
     * <ul>
     *     <li>Legacy: e.g. {@code 20} for {@code 1.20.4}.</li>
     *     <li>Calendar versioning: the month/feature component, e.g. {@code 1} for {@code 26.1.2}.</li>
     * </ul>
     */
    public static final int MINOR_NUMBER;

    /**
     * Parsed patch version number, e.g. {@code 4} for {@code 1.20.4} or {@code 2} for {@code 26.1.2}.
     */
    public static final int PATCH_NUMBER;

    /**
     * {@code true} when the server uses calendar versioning (major &gt; 1), i.e. 26.x or newer.
     */
    public static final boolean YEAR_VERSIONING;

    static {
        // Bukkit.getBukkitVersion() examples:
        //   Legacy:  "1.8.8-R0.1-SNAPSHOT", "1.20.4-R0.1-SNAPSHOT", "1.21.8-R0.1-SNAPSHOT"
        //   CalVer:  "26.1.2.build.74-stable", "26.3.0-R0.1-SNAPSHOT"
        String bukkitVersion = Bukkit.getBukkitVersion();
        Matcher matcher = Pattern
                .compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?")
                .matcher(bukkitVersion);

        int major = 1;
        int minor = 0;
        int patch = 0;

        if (matcher.find()) {
            try {
                major = Integer.parseInt(matcher.group("major"));
                minor = Integer.parseInt(matcher.group("minor"));
                String p = matcher.group("patch");
                patch = (p == null || p.isEmpty()) ? 0 : Integer.parseInt(p);
            } catch (NumberFormatException ignored) {
                // Fall back to defaults (treat as unknown legacy version).
            }
        }

        MAJOR_NUMBER = major;
        MINOR_NUMBER = minor;
        PATCH_NUMBER = patch;
        YEAR_VERSIONING = major > 1;
    }

    /**
     * @return The server's minor version number (legacy compatibility).
     *         For calendar-versioned servers this returns the month/feature
     *         component (e.g. {@code 1} for {@code 26.1.2}). Prefer
     *         {@link #getMajorNumber()} / {@link #getMinorNumber()} /
     *         {@link #getPatchNumber()} for new code.
     */
    public static int getVersion() {
        return MINOR_NUMBER;
    }

    public static int getMajorNumber() {
        return MAJOR_NUMBER;
    }

    public static int getMinorNumber() {
        return MINOR_NUMBER;
    }

    public static int getPatchNumber() {
        return PATCH_NUMBER;
    }

    /**
     * @return {@code true} if the server uses calendar versioning (26.x or newer).
     */
    public static boolean isYearVersioning() {
        return YEAR_VERSIONING;
    }

    /**
     * @return A human-readable version string, e.g. {@code "1.20.4"} or {@code "26.1.2"}.
     */
    public static String getMinecraftVersion() {
        return MAJOR_NUMBER + "." + MINOR_NUMBER + "." + PATCH_NUMBER;
    }

    /**
     * @return {@code true} if the server is running on 1.8 – 1.12.2.
     */
    public static boolean isLegacy() {
        return !supports(13);
    }

    /**
     * @return {@code true} if the server is running on 1.8.* or lower.
     */
    public static boolean isSuperLegacy() {
        return !supports(9);
    }

    /**
     * Checks whether the server version is equal to or greater than the given
     * <b>legacy</b> minor version (the major version is assumed to be {@code 1}).
     * <p>
     * For calendar-versioned servers (26.x+) this always returns {@code true},
     * because they are newer than every legacy {@code 1.x} release.
     *
     * @param version the legacy minor version to compare (e.g. {@code 13} for 1.13, {@code 16} for 1.16)
     * @return {@code true} if the server is equal to or newer than {@code 1.<version>}
     */
    public static boolean supports(int version) {
        return supports(1, version, 0);
    }

    /**
     * Checks whether the server version is equal to or greater than the given
     * {@code major.minor} version (patch assumed to be {@code 0}).
     *
     * @param major major version (1 for legacy, 26+ for calendar versioning)
     * @param minor minor version
     * @return {@code true} if the server is equal to or newer than the given version
     */
    public static boolean supports(int major, int minor) {
        return supports(major, minor, 0);
    }

    /**
     * Checks whether the server version is equal to or greater than the given
     * {@code major.minor.patch} version.
     *
     * @param major major version (1 for legacy, 26+ for calendar versioning)
     * @param minor minor version
     * @param patch patch version
     * @return {@code true} if the server is equal to or newer than the given version
     */
    public static boolean supports(int major, int minor, int patch) {
        if (MAJOR_NUMBER != major) return MAJOR_NUMBER > major;
        if (MINOR_NUMBER != minor) return MINOR_NUMBER > minor;
        return PATCH_NUMBER >= patch;
    }
}
