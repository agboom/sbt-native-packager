package com.typesafe.sbt.packager

import sbt.{librarymanagement as lm, PathFinder, File}
import sbt.internal.{librarymanagement as ilm, BuildDependencies as InternalBuildDependencies}
import sbt.util.CacheStore

import java.nio.file.Path

object Compat {
  val IvyActions = ilm.IvyActions
  type IvySbt = ilm.IvySbt
  type IvyScala = sbt.librarymanagement.ScalaModuleInfo
  val IvyScala = sbt.librarymanagement.ScalaModuleInfo

  type UpdateConfiguration = lm.UpdateConfiguration

  /**
    * Used in
    *   - [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]]
    */
  type BuildDependencies = InternalBuildDependencies

  /**
    */
  type Process = sys.process.Process

  /**
    * Used in
    *   - [[com.typesafe.sbt.packager.docker.DockerPlugin]]
    */
  type ProcessLogger = sys.process.ProcessLogger

  /**
    * Used in
    *   - [[com.typesafe.sbt.packager.Stager]]
    * @param file
    * @return
    *   a CacheStore
    */
  implicit def fileToCacheStore(file: java.io.File): CacheStore = CacheStore(file)

  type CompatFile = xsbti.HashedVirtualFileRef

  def fromFile(file: File, extracted: sbt.Extracted): CompatFile = {
    extracted.get(sbt.Keys.fileConverter).toVirtualFile(Path.of(file.toURI))
  }

  def toFile(file: CompatFile, extracted: sbt.Extracted): File = {
    extracted.get(sbt.Keys.fileConverter).toPath(file).toFile
  }

  val moduleKey = sbt.Keys.moduleIDStr
  val artifactKey = sbt.Keys.artifactStr
}
