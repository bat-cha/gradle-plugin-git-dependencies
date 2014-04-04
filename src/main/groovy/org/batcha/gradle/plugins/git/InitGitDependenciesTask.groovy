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
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.TaskAction

import com.google.common.collect.Multimap

/**
 * Gradle Task to set-up the multi-build environment for external dependency needing a git repo
 * @author bat-cha
 *
 */
class InitGitDependenciesTask extends DefaultTask {

  @Override
  def String getDescription() {

    return "Setup a multi-project build with dependencies specified using 'git' extra property in dependencies' configurations"
  }

  /**
   * Iterates trough dependencies specification and resolve the Git-Dependencies.
   */
  @TaskAction
  def init() {

    Multimap<ExternalModuleDependency, Configuration> gitDependencies = GradleDependenciesHelper.getUnresolvedGitDependencies(project)

    if (!gitDependencies.isEmpty()) {

      GradleDependenciesHelper.addProjectForMultiBuild(project.gitDependenciesDir, project.name)

      init(project, gitDependencies)
    }
  }

  def init(Project project, Multimap<ExternalModuleDependency, Configuration> gitDependencies) {

    //resolve the dependencies found
    gitDependencies.keySet().each { dependency ->

      GradleDependenciesHelper.refreshGitDependency(project, dependency)

      GradleDependenciesHelper.addProjectForMultiBuild(project.gitDependenciesDir,project.gitDependenciesDir.name+':'+dependency.name)

    }
  }
}
