pipeline {
    agent { label 'slave' }
    options { timestamps() }
    stages {
        stage('SCM-SKIP') {
            steps {
                scmSkip(skipPattern:'.*\\[ci skip\\].*')
            }
        }
        stage('Build') {
            tools {
                jdk "OpenJDK 18"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    sh 'git fetch --tags'
                    sh 'git reset --hard'
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
