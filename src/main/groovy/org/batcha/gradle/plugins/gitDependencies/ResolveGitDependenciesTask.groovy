/**
 * Copyright 2013 Baptiste Chatrain
 *
 * This file is part of Gradle Git-Dependencies Plugin.
 *
 * Gradle Git-Dependencies Plugin is free software:
 * you can redistribute it and/or modifyit under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gradle Git-Dependencies Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradle Git-Dependencies Plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */

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

/**
 * Gradle Task processing the specified dependencies and resolving them via Gradle Wrapper and Maven Install plugin
 * @author bat-cha
 *
 */
class ResolveGitDependenciesTask extends DefaultTask {
  
  @Override
  def String getDescription() {
    
    return "Resolves dependencies specified using 'git' extra property in dependencies' configurations"
    
  }

  /**
   * Iterates trough dependencies specification and resolve the Git-Dependencies.
   */
  @TaskAction
  def installDependencies() {

    project.configurations.testCompile.allDependencies.withType(ExternalModuleDependency).each { d ->

      if (d.hasProperty("git")) {

        def destination = new File(project.gitDependenciesDir + File.separator + d.name)

        refreshGitRepository(d.git, d.version, destination)
      }
    }
  }
  
  /**
   * Refresh Git Dependency.
   * @param repositoryUri
   * @param version
   * @param destinationDir
   */
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
   
  /**
   *  Checkout version (or master if not found)
   * @param destinationDir
   * @param version
   */
  def checkoutVersion(File destinationDir, String version) {
    
    Git repo = Git.open(destinationDir)
        
    Set tags = repo.getRepository().getTags().keySet()
    
    List branchesList = repo.branchList().setListMode(ListMode.REMOTE).call()
    
    Set branches = new HashSet<String>(branchesList.size())
    
    for (branchRef in branchesList) {
      
      branches.add(branchRef.getName().replace("refs/remotes/origin/", ""))
      
    }
    
    CheckoutCommand cmd = repo.checkout()
    
    if (version in tags || version in branches ) {
      
      cmd.setName(version)
      
    } else {
    
      cmd.setName("master")
    
    }
    
    cmd.call()
    
  }
  
  /**
   * Fetch from upstream.  
   * @param repositoryUri
   * @param destinationDir
   */
  def fetchGitRepository(String repositoryUri, File destinationDir) {
        
    FetchCommand cmd = Git.open(destinationDir).fetch()
    
    cmd.call()
    
  }
  
  /**
   * Clone Git dependency's repository
   * @param repositoryUri
   * @param destinationDir
   */
  def cloneGitRepository(String repositoryUri, File destinationDir) {
    
    CloneCommand cmd = Git.cloneRepository()
    
    cmd.setURI(repositoryUri)
    
    cmd.setDirectory(destinationDir)
               
    cmd.call()
            
  }
  
  /**
   * Install via Gradle Wrapper assuming Maven plugin used by dependency.
   * @param destinationDir
   */
  def installGitDependency(File destinationDir) {
    
    def wrapperName = "gradlew"
    
    def os = System.getProperty("os.name").toLowerCase()
    
    if (os.contains("windows")) {
      
      wrapperName = "gradlew.bat"
      
    }
        
    def command = wrapperName + " install" 
        
    def install = command.execute(null, destinationDir)
    
    install.waitFor()
    
  }
  
}
