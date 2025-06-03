import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mockkVersion = "1.14.2"
val logstashVersion = "8.1"
val jsoupVersion = "1.20.1"
val openHtmlToPdfVersion = "1.1.28"
val kotlinxHtmlVersion = "0.12.0"
val springDocVersion = "2.8.8"
val klageKodeverkVersion = "1.10.11"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

plugins {
    val kotlinVersion = "2.1.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "3.5.0"
    idea
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")

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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standard-out")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main/kotlin")
