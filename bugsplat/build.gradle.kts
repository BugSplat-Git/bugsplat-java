/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("com.bugsplat.java-library-conventions")
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("org.json:json:20230227")
}

group = "com.bugsplat"
version = "0.0.1-snapshot"

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "bugsplat-java"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("bugsplat-java")
                description.set("BugSplat exception reporting for JVM applications")
                url.set("https://www.github.com/BugSplat-Git/bugsplat-java")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/BugSplat-Git/bugsplat-java/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("bobbyg603")
                        name.set("Bobby Galli")
                        email.set("bobby@bugsplat.com")
                    }
                    developer {
                        id.set("billbugsplat")
                        name.set("Bill Plunkett")
                        email.set("bill@bugsplat.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://www.github.com/BugSplat-Git/bugsplat-java.git")
                    developerConnection.set("scm:git:ssh://www.github.com/BugSplat-Git/bugsplat-java.git")
                    url.set("https://www.github.com/BugSplat-Git/bugsplat-java")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}