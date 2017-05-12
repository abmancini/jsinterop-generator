/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package jsinterop.generator.closure;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.javascript.jscomp.SourceFile;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/** Entry point for generating JsType from closure extern files. */
public class Runner {
  @Option(name = "--output", usage = "Output jar file path", required = true)
  String output = null;

  @Option(name = "--output_dependency_file", usage = "Output dependency file path", required = true)
  String outputDependencyPath = null;

  @Option(name = "--package_prefix", usage = "Package prefix")
  String packagePrefix = null;

  @Option(name = "--copyright", usage = "Copyright to emit at the beginning of each file")
  String copyright = null;

  @Option(
    name = "--extension_type_prefix",
    usage = "Value used for prefixing extension types",
    required = true
  )
  String extensionTypePrefix = null;

  @Option(name = "--debug_mode", usage = "Enable debug mode")
  boolean debugEnabled = false;

  @Option(
    name = "--bean_convention",
    usage = "Use java bean convention for generated getters and setters"
  )
  boolean beanConvention = false;

  @Option(
    name = "--dependency_mapping_file",
    usage =
        "File generated by the generator that contains the mapping between native fqn and java "
            + "fqn of types provided by a dependency"
  )
  List<String> dependencyMappingFilePaths = new ArrayList<>();

  @Option(
    name = "--name_mapping_file",
    usage =
        "File containing between a fqn of parameter and its final name. This file allows the "
            + "generator to rename some parameters with a well-defined name."
  )
  List<String> nameMappingFilePaths = new ArrayList<>();

  @Option(name = "--dependency", usage = "Source file of a dependency")
  List<String> dependencyFilePaths = new ArrayList<>();

  @Argument(required = true, multiValued = true, usage = "list of source files")
  List<String> sourceFilePaths = new ArrayList<>();

  private void run() {
    Options options =
        Options.builder()
            .outputJarFile(output)
            .outputDependencyFile(outputDependencyPath)
            .packagePrefix(packagePrefix)
            .copyright(copyright)
            .extensionTypePrefix(extensionTypePrefix)
            .debugEnabled(debugEnabled)
            .beanConventionUsed(beanConvention)
            .dependencyMappingFiles(dependencyMappingFilePaths)
            .nameMappingFiles(nameMappingFilePaths)
            .dependencies(
                dependencyFilePaths.stream().map(SourceFile::fromFile).collect(toImmutableList()))
            .sources(sourceFilePaths.stream().map(SourceFile::fromFile).collect(toImmutableList()))
            .build();

    new ClosureJsInteropGenerator(options).convert();
  }

  public static void main(String[] args) throws CmdLineException {
    Runner runner = new Runner();
    CmdLineParser parser = new CmdLineParser(runner);
    parser.parseArgument(args);
    runner.run();
  }
}