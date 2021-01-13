import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val javaxActivationVersion = "1.2.0"
val jaxRiVersion = "2.3.2"
val kotlinJacksonVersion = "2.11.2"
val prometheusVersion = "1.3.14"
val tokenValidationSpringSupportVersion = "1.3.0"

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.10"
    id("org.springframework.boot") version "2.2.10.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("no.nav.security:token-validation-spring:$tokenValidationSpringSupportVersion")

    implementation("com.sun.xml.ws:jaxws-ri:$jaxRiVersion")
    implementation("com.sun.activation:javax.activation:$javaxActivationVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("org.slf4j:slf4j-api:1.7.25")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-test-support:$tokenValidationSpringSupportVersion")
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.ApplicationKt"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
