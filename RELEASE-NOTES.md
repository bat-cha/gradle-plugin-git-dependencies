# Release Notes for Gradle Git Dependencies Plugin

## version 0.2
Change of scope, plugin is no longer relying on wrapper being installed on your dependencies.
The plugin uses gradle multi-project build capability instead of gradle wrapper. 
Also, Git dependencies are referenced as Gradle Project Dependency in a multi-build environement only if dependency cannot be resolved

## version 0.1
The Git dependencies plugin provides integration of other Gradle project dependencies via git