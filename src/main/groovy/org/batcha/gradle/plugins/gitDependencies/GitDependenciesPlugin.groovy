package org.batcha.gradle.plugins.gitDependencies

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.GradleException

import org.ajoberstar.gradle.git.tasks.*

class GitDependenciesPlugin implements Plugin<Project> {
    void apply(final Project project) {
		
		project.convention.plugins.gitDependencies = new GitDependenciesConvention(project)
		
		project.sourceSets.all { SourceSet sourceSet ->
			def cloneDependencyTaskName = sourceSet.getTaskName('clone', 'dependency')
			GitClone cloneDependencyTask = project.tasks.add(cloneDependencyTaskName, GitClone)
			
			def gitDependenciesConfigName = (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME) ? "git" : sourceSet.getName() + "Git")
			project.configurations.add(gitDependenciesConfigName) {
				visible = false
				transitive = false
				extendsFrom = []
			}
			
			def gitRepositoriesConfigName = (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME) ? "gitRepositories" : sourceSet.getName() + "GitRepositories")
			
			def installDependencyTaskName = sourceSet.getTaskName('install', 'dependency')
			def installDependencyTask = project.tasks.add(installDependencyTaskName) {
				description = "Install dependencies specified by 'git' configuration"
				actions = [
				{
					project.configurations[gitDependenciesConfigName].each { file ->
						println file
						/*
						def destination = file(project.gitDependenciesDir + File.separator + file)
						cloneDependencyTask.destinationDir = destination
						cloneDependencyTask.uri = "git://github.com/bat-cha/dummy-java-project-a.git"
						cloneDependencyTask.bare = false
						cloneDependencyTask.enabled =
						
						
						if (file.path.endsWith('.jar') || file.path.endsWith('.zip')) {
							ant.unzip(src: file.path, dest: project.extractedProtosDir + "/" + sourceSet.getName())
						} else {
							def compression

							if (file.path.endsWith('.tar')) {
								 compression = 'none'
							} else
							if (file.path.endsWith('.tar.gz')) {
								compression = 'gzip'
							} else if (file.path.endsWith('.tar.bz2')) {
								compression = 'bzip2'
							} else {
								throw new GradleException(
									"Unsupported file type (${file.path}); handles only jar, tar, tar.gz & tar.bz2")
							}

							ant.untar(
								src: file.path,
								dest: project.extractedProtosDir + "/" + sourceSet.getName(),
								compression: compression)
						}
						*/
					}
					
				} as Action
				]
			}
					
			//installDependencyTask.dependsOn(cloneDependencyTask)
			
			/*
			cloneDependencyTask.getSource().srcDir project.extractedProtosDir + "/" + sourceSet.getName()
			
			sourceSet.java.srcDir getGeneratedSourceDir(project, sourceSet)
			String compileJavaTaskName = sourceSet.getCompileTaskName("java");
			Task compileJavaTask = project.tasks.getByName(compileJavaTaskName);
			compileJavaTask.dependsOn(cloneDependencyTask)
			*/
		}
		
		  
    }


}
