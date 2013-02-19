# Git Dependencies Plugin for Gradle
The Git dependencies plugin helps resolving Gradle project dependencies via git using seamless gradle syntax.
It relies on Gradle wrapper tasks and Gradle maven plugin being applied to your dependencies.

## What's this ?
For each dependency you want to resolve using it sources from a git repository,
you can define the Git repository via the ```git``` extra property.

The git-dependencies plugin then does the following:
* clone the repository or fetch from upstream into ```project.gitDependenciesDir```, default to ```"${project.buildDir.path}/git-dependencies"```
* checkout branch/tag corresponding to specified version (master if not found)
* ./gradlew install 

To be resolved just as other dependencies, this plugin add the mavenLocal repository to your project 

## Usage
To use the git-dependencies plugin, include in your build script:

```groovy
apply plugin: 'git-dependencies'

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.batcha.gradle.plugins:git-dependencies:0.1'
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

## API Doc
* [Groovydoc](http://bat-cha.github.com/gradle-plugin-git-dependencies/docs/groovydoc)
