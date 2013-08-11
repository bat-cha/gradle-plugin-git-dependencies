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

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.CreateBranchCommand
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
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.OutputDirectory;
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

    Set<ExternalModuleDependency> dependencies = new HashSet<ExternalModuleDependency>()
    //scan each configuration for git-dependencies
    for ( config in project.configurations) {

      config.allDependencies.withType(ExternalModuleDependency).each { d ->

        if (d.hasProperty("git")) {

          dependencies.add(d)
        }
      }
    }
    //resolve the dependencies found
    for (ExternalModuleDependency d : dependencies) {

      logger.info("Git dependency found for " + d.name + " " + d.version + " " + d.git)

      def destination = new File(project.gitDependenciesDir + File.separator + d.name)

      refreshGitRepository(d.git, d.version, destination)
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
    
    List branchesList = repo.branchList().setListMode(ListMode.ALL).call()

    Set branchesRemote = new HashSet<String>()
    Set branchesLocal = new HashSet<String>()
    
    for (branchRef in branchesList) {

      if(branchRef.getName().find("refs/heads")) {

        branchesLocal.add(branchRef.getName().replace("refs/heads/", ""))

      } else if(branchRef.getName().find("refs/remotes")) {

        branchesRemote.add(branchRef.getName().replace("refs/remotes/origin/", ""))

      }

    }

    CheckoutCommand cmd = repo.checkout()
    
    if (version in tags) {
      
      cmd.setName(version)
      
    } else if (version in branchesRemote) {
      
      if(!branchesLocal.contains(version)) {
        cmd.setCreateBranch(true)
        cmd.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
        cmd.setStartPoint("origin/" + version);
      }
      cmd.setName(version)
      
    } else {
    
      cmd.setName("master")
    
    }
    
    logger.info("Git dependency checkout " + version + " in " + destinationDir)
    
    cmd.call()
    
  }
  
  /**
   * Fetch from upstream.  
   * @param repositoryUri
   * @param destinationDir
   */
  def fetchGitRepository(String repositoryUri, File destinationDir) {
        
    FetchCommand cmd = Git.open(destinationDir).fetch()
    
    logger.info("Git dependency fetch from  " + repositoryUri)
    
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
    
    logger.info("Git dependency clone from  " + repositoryUri)
               
    cmd.call()
            
  }
  
  /**
   * Install via Gradle Wrapper assuming Maven plugin used by dependency.
   * @param destinationDir
   */
  def installGitDependency(File destinationDir) {
    
    def wrapperName = "./gradlew"
    
    def os = System.getProperty("os.name").toLowerCase()
    
    if (os.contains("windows")) {
      
      wrapperName = "gradlew.bat"
      
    }
    
    def wrapper = new File(destinationDir.absolutePath + File.separator + wrapperName)
    
    if (wrapper.exists()) {
      
      def command = wrapperName + " install"
      
      def install = command.execute(null, destinationDir)
      
      logger.info("Git dependency install via gradle wrapper from  " + destinationDir)
      
      install.waitFor()
      
    } else {
   
      logger.info("Gradle wrapper not found in " + destinationDir + " ! The dependency won't be installed to your local repository ")
    
    }

  }
  
}
