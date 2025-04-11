# AddTestTagRecipe

It adds `@Tag("unit")` in your tests classes if the class contains `@Test` annotation and if it doesn't 
contain one of the following annotations:

* `@SpringBootTest`
* `@DataJpaTest`
* `@WebMvcTest`
* `@WebFluxTest`
* `@JdbcTest`
* `@DataMongoTest`
* `@DataRedisTest`
* `@DataCassandraTest`
* `@RestClientTest`

It adds `@Tag("integration")` in your tests classes if the class contains `@Test` annotation and one of the following
annotations:

* `@SpringBootTest`
* `@DataJpaTest`
* `@WebMvcTest`
* `@WebFluxTest`
* `@JdbcTest`
* `@DataMongoTest`
* `@DataRedisTest`
* `@DataCassandraTest`
* `@RestClientTest`


# To use it

Add this into your pom.xml file (adapt the version if required).

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.4.0</version>
    <configuration>
        <activeRecipes>
            <recipe>fr.example.recipe.AddTestTagRecipe</recipe>
        </activeRecipes>
        <failOnDryRunResults>true</failOnDryRunResults>
    </configuration>
    <dependencies>
        <!-- Dependency to the recipe -->
        <dependency>
            <groupId>fr.example</groupId>
            <artifactId>add-test-tag-open-rewrite-recipe</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- Needed because @Tag annotation is a part of junit-jupiter-api -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.10.5</version>
        </dependency>
    </dependencies>
</plugin>
```
