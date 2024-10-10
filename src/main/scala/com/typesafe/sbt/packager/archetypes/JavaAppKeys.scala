package com.typesafe.sbt.packager.archetypes

import com.typesafe.sbt.packager.Compat.CompatFile
import sbt.*

/**
  * Available settings/tasks for the [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]] and all depending
  * archetypes.
  */
trait JavaAppKeys {

  // TODO - we should change this key name in future versions; it also specified
  // the location of the systemd EnvironmentFile
  val bashScriptEnvConfigLocation = SettingKey[Option[String]](
    "bashScriptEnvConfigLocation",
    "The location of a bash script that will be sourced before running the app."
  )
  val scriptClasspathOrdering = TaskKey[Seq[(CompatFile, String)]](
    "scriptClasspathOrdering",
    "The order of the classpath used at runtime for the bat/bash scripts."
  )
  val projectDependencyArtifacts = TaskKey[Def.Classpath](
    "projectDependencyArtifacts",
    "The set of exported artifacts from our dependent projects."
  )
  val scriptClasspath = TaskKey[Seq[String]](
    "scriptClasspath",
    "A list of relative filenames (to the lib/ folder in the distribution) of what to include on the classpath."
  )
}
