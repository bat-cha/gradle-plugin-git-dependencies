# Git Dependencies Plugin for Gradle
The Git dependencies plugin helps resolving Gradle project dependencies via git using seamless gradle syntax.
It relies on Gradle multi-project build capabilities. The purpose of this plugin is to setup a multi-project Gradle environment ONLY when 
a dependency cannot be resolved via the classical maven or ivy Gradle Dependency Resolution.

## What's this ?
If you sometime need to convert some [external module dependency](http://www.gradle.org/docs/current/userguide/dependency_management.html#sub:module_dependencies) to [project dependency](http://www.gradle.org/docs/current/userguide/dependency_management.html#sub:project_dependencies) then this plugin is for you !
Just specify your git repo where the plugin can find your dependency.

The git-dependencies plugin adds 3 tasks to your project:
* initGitDependencies
* resolveGitDependencies
* refreshGitDependencies

## ```initGitDependencies``` task
This task will look for dependency that cannot be resolved but have a ```git``` extra property. It will clone them into ```project.gitDependenciesDir```, default to ```"${projectDir.parent}/${project.name}-git-dependencies"```
and prepare your environment for a multi-project build by adding '''settings.gradle''' in ```"${projectDir.parent}```

## ```resolveGitDependencies``` task
This task will always be called after your project evaluation, it is simply replacing any unresolved git dependencies by a project dependency using the cloned repositories.

## ```refreshGitDependencies``` task
This task simply refresh all cloned repository

## Usage
To use the git-dependencies plugin, include in your build script:

```groovy
apply plugin: 'git-dependencies'

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.batcha.gradle.plugins:git-dependencies:0.2'
    }
}
```
If you do not need the gradle --offline (cf http://issues.gradle.org/browse/GRADLE-1768), You can also do
```groovy  
apply from: 'https://raw.github.com/bat-cha/gradle-plugin-git-dependencies/0.2/git-dependencies.gradle'
```

To declare a Dependency,
```groovy 
dependencies {

  compile('org.batcha:dummy-project-a:4.2').ext.git = 'https://github.com/bat-cha/dummy-java-project-a.git'
    
}
```
or if you want to use a different version from the unresolved dependency
```groovy 

dependencies {

  compile('org.anic:myproject:42.0') {
    ext.git = 'git@github.com:fabzo/gradle-plugin-git-dependencies.git'
    ext.gitVersion = 'some/otherBranch/h2g2'
  }
    
}
```

To setup the multi-project build for all the non resolved dependencies having a git property,
``` 
./gradlew initGitDependencies

```
To refresh git repositories for the non resolved dependencies having a git property,
``` 
./gradlew refreshGitDependencies
```

## API Doc
* [Groovydoc](http://bat-cha.github.com/gradle-plugin-git-dependencies/docs/groovydoc)
