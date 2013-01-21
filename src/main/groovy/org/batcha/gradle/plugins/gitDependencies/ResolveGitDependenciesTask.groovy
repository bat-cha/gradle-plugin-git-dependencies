package org.batcha.gradle.plugins.gitDependencies

import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.TransportException
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.TestOutputEvent.Destination;

class ResolveGitDependenciesTask extends DefaultTask {
  
  def String getDescription() {
    return "Resolves dependencies specified using 'git' extra property in dependencies' configurations"
  }
  
  
  //@OutputDirectories
  Iterable<File> outputs = new ArrayList<File>()
  

  @TaskAction
  def installDependencies() {

    project.sourceSets.all { SourceSet sourceSet ->

      project.configurations[sourceSet.compileConfigurationName].allDependencies.withType(ExternalModuleDependency).each { d ->

        if (d.hasProperty("git")) {

          def destination = new File(project.gitDependenciesDir + File.separator + d.name)
          
          outputs.add(destination)

          refreshGitRepository(d.git, d.version, destination)
          
        }
        
      }
      
    }
    
  }
  
  def refreshGitRepository(String repositoryUri, String version, File destinationDir) {
    
    if (destinationDir.exists()) {
      
      pullGitRepository(repositoryUri,destinationDir)
      
    }
    
    else {
    
      cloneGitRepository(repositoryUri,destinationDir)
      
    }
    
    checkoutVersion(destinationDir, version)
    
    installGitDependency(destinationDir)
    
  }
  
  def checkoutVersion(File destinationDir, String version) {
    
  }
    
  def pullGitRepository(String repositoryUri, File destinationDir) {
    
    PullCommand cmd = Git.open(destinationDir).pull()
    
    try {
      
      cmd.call()
      
    } catch (TransportException e) {
    
      throw new GradleException("Problem with transport.", e.getMessage() )
      
    } catch (GitAPIException e) {
    
      throw new GradleException("Problem with clone.", e.getMessage() )
      
    }
    
    
  }
  
  
  def cloneGitRepository(String repositoryUri, File destinationDir) {
    
    CloneCommand cmd = Git.cloneRepository()
    
    cmd.setURI(repositoryUri)
    
    cmd.setDirectory(destinationDir)
    
    try {
      
      cmd.call()
      
    } catch (TransportException e) {
    
      throw new GradleException("Problem with transport.", e.getMessage() )
      
    } catch (GitAPIException e) {
    
      throw new GradleException("Problem with clone.", e.getMessage() )
      
    }
    
  }
  
  
  def installGitDependency(File destinationDir) {
    
    def wrapperName = 'gradlew'
    
    def os = System.getProperty("os.name").toLowerCase()
    
    if (os.contains('windows')) {
      
      wrapperName = 'gradlew.bat'
      
    }
        
    def command = wrapperName + " install" 
        
    command.execute(null, destinationDir)
    
  }
  
}
