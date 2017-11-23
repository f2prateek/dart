package dart.henson.plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class HensonPluginFunctionalTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File manifestFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        testProjectDir.newFolder('src','main')
        manifestFile = testProjectDir.newFile('src/main/AndroidManifest.xml')
    }

    def "fails on non android projects"() {
        buildFile << """
        plugins {
            id 'java-library'
            id 'dart.henson-plugin'
        }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build')
            .withPluginClasspath()
            .build()

        then:
        org.gradle.testkit.runner.UnexpectedBuildFailure ex = thrown()
        ex.message.contains("'android' or 'android-library' plugin required.")
    }

    def "applies to android projects"() {
        manifestFile << """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="test">
        
          <application
              android:label="Test"
              android:name=".Test"/>
        </manifest>
        """

        buildFile << """
        buildscript {
            repositories {
                google()
                jcenter()
            }
        
            dependencies {
                classpath 'com.android.tools.build:gradle:3.0.1'
            }
        }

        plugins {
            //the order matters here
            id 'com.android.application'
            id 'dart.henson-plugin'
        }
        
        android {
            compileSdkVersion 26
            defaultConfig {
                applicationId 'test'
                minSdkVersion 26
                targetSdkVersion 26
                versionCode 1
                versionName '1.0.0'
            }
            flavorDimensions "color"

            productFlavors {
                red {
                    applicationId "com.blue"
                    dimension "color"
                }
                blue {
                    applicationId "com.red"
                    dimension "color"
                }
            }
        }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('--no-build-cache', 'assemble', 'intentBuilderJar', 'intentBuilderJarRed', 'intentBuilderJarRelease', 'intentBuilderJarBlueDebug', '-d', '-s')
                .withPluginClasspath()
                .build()

        then:
        result.task(":assemble").outcome == SUCCESS
        result.task(":intentBuilderJar").outcome == SUCCESS
        result.task(":intentBuilderJarRed").outcome == SUCCESS
        result.task(":intentBuilderJarRelease").outcome == SUCCESS
        result.task(":intentBuilderJarBlueDebug").outcome == SUCCESS
    }
}