package dart.henson.plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static groovy.io.FileType.FILES

class HensonPluginFunctionalTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File buildFile
    File manifestFile
    File srcMain
    File srcNavigationMain

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        buildFile = testProjectDir.newFile('build.gradle')
        testProjectDir.newFolder('src','main')
        manifestFile = testProjectDir.newFile('src/main/AndroidManifest.xml')
        testProjectDir.newFolder('src','main', 'java', 'test')
        srcMain = testProjectDir.newFile('src/main/java/test/FooActivity.java')
        testProjectDir.newFolder('src','navigation', 'main', 'java', 'test')
        srcNavigationMain = testProjectDir.newFile('src/navigation/main/java/test/Foo.java')
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
        srcMain << """
        package test;
        
        import android.app.Activity;
        import android.os.Bundle;
        
        class FooActivity extends Activity {
          
          @Override
          public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Foo foo = new Foo();  
          }
        }
        """
        srcNavigationMain << """
        package test;
        
        import dart.BindExtra;
        import dart.DartModel;
        
        @DartModel("test.TestActivity")
        class Foo {
          @BindExtra String s;
        }
        """

        settingsFile << """
        rootProject.name = "test-project"
        """

        buildFile << """
        buildscript {
            repositories {
                google()
                jcenter()
                mavenLocal()
                maven {
                  url 'https://oss.sonatype.org/content/repositories/snapshots'
                }
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
        
        repositories {
            google()
            jcenter()
            mavenLocal()
            maven {
              url 'https://oss.sonatype.org/content/repositories/snapshots'
            }
        }
        """

        when:
        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                //.withArguments('--no-build-cache', 'assemble', 'tasks', '--all', '-d', '-s')
                .withArguments('--no-build-cache', 'clean', 'assemble', 'intentBuilderJar', 'intentBuilderJarRed', 'intentBuilderJarRelease', 'intentBuilderJarBlueDebug', '-d', '-s')
                .withPluginClasspath()

        def projectDir = runner.projectDir
        def result = runner.build()

        then:
        println result.output
        result.task(":assemble").outcome != FAILED
        //result.task(":tasks").outcome == SUCCESS
        result.task(":intentBuilderJar").outcome != FAILED
        result.task(":intentBuilderJarRed").outcome != FAILED
        result.task(":intentBuilderJarRelease").outcome != FAILED
        result.task(":intentBuilderJarBlueDebug").outcome != FAILED
        println new File(projectDir, "/build").eachFileRecurse(FILES) {
            println it
        }
        println new File(projectDir, "/build/libs").eachFileRecurse(FILES) {
            if(it.name.endsWith('.jar')) {
                def zip = new ZipFile(it)
                zip.entries().each { entry ->
                    println entry.name
                }
            }
        }
    }
}