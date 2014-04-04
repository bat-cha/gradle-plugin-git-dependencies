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

import org.batcha.gradle.plugins.git.dependencies.GradleDependenciesHelper
import org.gradle.api.DefaultTask
import org.gradle.api.UnknownProjectException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction

import com.google.common.collect.Multimap

/**
 * Gradle Task processing the specified dependencies and resolving them via Gradle Wrapper and Maven Install plugin
 * @author bat-cha
 *
 */
class ResolveGitDependenciesTask extends DefaultTask {

  @Override
  def String getDescription() {

    return "Replace by Project dependency the unresolved external dependencies specified using 'git' extra property in dependencies' configurations"
  }

  /**
   * Iterates through dependencies specification and resolve the Git-Dependencies.
   */
  @TaskAction
  def resolveDependencies() {

    Multimap<ExternalModuleDependency, Configuration> gitDependencies = GradleDependenciesHelper.getAlreadyResolvedGitDependencies(project)

    gitDependencies.entries().each { entry ->

      def dependency = entry.getKey()
      def config = entry.getValue()

      def projectPath = ':'+project.gitDependenciesDir.name+':'+dependency.name;

      def projectDep = ['path':projectPath, 'configuration':config.name]

      try {

        ProjectDependency replaced = project.dependencies.project(projectDep);

        replaced.ext.git = dependency.git
        if (dependency.hasProperty("gitVersion")) {
          replaced.ext.gitVersion = dependency.gitVersion
        }

        config.dependencies.add(replaced)

        config.dependencies.remove(dependency)

        logger.info("Replacing an ExternalDependency by a Project Dependency")
      } catch (UnknownProjectException e) {

        logger.error("Failed to Replace an ExternalDependency by a Project Dependency... ")
      }
    }
  }
}
