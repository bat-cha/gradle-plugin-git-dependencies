/*
 * Copyright 2014 Baptiste Chatrain <baptiste.chatrain@gmail.com>
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
package org.batcha.gradle.plugins.git

import org.batcha.gradle.plugins.git.dependencies.GitHelper
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.TaskAction

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

      refreshGitDependency(d)

    }
  }

  /**
   * Refresh Git Dependency.
   * @param repositoryUri
   * @param version
   * @param destinationDir
   */
  def refreshGitRepository(ExternalModuleDependency d) {

    def gitVersion = d.version

    if (d.hasProperty("gitVersion")) {
      gitVersion = d.gitVersion
    }


    logger.info("Git dependency found for " + d.name + " " + d.version + " " + d.git + "[version/branch: " + gitVersion + "]")

    def destinationDir = new File(project.gitDependenciesDir + File.separator + d.name)

    if (destinationDir.exists()) {

      GitHelper.fetchGitRepository(d.git,destinationDir)

    }

    else {

      GitHelper.cloneGitRepository(repositoryUri,destinationDir)

    }

    GitHelper.checkoutVersion(destinationDir, gitVersion)

    //InstallHelper.installGitDependency(d, destinationDir)

  }

}
