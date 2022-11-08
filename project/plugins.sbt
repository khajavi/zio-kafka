addSbtPlugin("org.scalameta"             % "sbt-scalafmt"    % "2.4.6")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"    % "0.4.1")
addSbtPlugin("com.eed3si9n"              % "sbt-buildinfo"   % "0.11.0")
addSbtPlugin("com.github.sbt"            % "sbt-ci-release"  % "1.5.10")
addSbtPlugin("org.scalameta"             % "sbt-mdoc"        % "2.3.3")
addSbtPlugin("dev.zio"                   % "zio-sbt-website" % "0.0.0+80-e5b408eb-SNAPSHOT")

resolvers += Resolver.sonatypeRepo("public")
