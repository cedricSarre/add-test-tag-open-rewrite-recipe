package fr.example.recipe;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Comparator;

public class AddTestTagRecipe extends Recipe {

    private static final String IMPORT_JUNIT_TAG = "org.junit.jupiter.api.Tag";

    @Override
    public @NotNull String getDisplayName() {
        return "Add @Tag('unit') or @Tag('integration') to your test classes";
    }

    @Override
    public @NotNull String getDescription() {
        return "This adds either @Tag('unit') tag if your test class is a unitary test class, or @Tag" +
                "('integration') if your test class is an integration test class (@SpringBootTest, @DataJpaTest, " +
                "@WebMvcTest, @WebFluxTest, @JdbcTest, @DataMongoTest, @DataRedisTest, @DataCassandraTest, " +
                "@RestClientTest).";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            private boolean hasFoundTestClass = false;

            @Override
            public J.@NotNull CompilationUnit visitCompilationUnit(J.@NotNull CompilationUnit cu, @NotNull ExecutionContext ctx) {
                hasFoundTestClass = false;
                J.CompilationUnit result = super.visitCompilationUnit(cu, ctx);

                if (hasFoundTestClass && !hasTagImport(result)) {
                    doAfterVisit(new AddImport<>(IMPORT_JUNIT_TAG, null, false));
                }

                return result;
            }

            @Override
            public J.@NotNull ClassDeclaration visitClassDeclaration(J.@NotNull ClassDeclaration cd, @NotNull ExecutionContext ctx) {
                if (!isTestClass(cd)) {
                    return cd;
                }
                hasFoundTestClass = true;

                cd = super.visitClassDeclaration(cd, ctx);

                if (hasTagAnnotation(cd)) {
                    return cd;
                }

                boolean isIntegrationTest = cd.getLeadingAnnotations().stream().anyMatch(this::isIntegrationAnnotation);

                String tagValue = isIntegrationTest ? "integration" : "unit";

                return addTagAnnotation(cd, tagValue);
            }

            private boolean isTestClass(J.ClassDeclaration cd) {
                J.CompilationUnit cu = getCursor().firstEnclosingOrThrow(J.CompilationUnit.class);
                String path = cu.getSourcePath().toString();
                boolean isInTestPath = path.contains("/src/test/") || path.contains("/test/") || path.contains("Test.java");

                boolean hasTestAnnotations = cd.getLeadingAnnotations().stream().anyMatch(ann -> isIntegrationAnnotation(ann) || isAnnotationWithName(ann, "Test"));

                return isInTestPath || hasTestAnnotations;
            }

            private boolean hasTagImport(J.CompilationUnit cu) {
                return cu.getImports().stream().anyMatch(i -> i.getTypeName().equals("Tag") && i.getPackageName().equals("org.junit.jupiter.api"));
            }

            private boolean hasTagAnnotation(J.ClassDeclaration cd) {
                return cd.getLeadingAnnotations().stream().anyMatch(ann -> isAnnotationWithName(ann, "Tag"));
            }

            private boolean isAnnotationWithName(J.Annotation ann, String name) {
                return ann.getAnnotationType() instanceof J.Identifier && name.equals(((J.Identifier) ann.getAnnotationType()).getSimpleName());
            }

            private boolean isIntegrationAnnotation(J.Annotation ann) {
                if (!(ann.getAnnotationType() instanceof J.Identifier)) {
                    return false;
                }

                return switch (((J.Identifier) ann.getAnnotationType()).getSimpleName()) {
                    case "SpringBootTest", "DataJpaTest", "WebMvcTest",
                         "WebFluxTest", "JdbcTest", "DataMongoTest",
                         "DataRedisTest", "DataCassandraTest", "RestClientTest"
                            -> true;
                    default -> false;
                };
            }

            private J.ClassDeclaration addTagAnnotation(J.ClassDeclaration cd, String tagValue) {
                JavaTemplate tagTemplate = JavaTemplate
                        .builder("@Tag(\"" + tagValue + "\")")
                        .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api").logCompilationWarningsAndErrors(false))
                        .imports(IMPORT_JUNIT_TAG)
                        .build();

                return tagTemplate.apply(getCursor(), cd.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName))
                );
            }
        };
    }
}
