package kz.narxoz.vrs;

import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** То, что лаба просит проверять grep'ом, проверяется в каждом mvn verify. */
class ArchitectureTest {

    private static final Path DOMAIN = Path.of("src/main/java/kz/narxoz/vrs/domain");

    @Test
    void domainHasNoJdbcAndNoSpring() throws IOException {
        try (Stream<Path> files = Files.walk(DOMAIN)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String code = Files.readString(file);
                assertFalse(code.contains("java.sql"), file + " imports java.sql");
                assertFalse(code.contains("org.springframework"), file + " imports Spring");
            }
        }
    }

    @Test
    void schemaCheckListsExactlyOurStatuses() throws IOException {
        String schema = Files.readString(Path.of("src/main/resources/db/schema.sql"));
        Matcher m = Pattern.compile("CHECK \\(status IN \\(([^)]*)\\)\\)").matcher(schema);
        assertTrue(m.find(), "schema.sql has no CHECK on status");

        Set<String> inSql = Arrays.stream(m.group(1).split(","))
                .map(s -> s.trim().replace("'", ""))
                .collect(Collectors.toSet());
        Set<String> inJava = Arrays.stream(VehicleRequestStatus.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertEquals(inJava, inSql);
    }

    @Test
    void readmeMentionsEveryStatus() throws IOException {
        String readme = Files.readString(Path.of("README.md"));
        List<String> missing = Arrays.stream(VehicleRequestStatus.values())
                .map(Enum::name)
                .filter(s -> !readme.contains("`" + s + "`"))
                .toList();
        assertTrue(missing.isEmpty(), "README is missing statuses " + missing);
    }
}
