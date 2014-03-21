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

import org.gradle.api.artifacts.ExternalModuleDependency

@Slf4j
class InstallHelper {


  /**
   * Install via Gradle Wrapper install task
   * @param destinationDir
   */
  static def installGitDependency(File destinationDir, ExternalModuleDependency dependency) {

    def wrapperName = "./gradlew"

    def os = System.getProperty("os.name").toLowerCase()

    if (os.contains("windows")) {

      wrapperName = "gradlew.bat"
    }

    def wrapper = new File(destinationDir.absolutePath + File.separator + wrapperName)

    if (wrapper.exists()) {

      def command = wrapperName + " install"

      def install = command.execute(null, destinationDir)

      log.info("Git dependency install via gradle wrapper from  " + destinationDir)

      install.waitFor()
    } else {

      log.info("Gradle wrapper not found in " + destinationDir + " ! The dependency won't be installed to your local repository ")
    }
  }
}
