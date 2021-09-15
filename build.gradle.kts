plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.71"
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    jcenter()
    mavenCentral()
}

javafx {
    version = "13"
    modules = mutableListOf("javafx.controls", "javafx.fxml", "javafx.base", "javafx.media")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("de.jensd:fontawesomefx-fontawesome:4.7.0-9.1.2")
    implementation("org.jetbrains.exposed:exposed-core:0.28.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.28.1")
    implementation("com.zaxxer:HikariCP:3.4.5")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-fxml:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-base:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-media:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-fxml:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-base:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-media:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:mac")
    runtimeOnly("org.openjfx:javafx-fxml:${javafx.version}:mac")
    runtimeOnly("org.openjfx:javafx-base:${javafx.version}:mac")
    runtimeOnly("org.openjfx:javafx-media:${javafx.version}:mac")
    runtimeOnly(fileTree("libs") { include("*jar") })
}

application {
    mainClassName = "todo.zero.AppKt"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    register("finalJar", Jar::class.java) {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes("Main-Class" to application.mainClassName)
        }
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        from(sourcesMain.output)
    }
}