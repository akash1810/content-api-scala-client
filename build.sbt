import sbt.Keys._
import ReleaseTransformations._
import Dependencies._

/* --------------------------------------------------------------------- */

lazy val root = (project in file("."))
  .aggregate(client, defaultClient)
  .settings(commonSettings)
  .settings(
    publish / skip     := true,
    sources in Compile := Seq.empty,
    sources in Test    := Seq.empty,
    releaseVcsSign     := true,
    releaseCrossBuild  := true,
    releaseProcess     := releaseSteps
  )

lazy val client = (project in file("client"))
  .settings(commonSettings, clientSettings, publishSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("client-default"))
  .dependsOn(client)
  .settings(commonSettings, defaultClientSettings, publishSettings)

lazy val aws = (project in file("aws"))
  .settings(commonSettings, awsSettings, publishSettings)

/* --------------------------------------------------------------------- */

lazy val commonSettings: Seq[Setting[_]] = Metadata.settings ++ Seq(
  releaseUseGlobalVersion := false,
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.min,
  pomIncludeRepository    := { _ => false },
  javacOptions            ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions           ++= Seq("-deprecation", "-unchecked"),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
)

lazy val clientSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject,
  description         := "Scala client for the Guardian's Content API",
  developers          := Metadata.clientDevs,
  releaseVersionFile  := file("version-client.sbt"),
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject + "-default",
  description         := "Default scala client for the Guardian's Content API",
  developers          := Metadata.clientDevs,
  releaseVersionFile  := file("version-client.sbt"),
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  initialCommands in console := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)

lazy val awsSettings: Seq[Setting[_]] = Seq(
  name                := Metadata.ghProject + "-aws",
  description         := "AWS helper functionality for using Guardian's Content API scala client",
  developers          := Metadata.awsDevs,
  releaseVersionFile  := file("version-aws.sbt"),
  libraryDependencies ++= awsDeps,
)

lazy val publishSettings: Seq[Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  publishTo := sonatypePublishTo.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
)

lazy val releaseSteps: Seq[ReleaseStep] = Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeRelease"),
  pushChanges
)