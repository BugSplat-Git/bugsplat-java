[![BugSplat Banner Image](https://user-images.githubusercontent.com/20464226/149019306-3186103c-5315-4dad-a499-4fd1df408475.png)](https://bugsplat.com)

# BugSplat
### **Crash and error reporting built for busy developers.**

[![Follow @bugsplatco on Twitter](https://img.shields.io/twitter/follow/bugsplatco?label=Follow%20BugSplat&style=social)](https://twitter.com/bugsplatco)
[![Join BugSplat on Discord](https://img.shields.io/discord/664965194799251487?label=Join%20Discord&logo=Discord&style=social)](https://discord.gg/bugsplat)

## 👋 Introduction

BugSplat's Java SDK allows you to capture and track exceptions on all JVM platforms. Before continuing with the tutorial please make sure you have completed the following checklist:

- [Register](https://app.bugsplat.com/v2/sign-up) as a new BugSplat user.
- [Log in](https://app.bugsplat.com/auth0/login) using your email address.

## 🏗 Installation

[![Maven Badge](https://maven-badges.herokuapp.com/maven-central/com.bugsplat/bugsplat-java/badge.svg?style=flat)](https://search.maven.org/artifact/com.bugsplat/bugsplat-java)

Install `com.bugsplat` from [Maven Central](https://search.maven.org/artifact/com.bugsplat/bugsplat-java).

**Maven**
```xml
<dependency>
    <groupId>com.bugsplat</groupId>
    <artifactId>bugsplat-java</artifactId>
    <version>0.0.0</version>
</dependency>
```

**Gradle**
```kotlin
implementation("com.bugsplat:bugsplat-java:0.0.0")
```

## ⚙️ Configuration

After you've installed the SDK from Maven, add an import statement for `com.bugsplat.BugSplat`:

```java
import com.bugsplat.BugSplat;
```

Call `BugSplat.init` providing it your `database`, `application`, and `version`. It's best to do this at the entry point of your application. Several defaults can be provided to BugSplat. You can provide default values for things such as `description`, `email`, `key`, `notes`, `user` and additional file attachments.

```java
BugSplat.init("Fred", "MyJavaCrasherConsole", "1.0");
BugSplat.setDescription("Please enter a description");
BugSplat.setEmail("fred@bugsplat.com");
BugSplat.setNotes("bobby testing notes");
BugSplat.setKey("en-US");
BugSplat.setUser("Fred");
BugSplat.addAdditionalFile(new File("file.txt").getAbsolutePath());
```

For servers, console applications, or applications where you don't want to show the report dialog, call `BugSplat.setQuietMode` to prevent the BugSplat dialog from appearing.

```java
BugSplat.setQuietMode(true);
```

Wrap your application in a try/catch block and call `BugSplat.handleException` in the catch block. This will post the exception to BugSplat.

```java
try {
    throw new Exception("BugSplat rocks!");
} catch (Exception ex) {
    Bugsplat.handleException(ex);
}
```

## 🗺️ Samples

This repo includes sample projects that demonstrate how to integrate `bugsplat-java`. Please review the [my-java-crasher](https://github.com/BugSplat-Git/bugsplat-java/tree/master/my-java-crasher) and [my-java-crasher-console](https://github.com/BugSplat-Git/bugsplat-java/tree/master/my-java-crasher-console) folders for more a sample implementation. To run the sample projects, clone this repo and open it in either [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [VS Code](https://code.visualstudio.com/).

## ✅ Verification

Once you've generated an exception, navigate to the BugSplat [Dashboard](https://app.bugsplat.com/v2/dashboard) and ensure you have to correct database selected in the dropdown menu. You should see a new report under the **Recent Crashes** section:

<img width="1728" alt="BugSplat Dashboard Page" src="https://user-images.githubusercontent.com/2646053/234051911-2bed816e-ffff-424e-8dcd-d81f678cfbfa.png">

Click the link in the **ID** column to see details about the report:

<img width="1727" alt="BugSplat Crash Page" src="https://user-images.githubusercontent.com/2646053/234052953-465c53f2-e4c2-4190-8d5b-d6d6ec556e97.png">

That’s it! Your application is now configured to post reports to BugSplat.

## 👷 Support

If you have any additional questions, please email or [support](mailto:support@bugsplat.com) team, join us on [Discord](https://discord.gg/K4KjjRV5ve), or reach out via the chat in our web application.
