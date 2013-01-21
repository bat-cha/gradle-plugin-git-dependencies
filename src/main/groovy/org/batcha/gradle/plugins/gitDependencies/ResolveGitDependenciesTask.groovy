package org.batcha.gradle.plugins.gitDependencies

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ListTagCommand;
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

  @TaskAction
  def installDependencies() {

    project.configurations.testCompile.allDependencies.withType(ExternalModuleDependency).each { d ->

      if (d.hasProperty("git")) {

        def destination = new File(project.gitDependenciesDir + File.separator + d.name)

        refreshGitRepository(d.git, d.version, destination)
      }
    }
  }
  
  def refreshGitRepository(String repositoryUri, String version, File destinationDir) {
    
    if (destinationDir.exists()) {
      
      fetchGitRepository(repositoryUri,destinationDir)
      
    }
    
    else {
    
      cloneGitRepository(repositoryUri,destinationDir)
      
    }
    
    checkoutVersion(destinationDir, version)
    
    installGitDependency(destinationDir)
    
  }
    
  def checkoutVersion(File destinationDir, String version) {
    
    Git repo = Git.open(destinationDir)
        
    Set tags = repo.getRepository().getTags().keySet()
    
    List branchesList = repo.branchList().setListMode(ListMode.REMOTE).call()
    
    Set branches = new HashSet<String>(branchesList.size())
    
    for (branchRef in branchesList) {
      
      branches.add(branchRef.getName().replace("refs/remotes/origin/", ""))
      
    }
    
    println version
    println tags
    println branches
    
    CheckoutCommand cmd = repo.checkout()
    
    if (version in tags || version in branches ) {
      
      cmd.setName(version)
      
    } else {
    
      cmd.setName("master")
    
    }
    
    cmd.call()
    
  }
    
  def fetchGitRepository(String repositoryUri, File destinationDir) {
        
    FetchCommand cmd = Git.open(destinationDir).fetch()
    
    cmd.call()
    
  }
  
  
  def cloneGitRepository(String repositoryUri, File destinationDir) {
    
    CloneCommand cmd = Git.cloneRepository()
    
    cmd.setURI(repositoryUri)
    
    cmd.setDirectory(destinationDir)
               
    cmd.call()
            
  }
  
  
  def installGitDependency(File destinationDir) {
    
    def wrapperName = "gradlew"
    
    def os = System.getProperty("os.name").toLowerCase()
    
    if (os.contains("windows")) {
      
      wrapperName = "gradlew.bat"
      
    }
        
    def command = wrapperName + " install" 
        
    command.execute(null, destinationDir)
    
  }
  
}
