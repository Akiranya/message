plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.oskarsmc"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation("org.bstats:bstats-velocity:3.0.0")
    implementation("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    implementation("cloud.commandframework:cloud-velocity:1.7.0")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.7.0")
    compileOnly("net.luckperms:api:5.4")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    processResources {
        expand("project" to project)
    }

    jar {
        archiveClassifier.set("noshade")
    }

    shadowJar {
        archiveClassifier.set("")
        dependencies {
            include {
                it.moduleGroup == "org.bstats" || it.moduleGroup == "cloud.commandframework" || it.moduleGroup == "io.leangen.geantyref"
            }
        }
        relocate("org.bstats", "com.oskarsmc.message.relocated.bstats")
        relocate("cloud.commandframework", "com.oskarsmc.message.relocated.cloud")
        relocate("io.leangen.geantyref", "com.oskarsmc.message.relocated.geantyref")
    }

    build {
        dependsOn(named("shadowJar"))
    }

    register("deployJar") {
        doLast {
            exec {
                commandLine("rsync", shadowJar.get().archiveFile.get().asFile.absoluteFile, "dev:velocity/jar")
            }
        }
    }
    register("deployJarFresh") {
        dependsOn(build)
        finalizedBy(named("deployJar"))
    }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "message"
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = "OskarsMC"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String?
            artifactId = project.name
            version = project.version as String?

            from(components["java"])
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
