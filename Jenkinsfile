pipeline {
    agent { label 'slave' }
    options { timestamps() }
    stages {
        stage('SCM-SKIP') {
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
            }
        }
        stage('Build') {
            tools {
                jdk "OpenJDK 16"
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
                    archiveArtifacts "**/build/libs/*-all.jar"
                }
                failure {
                    cleanWs()
                }
            }
        }
    }
}
