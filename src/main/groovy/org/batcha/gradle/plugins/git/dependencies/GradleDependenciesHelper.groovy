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
package org.batcha.gradle.plugins.git.dependencies

import groovy.util.logging.Slf4j

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
/**
 * Some helper functions to filter Git dependencies from Gradle dependencies.
 * @author bchatrain
 *
 */
@Slf4j
class GradleDependenciesHelper {

  /**
   * Look for already resolved git dependencies
   * @param project to analyze.
   * @return a multimap of {@link ExternalModuleDependency} associated with the {@link Configuration}s in which the dependency appears.
   */
  public static Multimap<Dependency, Configuration> getAlreadyResolvedGitDependencies(Project project) {

    Multimap<Dependency, Configuration> result = HashMultimap.create();

    //scan each configuration
    for ( config in project.configurations) {

      config.allDependencies.each { dependency ->

        // select this dependency
        if (dependency.hasProperty("git") && isAlreadyResolved(project.gitDependenciesDir, dependency.name)) {

          result.put(dependency,config)
        }

      }
    }

    return result
  }

  /**
   * Look for dependencies that cannot be resolved but have a git extension
   * @param project to analyze.
   * @return a multimap of {@link ExternalModuleDependency} associated with the {@link Configuration}s in which the dependency appears.  
   */
  public static Multimap<ExternalModuleDependency, Configuration> getUnresolvedGitDependencies(Project project) {

    Multimap<ExternalModuleDependency, List<Configuration>> result = HashMultimap.create();

    //scan each configuration
    for ( config in project.configurations) {

      Configuration configCopy = config.copy()

      //if a configuration cannot be resolved
      if (configCopy.resolvedConfiguration.hasError()) {

        config.allDependencies.withType(ExternalModuleDependency).each { dependency ->

          // select this dependency
          if (dependency.hasProperty("git")) {

            log.info("Git Repository Found for Unresolved Dependecy {}:{}:{} !",dependency.group,dependency.name,dependency.version)

            result.put(dependency,config)
          }
        }
      }
    }

    return result
  }

  public static void addProjectForMultiBuild(File gitDir, String projectName) {

    def settingsFile = new File(gitDir.parent + File.separator + 'settings.gradle')

    if (!settingsFile.exists()) {
      settingsFile.createNewFile()
    }

    boolean isNotIncluded = settingsFile.findAll {it.contains(projectName) }.empty

    if (isNotIncluded) {
      settingsFile.withWriterAppend{ it.writeLine("include '"+projectName+"'")}
    }

  }

  public static boolean isAlreadyResolved(File gitDir, String projectName) {

    def settingsFile = new File(gitDir.parent + File.separator + 'settings.gradle')

    boolean isIncluded = settingsFile.exists() && !(settingsFile.findAll {it.contains(projectName) }.empty)

    File gitCloneDir = new File(gitDir.absolutePath + File.separator + projectName);
    boolean hasBeenCloned = gitCloneDir.exists() && (gitCloneDir.listFiles().length > 0)


    return isIncluded && hasBeenCloned;

  }

  /**
   * Refresh Git Dependency.
   * @param dependency to refresh.
   */
  public static void refreshGitDependency(Project project, Dependency dependency) {

    def gitVersion = dependency.version

    if (dependency.hasProperty("gitVersion")) {
      gitVersion = dependency.gitVersion
    }

    log.info("Refreshing Git dependency {}:{}:{} url [{}] [version/branch: {}]",dependency.group,dependency.name, dependency.version, dependency.git, gitVersion)

    def destinationDir = new File(project.gitDependenciesDir.absolutePath + File.separator + dependency.name)

    if (destinationDir.exists()) {

      GitHelper.fetchGitRepository(dependency.git,destinationDir)

    }

    else {

      GitHelper.cloneGitRepository(dependency.git,destinationDir)

    }

    GitHelper.checkoutVersion(destinationDir, gitVersion)

  }
}
