package org.batcha.gradle.plugins.gitDependencies

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitDependenciesPlugin implements Plugin<Project> {

  def void apply( Project project) {

    project.convention.plugins.gitDependencies = new GitDependenciesConvention(project)
    
    //We make use of the local Maven repository since we install git dependencies 
    project.repositories {
            
      mavenLocal()
      
    }
    
    project.task('resolveGitDependencies', type: ResolveGitDependenciesTask)

    project.compileJava.dependsOn project.resolveGitDependencies
    
  }

}
