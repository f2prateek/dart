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
    File buildFileModule1
    File module1ManifestFile
    File testClass1
    File buildFileModuleNavigation1
    File module1NavigationManifestFile
    File testNavigationModel1

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        buildFile = testProjectDir.newFile('build.gradle')

        testProjectDir.newFolder('module1')
        testProjectDir.newFolder('module1-navigation')
        buildFileModule1 = testProjectDir.newFile('module1/build.gradle')
        buildFileModuleNavigation1 = testProjectDir.newFile('module1-navigation/build.gradle')

        testProjectDir.newFolder('module1', 'src','main', 'java', 'module1')
        module1ManifestFile = testProjectDir.newFile('module1/src/main/AndroidManifest.xml')
        testClass1 = testProjectDir.newFile('module1/src/main/java/module1/FooActivity.java')
        testProjectDir.newFolder('module1-navigation', 'src','main', 'java', 'module1')
        module1NavigationManifestFile = testProjectDir.newFile('module1-navigation/src/main/AndroidManifest.xml')
        testNavigationModel1 = testProjectDir.newFile('module1-navigation/src/main/java/module1/FooActivityNavigationModel.java')
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
        settingsFile << """
        include(':module1')
        include(':module1-navigation')
        """

        buildFileModule1 << """
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
            navigatorPackageName = "module1"
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
          implementation project(':module1-navigation')
        }
        
        """

        module1ManifestFile << """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="module1">
        
          <application
              android:label="Module1"
              android:name="Test"/>
        </manifest>
        """

        testClass1 << """
        package module1;
        
        import android.app.Activity;
        import android.os.Bundle;
        import android.content.Intent;
        
        class FooActivity extends Activity {
          
          @Override
          public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            FooActivityNavigationModel foo = new FooActivityNavigationModel();
            Intent intent = HensonNavigator.gotoFooActivity(this)
              .s("s")
              .build();
          }
        }
        """

        buildFileModuleNavigation1 << """
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
            id 'com.android.library'
        }

        android {
            compileSdkVersion 26
            defaultConfig {
                minSdkVersion 26
                targetSdkVersion 26
                versionCode 1
                versionName '1.0.0'
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
            implementation 'com.f2prateek.dart:dart-annotations:3.0.0-RC3-SNAPSHOT'
            implementation 'com.f2prateek.dart:dart:3.0.0-RC3-SNAPSHOT'
            implementation 'com.f2prateek.dart:henson:3.0.0-RC3-SNAPSHOT'
            annotationProcessor 'com.f2prateek.dart:dart-processor:3.0.0-RC3-SNAPSHOT'
            annotationProcessor 'com.f2prateek.dart:henson-processor:3.0.0-RC3-SNAPSHOT'
        }

        """
        module1NavigationManifestFile << """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="module1_navigation">        
        </manifest>
        """

        testNavigationModel1 << """
        package module1;
        
        import dart.BindExtra;
        import dart.DartModel;
        
        @DartModel()
        class FooActivityNavigationModel {
          @BindExtra String s;
        }
        """

        when:
        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('--no-build-cache', 'clean', ':module1:assemble', '-d', '-s')
                .withPluginClasspath()

        def projectDir = runner.projectDir
        def result = runner.build()

        then:
        println result.output
        result.task(":module1:assemble").outcome != FAILED

        testJarsContent(projectDir)
    }

    boolean testJarsContent(projectDir) {
        new File(projectDir, "module1-navigation/build/intermediates/bundles/debug").eachFileRecurse(FILES) { file ->
            if (file.name.endsWith('.jar')) {
                println "Testing jar: ${file.name}"
                def content = getJarContent(file)
                println "Jar content: ${content}"
                assert content.contains("META-INF/")
                assert content.contains("META-INF/MANIFEST.MF")
                assert content.contains("module1/")
                assert content.contains("module1/FooActivityNavigationModel.class")
                assert content.contains("module1/FooActivityNavigationModel__ExtraBinder.class")
                assert content.contains("module1/Henson\$1.class")
                assert content.contains("module1/Henson\$WithContextSetState.class")
                assert content.contains("module1/Henson.class")
                assert content.contains("module1/FooActivityNavigationModel__IntentBuilder\$AllSet.class")
                assert content.contains("module1/FooActivityNavigationModel__IntentBuilder\$InitialState.class")
                assert content.contains("module1/FooActivityNavigationModel__IntentBuilder.class")
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
