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
        srcNavigationMain = testProjectDir.newFile('src/navigation/main/java/test/FooActivityNavigationModel.java')
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
        import android.content.Intent;
        
        class FooActivity extends Activity {
          
          @Override
          public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            FooActivityNavigationModel foo = new FooActivityNavigationModel();
            Intent intent = HensonNavigator.gotoFooActivityNavigationModel(this)
            .s("s")
            .build();
          }
        }
        """
        srcNavigationMain << """
        package test;
        
        import dart.BindExtra;
        import dart.DartModel;
        
        @DartModel()
        class FooActivityNavigationModel {
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
                mavenCentral()
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
        
        henson {
            navigatorPackageName = "test"
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
            mavenCentral()
            maven {
              url 'https://oss.sonatype.org/content/repositories/snapshots'
            }
        }
        
        dependencies {
          navigationApiOfSelf()
        }
        
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('--no-build-cache', 'tasks', '--all', '-d', '-s')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains("navigationApiCompileJava")
        result.output.contains("navigationApiJar")

        when:
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('--no-build-cache', 'dependencies', '-d', '-s')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains("navigationApi")
        result.output.contains("navigationImplementation")
        result.output.contains("navigationAnnotationProcessor")
        result.output.contains("navigationCompileOnly")

        when:
        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('--no-build-cache', 'clean', 'navigationApiJar', 'assemble', '-d', '-s')
                .withPluginClasspath()

        def projectDir = runner.projectDir
        result = runner.build()

        then:
        println result.output
        result.task(":assemble").outcome != FAILED
        result.task(":navigationApiJar").outcome != FAILED

        testJarsContent(projectDir)

        new File(testProjectDir.root, 'src/blueDebug/java/test/HensonNavigator.java').exists()
        new File(testProjectDir.root, 'src/redRelease/java/test/HensonNavigator.java').exists()
    }

    boolean testJarsContent(projectDir) {
        new File(projectDir, "/build/libs").eachFileRecurse(FILES) { file ->
            if (file.name.endsWith('.jar')) {
                println "Testing jar: ${file.name}"
                def content = getJarContent(file)
                println "Jar content: ${content}"
                assert content.contains("META-INF/")
                assert content.contains("META-INF/MANIFEST.MF")
                assert content.contains("test/")
                assert content.contains("test/FooActivityNavigationModel.class")
                assert content.contains("test/FooActivityNavigationModel__ExtraBinder.class")
                assert content.contains("test/Henson\$1.class")
                assert content.contains("test/Henson\$WithContextSetState.class")
                assert content.contains("test/Henson.class")
                assert content.contains("test/FooActivityNavigationModel__IntentBuilder\$AllSet.class")
                assert content.contains("test/FooActivityNavigationModel__IntentBuilder.class")
            }
        }
        true
    }

    List<String> getJarContent(file) {
        def List<String> result
        if(file.name.endsWith('.jar')) {
            result = new ArrayList<>()
            def zip = new ZipFile(file)
            zip.entries().each { entry ->
                result.add(entry.name)
            }
        }
        result
    }
}
