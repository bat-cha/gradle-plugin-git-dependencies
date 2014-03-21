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

import org.eclipse.jgit.api.CheckoutCommand
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.FetchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk

@Slf4j
class GitHelper {

  /**
   *  Checkout version (or master if not found)
   * @param destinationDir
   * @param version
   */
  static def checkoutVersion(File destinationDir, String version) {

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
      cmd.setCreateBranch(true)
      RevWalk revWalk = new RevWalk(repo.getRepository())
      ObjectId id = repo.getRepository().resolve(version)
      if (id == null) {
        cmd.setName("master")
      } else {
        RevCommit commit = revWalk.parseCommit(id)
        cmd.setStartPoint(commit)
        cmd.setName(version)
      }
    }

    log.info("Git dependency checkout " + version + " in " + destinationDir)

    cmd.call()
  }

  /**
   * Fetch from upstream.
   * @param repositoryUri
   * @param destinationDir
   */
  static def fetchGitRepository(String repositoryUri, File destinationDir) {

    FetchCommand cmd = Git.open(destinationDir).fetch()

    log.info("Git dependency fetch from  " + repositoryUri)

    cmd.call()
  }

  /**
   * Clone Git dependency's repository
   * @param repositoryUri
   * @param destinationDir
   */
  static def cloneGitRepository(String repositoryUri, File destinationDir) {

    CloneCommand cmd = Git.cloneRepository()

    cmd.setURI(repositoryUri)

    cmd.setDirectory(destinationDir)

    log.info("Git dependency clone from  " + repositoryUri + " to " + destinationDir.absolutePath)

    cmd.call()
  }
}
