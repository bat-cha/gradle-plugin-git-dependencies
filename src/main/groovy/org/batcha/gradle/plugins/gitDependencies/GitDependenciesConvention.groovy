package org.batcha.gradle.plugins.gitDependencies

import org.gradle.api.Project

class GitDependenciesConvention {
    def GitDependenciesConvention(Project project) {
        gitDependenciesDir = "${project.buildDir.path}/git-dependencies"
    }
    
    /**
     * Directory to clone/pull sources from git dependencies into
     */
    def String gitDependenciesDir
}
