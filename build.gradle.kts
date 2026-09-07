import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val ktlintVersion = "1.8.0"
val mockkVersion = "1.14.11"
val logstashVersion = "9.0"
val jsoupVersion = "1.23.2"
val openHtmlToPdfVersion = "1.1.85"
val kotlinxHtmlVersion = "0.12.0"
val springDocVersion = "3.1.0"
val klageKodeverkVersion = "3.3.8"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

plugins {
    val kotlinVersion = "2.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "4.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    idea
}

// Temporary override: tomcat-embed-core 11.0.24 from the Spring Boot BOM has CVE-2026-65905.
// Remove when Spring Boot ships 11.0.25 or newer.
extra["tomcat.version"] = "11.0.25"

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.4.10")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:$openHtmlToPdfVersion")
    implementation("io.github.openhtmltopdf:openhtmltopdf-svg-support:$openHtmlToPdfVersion")

    implementation("ch.qos.logback:logback-classic")
    implementation("org.codehaus.janino:janino")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")

    implementation("no.nav.klage:klage-kodeverk:$klageKodeverkVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.springframework:spring-mock:2.0.8")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
    testImplementation("org.mockito:mockito-inline:5.2.0")
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

java.sourceCompatibility = JavaVersion.VERSION_21

ktlint {
    version.set(ktlintVersion)
    ignoreFailures.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
    filter {
        exclude { it.file.path.contains("${File.separator}build${File.separator}") }
    }
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(false)
}

// NamedArguments implements RequiresAnalysisApi, so it only reports when detekt
// runs with a compile classpath. The plain `detekt` task has no classpath and
// would silently pass, hence the analysis aware tasks are wired into `check`
// and the plain one is disabled.
tasks.named("detekt") {
    enabled = false
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    jvmTarget.set(JvmTarget.JVM_21.target)
    reports {
        html.required.set(true)
        checkstyle.required.set(true)
        sarif.required.set(false)
        markdown.required.set(false)
    }
}

tasks.named("check") {
    dependsOn("detektMain", "detektTest")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = false
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main/kotlin")
