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
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.TaskAction

/**
 * Gradle Task processing the specified dependencies and resolving them via Gradle Wrapper and Maven Install plugin
 * @author bat-cha
 *
 */
class ResolveGitDependenciesTask extends DefaultTask {

  def always() {
    outputs.upToDateWhen { false }
  }

  @Override
  def String getDescription() {

    return "Resolves dependencies specified using 'git' extra property in dependencies' configurations"
  }

  /**
   * Iterates trough dependencies specification and resolve the Git-Dependencies.
   */
  @TaskAction
  def installDependencies() {

    Map<ExternalModuleDependency, Configuration> dependencies = new HashMap<ExternalModuleDependency, Configuration>()
    //scan each configuration for git-dependencies
    for ( config in project.configurations) {

      Configuration configCopy = config.copy()

      if (configCopy.resolvedConfiguration.hasError()) {

        config.allDependencies.withType(ExternalModuleDependency).each { d ->

          if (d.hasProperty("git")) {

            dependencies.put(d,config)

          }
        }
      }


    }
    //resolve the dependencies found
    dependencies.each { dependency, config ->

      config.dependencies.remove(dependency)

      refreshGitDependency(dependency, config)

      def gitdir = project.file(project.gitDependenciesDir);

      def projectPath = ':'+gitdir.name+':'+dependency.name;

      def projectDep = ['path':projectPath, 'configuration':config.name]


      def settingsFile = new File(gitdir.absolutePath + File.separator + 'settings.gradle')

      settingsFile.withWriter{ it << "include '"+dependency.name+"'"}

      def buildFile = new File(gitdir.absolutePath + File.separator + 'build.gradle')

      buildFile.withWriter{ it << "evaluationDependsOn(':"+dependency.name+"')"}
      //
      //      Project dep = ProjectBuilder.builder().withName(projectPath).withParent(project).withProjectDir().build()
      //
      //      println dep
      //
      //      project.subprojects.add(dep)
      //
      //      dep.evaluate()

      project.dependencies.project(projectDep)

      println config.name
      println config.dependencies


    }


  }

  /**
   * Refresh Git Dependency.
   * @param dependency to refresh.
   */
  def refreshGitDependency(ExternalModuleDependency dependency, Configuration config) {

    def gitVersion = dependency.version

    if (dependency.hasProperty("gitVersion")) {
      gitVersion = dependency.gitVersion
    }


    logger.info("Git dependency found for " + dependency.name + " " + dependency.version + " " + dependency.git + "[version/branch: " + gitVersion + "]")

    def destinationDir = new File(project.gitDependenciesDir + File.separator + dependency.name)

    if (destinationDir.exists()) {

      GitHelper.fetchGitRepository(dependency.git,destinationDir)

    }

    else {

      GitHelper.cloneGitRepository(dependency.git,destinationDir)

    }

    GitHelper.checkoutVersion(destinationDir, gitVersion)

  }

}
