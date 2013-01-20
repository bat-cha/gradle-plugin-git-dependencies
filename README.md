# Git Dependencies Plugin for Gradle
The Git dependencies plugin provides integration of other Java gradle project dependencies via git to your project using seamless gradle syntax.
It relies on Gradle wrapper tasks and Gradle maven plugin.

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
You can also do
```  
apply from: 
```

To declare a Dependency,
``` 
dependencies {

  compile('org.batcha:dummy-project-a:4.2').ext.git = 'git://github.com/bat-cha/dummy-java-project-a.git'
    
}
```
