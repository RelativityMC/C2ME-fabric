pipeline {
    agent { label 'slave' }
    options { timestamps() }
    stages {
        stage('Build') {
            tools {
                jdk "OpenJDK 11"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                    sh 'chmod +x ./gradlew'
                    sh './gradlew clean build'
                }
            }
            post {
                success {
                    archiveArtifacts "build/libs/*.jar"
                }
                failure {
                    cleanWs()
                }
            }
        }
    }
}