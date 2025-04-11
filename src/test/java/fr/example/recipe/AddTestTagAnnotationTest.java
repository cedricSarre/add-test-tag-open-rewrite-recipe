package fr.example.recipe;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddTestTagAnnotationTest implements RewriteTest {

    @DocumentExample
    @Test
    void shouldAddUnitTag() {
        rewriteRun(
                spec -> spec.recipe(new AddTestTagRecipe()),
                java(
                        // Avant application de la recette
                        """
                        import org.junit.jupiter.api.Test;
        
                        class MyTest {
                            @Test
                            void testSomething() {}
                        }
                        """,
                        // Après application de la recette (avec @Tag("unit"))
                        """
                        import org.junit.jupiter.api.Tag;
                        import org.junit.jupiter.api.Test;
        
                        @Tag("unit")
                        class MyTest {
                            @Test
                            void testSomething() {}
                        }
                        """
                )
        );
    }

    @Test
    void shouldAddIntegrationTag() {
        rewriteRun(
                spec -> spec.recipe(new AddTestTagRecipe())
                        .expectedCyclesThatMakeChanges(1),
                java(
                        """
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.context.SpringBootTest;
    
                        @SpringBootTest
                        class MyIntegrationTest {
                            @Test
                            void testSomething() {}
                        }
                        """,
                        """
                        import org.junit.jupiter.api.Tag;
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.context.SpringBootTest;
    
                        @SpringBootTest
                        @Tag("integration")
                        class MyIntegrationTest {
                            @Test
                            void testSomething() {}
                        }
                        """
                )
        );
    }

    @Test
    void shouldNotAddTagIfAlreadyPresent() {
        rewriteRun(
                spec -> spec.recipe(new AddTestTagRecipe())
                        .expectedCyclesThatMakeChanges(0),
                java(
                        """
                        import org.junit.jupiter.api.Tag;
                        import org.junit.jupiter.api.Test;
    
                        @Tag("unit")
                        class MyTest {
                            @Test
                            void testSomething() {}
                        }
                        """
                )
        );
    }

    @Test
    void shouldNotAddTagIfAlreadyIntegrationTag() {
        rewriteRun(
                spec -> spec.recipe(new AddTestTagRecipe())
                        .expectedCyclesThatMakeChanges(0),
                java(
                        """
                        import org.junit.jupiter.api.Tag;
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.context.SpringBootTest;
    
                        @Tag("integration")
                        @SpringBootTest
                        class MyIntegrationTest {
                            @Test
                            void testSomething() {}
                        }
                        """
                )
        );
    }

    @Test
    void shouldAddIntegrationTagForDataJpaTest() {
        rewriteRun(
                spec -> spec.recipe(new AddTestTagRecipe())
                        .expectedCyclesThatMakeChanges(1),
                java(
                        """
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    
                        @DataJpaTest
                        class MyJpaTest {
                            @Test
                            void testSomething() {}
                        }
                        """,
                        """
                        import org.junit.jupiter.api.Tag;
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    
                        @DataJpaTest
                        @Tag("integration")
                        class MyJpaTest {
                            @Test
                            void testSomething() {}
                        }
                        """
                )
        );
    }

    @Test
    void shouldAddIntegrationTagForWebMvcTest() {
        rewriteRun(
                spec -> spec.recipe(new AddTestTagRecipe())
                        .expectedCyclesThatMakeChanges(1),
                java(
                        """
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    
                        @WebMvcTest
                        class MyControllerTest {
                            @Test
                            void testSomething() {}
                        }
                        """,
                        """
                        import org.junit.jupiter.api.Tag;
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    
                        @Tag("integration")
                        @WebMvcTest
                        class MyControllerTest {
                            @Test
                            void testSomething() {}
                        }
                        """
                )
        );
    }
}
