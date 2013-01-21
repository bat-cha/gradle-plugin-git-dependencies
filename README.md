# Git Dependencies Plugin for Gradle
The Git dependencies plugin helps resolving Gradle project dependencies via git using seamless gradle syntax.
It relies on Gradle wrapper tasks and Gradle maven plugin being applied to your dependencies.

## What it does
For each dependency you want to resolve using it sources from a git repository, via the ```git``` extra property
This plugin does the following:
* clone the repository if not yet done
* pull
* checkout branch/tag corresponding to specified version (master if not found)
* ./gradlew install 

## Usage
To use the git-dependencies plugin, include in your build script:

```groovy
apply plugin: 'git-dependencies'

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.batcha.gradle.plugins:gradle-plugin-git-dependencies:0.1'
    }
}
```
If you do not need the gradle --offline (cf http://issues.gradle.org/browse/GRADLE-1768), You can also do
```groovy  
apply from: 'https://raw.github.com/bat-cha/gradle-plugin-git-dependencies/0.1/git-dependencies.gradle'
```

To declare a Dependency,
```groovy 
dependencies {

  compile('org.batcha:dummy-project-a:4.2').ext.git = 'https://github.com/bat-cha/dummy-java-project-a.git'
    
}
```
