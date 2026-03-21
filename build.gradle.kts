plugins {
    kotlin("jvm") version "2.3.0"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.openapi.generator") version "7.7.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    runtimeOnly("org.postgresql:postgresql")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("io.swagger.core.v3:swagger-models:2.2.20")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

val generatedDir = layout.buildDirectory.dir("generated").get().asFile

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/openapi/openapi.yaml")
    outputDir.set(generatedDir.path)
    apiPackage.set("org.example.api")
    modelPackage.set("org.example.model")
    configOptions.set(
        mapOf(
            "basePackage" to "org.example",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true"
        )
    )
}

tasks.named("openApiGenerate").configure {
    doLast {
        val configFile = file("$generatedDir/src/main/kotlin/org/example/SpringDocConfiguration.kt")
        if (configFile.exists()) {
            configFile.writeText(
                configFile.readText()
                    .replace("class SpringDocConfiguration", "open class SpringDocConfiguration")
                    .replace("fun apiInfo():", "open fun apiInfo():")
            )
        }

        val userCreateFile = file("$generatedDir/src/main/kotlin/org/example/model/UserCreate.kt")
        if (userCreateFile.exists()) {
            userCreateFile.writeText(
                userCreateFile.readText()
                    .replace("@get:JsonProperty(\"ip_address\", required = true) val ipAddress", "@get:JsonProperty(\"ip_address\", required = true) @param:JsonProperty(\"ip_address\") val ipAddress")
                    .replace("@get:JsonProperty(\"topic\", required = true) val topic", "@get:JsonProperty(\"topic\", required = true) @param:JsonProperty(\"topic\") val topic")
                    .replace("@get:JsonProperty(\"user_name\", required = true) val userName", "@get:JsonProperty(\"user_name\", required = true) @param:JsonProperty(\"user_name\") val userName")
                    .replace("@get:JsonProperty(\"password\", required = true) val password", "@get:JsonProperty(\"password\", required = true) @param:JsonProperty(\"password\") val password")
                    .replace("@get:JsonProperty(\"port\", required = true) val port", "@get:JsonProperty(\"port\", required = true) @param:JsonProperty(\"port\") val port")
            )
        }
    }
}

sourceSets.main.get().kotlin.srcDir("$generatedDir/src/main/kotlin")
tasks.compileKotlin.get().dependsOn(tasks.named("openApiGenerate"))

tasks.test {
    useJUnitPlatform()
}