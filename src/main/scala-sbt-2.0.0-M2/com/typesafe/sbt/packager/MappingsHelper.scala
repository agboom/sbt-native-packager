package com.typesafe.sbt.packager

import sbt.*
import sbt.io.*
import sbt.librarymanagement.LibraryManagementCodec.*
import sjsonnew.support.scalajson.unsafe.*
import sjsonnew.JsonFormat
import Compat.*


/** A set of helper methods to simplify the writing of mappings */
object MappingsHelper extends Mapper {

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * mappings in Universal ++= directory("extra")
    *   }}}
    *
    * @param sourceDir
    * @return
    *   mappings
    */
  def directory(sourceDir: String): Seq[(File, String)] =
    directory(file(sourceDir))

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * mappings in Universal ++= contentOf("extra")
    *   }}}
    *
    * @param sourceDir
    *   as string representation
    * @return
    *   mappings
    */
  def contentOf(sourceDir: String): Seq[(File, String)] =
    contentOf(file(sourceDir))

  /**
    * Create mappings from your classpath. For example if you want to add additional dependencies, like test or model.
    *
    * @example
    *   Add all test artifacts to a separated test folder
    *   {{{
    * mappings in Universal ++= fromClasspath((managedClasspath in Test).value, target = "test")
    *   }}}
    *
    * @param entries
    * @param target
    * @return
    *   a list of mappings
    */
  def fromClasspath(entries: Def.Classpath, target: String, extracted: Extracted): Seq[(File, String)] =
    fromClasspath(entries, target, extracted, _ => true)

  /**
    * Create mappings from your classpath. For example if you want to add additional dependencies, like test or model.
    * You can also filter the artifacts that should be mapped to mappings.
    *
    * @example
    *   Filter all osgi bundles
    *   {{{
    * Universal / mappings ++= fromClasspath(
    *    (Runtime / managedClasspath).value,
    *    "osgi",
    *    artifact => artifact.`type` == "bundle"
    * )
    *   }}}
    *
    * @param entries
    *   from where mappings should be created from
    * @param target
    *   folder, e.g. `model`. Must not end with a slash
    * @param includeArtifact
    *   function to determine if an artifact should result in a mapping
    * @param includeOnNoArtifact
    *   default is false. When there's no Artifact meta data remove it
    */
  def fromClasspath(
    entries: Def.Classpath,
    target: String,
    extracted: Extracted,
    includeArtifact: Artifact => Boolean,
    includeOnNoArtifact: Boolean = false
  ): Seq[(File, String)] =
    // TODO: test: https://github.com/sbt/sbt/blob/78ac6d38097dac7eed75e857edb2262d05ce219e/main/src/main/scala/sbt/Defaults.scala#L4566
    entries
      .filter(attr =>
        attr
          .get(sbt.Keys.artifactStr)
          .map(artifactFromStr)
          .map(includeArtifact)
          .getOrElse(includeOnNoArtifact)
      )
      .map { attribute =>
        val file = Compat.toFile(attribute.data, extracted)
        file -> s"$target/${file.getName}"
      }

  private def artifactFromStr(str: String): Artifact = {
    val format: JsonFormat[Artifact] = summon[JsonFormat[Artifact]]
    val json = Parser.parseFromString(str).get
    Converter.fromJsonUnsafe(json)(format)
  }

  private def artifactToStr(art: Artifact): String = {
    val format: JsonFormat[Artifact] = summon[JsonFormat[Artifact]]
    CompactPrinter(Converter.toJsonUnsafe(art)(format))
  }

  /**
    * Get the mappings for the given files relative to the given directories.
    */
  def relative(files: Seq[File], dirs: Seq[File]): Seq[(File, String)] =
    (files --- dirs) pair (relativeTo(dirs) | flat)

  /**
    * Constructs a jar name from components...(ModuleID/Artifact)
    */
  def makeJarName(
    org: String,
    name: String,
    revision: String,
    artifactName: String,
    artifactClassifier: Option[String]
  ): String =
    org + "." +
      name + "-" +
      Option(artifactName.replace(name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") +
      revision +
      artifactClassifier.filterNot(_.isEmpty).map("-" + _).getOrElse("") +
      ".jar"

  // Determines a nicer filename for an attributed jar file, using the
  // ivy metadata if available.
  def getJarFullFilename(dep: Attributed[CompatFile], extracted: Extracted): String = {
    val filename: Option[String] = for {
      moduleStr <- dep.metadata.get(sbt.Keys.moduleIDStr)
      artifactStr <- dep.metadata.get(sbt.Keys.artifactStr)
    } yield {
      val module = Classpaths.moduleIdJsonKeyFormat.read(moduleStr)
      val artifact = artifactFromStr(artifactStr)
      makeJarName(module.organization, module.name, module.revision, artifact.name, artifact.classifier)
    }
    filename.getOrElse(toFile(dep.data, extracted).getName)
  }

  def getArtifact(file: Attributed[CompatFile]): Option[Artifact] =
    file.get(sbt.Keys.artifactStr).map(artifactFromStr)

  def setArtifact(artifact: Artifact, attr: Attributed[CompatFile]): Attributed[CompatFile] =
    attr.put(sbt.Keys.artifactStr, artifactToStr(artifact))

}
