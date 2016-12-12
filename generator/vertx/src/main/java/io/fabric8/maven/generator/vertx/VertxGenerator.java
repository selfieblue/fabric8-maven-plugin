/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.fabric8.maven.generator.vertx;

import io.fabric8.maven.core.util.MavenUtil;
import io.fabric8.maven.docker.config.ImageConfiguration;
import io.fabric8.maven.generator.api.GeneratorContext;
import io.fabric8.maven.generator.api.PortsExtractor;
import io.fabric8.maven.generator.javaexec.JavaExecGenerator;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.fabric8.maven.generator.vertx.Constants.*;

/**
 * Vert.x Generator.
 * <p>
 * Then main part of creating the base image is taken java-exec generator.
 * <p>
 * In addition this generator here adds an appropriate healthcheck when vertx-dropwizard-metrics is
 * given as a dependency (TODO)
 */
public class VertxGenerator extends JavaExecGenerator {

  public VertxGenerator(GeneratorContext context) {
    super(context, "vertx");
  }

  @Override
  public boolean isApplicable(List<ImageConfiguration> configs) throws MojoExecutionException {
    return shouldAddImageConfiguration(configs)
        && (MavenUtil.hasPlugin(getProject(), VERTX_MAVEN_PLUGIN_GA)
        || MavenUtil.hasDependency(getProject(), VERTX_GROUPID));
  }

  @Override
  protected List<String> getExtraJavaOptions() {
    List opts = super.getExtraJavaOptions();
    opts.add("-Dvertx.cacheDirBase=/tmp");
    return opts;
  }

  @Override
  protected boolean isFatJar() throws MojoExecutionException {
    return !hasMainClass() && isUsingFatJarPlugin() || super.isFatJar();
  }

  @Override
  public List<ImageConfiguration> customize(List<ImageConfiguration> configs, boolean isPrePackagePhase) throws MojoExecutionException {
    // TODO Check what need to be done here
    return super.customize(configs, isPrePackagePhase);
  }


  private boolean isUsingFatJarPlugin() {
    MavenProject project = getProject();
    Plugin shade = project.getPlugin(SHADE_PLUGIN_GA);
    Plugin vertx = project.getPlugin(VERTX_MAVEN_PLUGIN_GA);
    return shade != null || vertx != null;
  }

  @Override
  protected List<String> extractPorts() {
    List<String> ports = new ArrayList<>();
    PortsExtractor portsExtractor = new VertxPortsExtractor(log);
    for (Integer p : portsExtractor.extract(getProject()).values()) {
      ports.add(String.valueOf(p));
    }
    return ports;
  }
}
