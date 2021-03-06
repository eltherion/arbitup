import sbt._


object Dependencies {
  private val ammonite        = "com.lihaoyi"                   %  "ammonite"                       % VersionsOf.ammonite cross CrossVersion.full
  private val catsEffect      = "org.typelevel"                 %% "cats-effect"                    % VersionsOf.catsEffect
  private val janino          = "org.codehaus.janino"           %  "janino"                         % VersionsOf.janino
  private val jgrapht         = "org.jgrapht"                   %  "jgrapht-core"                   % VersionsOf.jgrapht
  private val logbackClassic  = "ch.qos.logback"                %  "logback-classic"                % VersionsOf.logbackClassic
  private val monix           = "io.monix"                      %% "monix"                          % VersionsOf.monix
  private val scalaLogging    = "com.typesafe.scala-logging"    %% "scala-logging"                  % VersionsOf.scalaLogging
  private val scalatest       = "org.scalatest"                 %% "scalatest"                      % VersionsOf.scalatest      % Test
  private val sttp            = "com.softwaremill.sttp.client3" %% "core"                           % VersionsOf.sttp
  private val sttpCats        = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % VersionsOf.sttp
  private val sttpCirce       = "com.softwaremill.sttp.client3" %% "circe"                          % VersionsOf.sttp

  val all = Seq(
    ammonite,
    catsEffect,
    janino,
    jgrapht,
    logbackClassic,
    monix,
    scalaLogging,
    scalatest,
    sttp,
    sttpCats,
    sttpCirce
  )
}
