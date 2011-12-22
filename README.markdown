EclEmma JaCoCo Module with Maven-Site-Plugin 3.0 Support
========================================================

This is a maven-site-plugin 3.0-supporting fork of the JaCoCo module in the [EclEmma project](https://sourceforge.net/projects/eclemma/). This fork will be of interest to people who would like to use Maven 3's "site" target and see JaCoCo coverage reports generated in the target/site directory.

You do not need to clone this GitHub repository to use the fork. You can use it immediately by making two simple changes to your project's pom.xml.

First, declare this project as a plugin repository:

<pre>
&lt;pluginRepositories&gt;
  &lt;pluginRepository&gt;
    &lt;id&gt;jacoco-fork&lt;/id&gt;
    &lt;name&gt;Maven2 JaCoCo Plugin Repository&lt;/name&gt;
    &lt;url&gt;https://raw.github.com/benalexau/jacoco/master/releases/&lt;/url&gt
  &lt;/pluginRepository&gt;
&lt;/pluginRepositories&gt;
</pre>

Second, enable the plugin:

<pre>
&lt;project&gt;
    &lt;build&gt;
        &lt;plugins&gt;
            &lt;plugin&gt;
            &lt;groupId&gt;org.jacoco&lt;/groupId&gt;
            &lt;artifactId&gt;jacoco-maven-plugin&lt;/artifactId&gt;
            &lt;version&gt;0.5.6.20111223072633&lt;/version&gt;
            &lt;executions&gt;
                &lt;execution&gt;
                    &lt;id&gt;amend-unit-test-java-agent-option&lt;/id&gt;
                    &lt;goals&gt;
                        &lt;goal&gt;prepare-agent&lt;/goal&gt;
                    &lt;/goals&gt;
                &lt;/execution&gt;
            &lt;/executions&gt;
        &lt;/plugin&gt;
        &lt;plugin&gt;
            &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
            &lt;artifactId&gt;maven-site-plugin&lt;/artifactId&gt;
            &lt;version&gt;3.0-beta-3&lt;/version&gt;
            &lt;configuration&gt;
                &lt;reportPlugins&gt;
                    &lt;plugin&gt;
                        &lt;groupId&gt;org.jacoco&lt;/groupId&gt;
                        &lt;artifactId&gt;jacoco-maven-plugin&lt;/artifactId&gt;
                    &lt;/plugin&gt;
                &lt;/reportPlugins&gt;
            &lt;/configuration&gt;
            &lt;/plugin&gt;
        &lt;/plugins&gt;
    &lt;/build&gt;
&lt;/project&gt;
</pre>

To use the plugin for a quick report:

<pre>mvn clean test jacoco:report</pre>

To use the plugin as part of Maven site builds:

<pre>mvn clean test site</pre>

[Ticket 3461365](https://sourceforge.net/tracker/?func=detail&atid=883351&aid=3461365&group_id=177969) has been logged against the [original project](https://sourceforge.net/projects/eclemma/) so they can easily test this fork with various Maven projects and ideally incorporate these changes. As such, please check the ticket for updates before using this fork.

