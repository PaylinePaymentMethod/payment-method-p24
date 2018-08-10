pipeline {
    agent any

    options {
      gitLabConnection('GitlabConnection')
      gitlabBuilds(builds: ["Assemble", "Test"])
      disableConcurrentBuilds()
      buildDiscarder(logRotator(numToKeepStr:'10'))
    }
    environment {
        versionInGradle = sh(script: './gradlew -q printVersion', returnStdout: true).trim()
    }
    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    stages {
        stage('Assemble') {
            steps {
                gitlabCommitStatus("Assemble") {
                    sh './gradlew clean assemble'
                }
            }
            post {
                failure {
                    updateGitlabCommitStatus name: 'Build', state: 'failed'
                    slackSend channel: '#devteam', color: 'danger', message: "Assemble ${currentBuild.fullDisplayName} KO"
                }
                success {
                    updateGitlabCommitStatus name: 'Build', state: 'success'
                }
            }
        }

        stage('Test') {
            steps {
                gitlabCommitStatus("Test") {
                    sh './gradlew test --continue'
                }
            }
            post {
                always {
		  catchError {
                    junit '**/build/test-results/**/*.xml'
		  }
                }
                failure {
                    updateGitlabCommitStatus name: 'Test', state: 'failed'
                    slackSend channel: '#devteam', color: 'danger', message: "Tests ${currentBuild.fullDisplayName} KO"
                }
                success {
                    updateGitlabCommitStatus name: 'Test', state: 'success'
                }
            }
        }
        stage("Dependency Check") {
            steps {
                sh './gradlew dependencyCheckAnalyze --info'
                dependencyCheckPublisher canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/build/reports/dependency-check-report.xml', unHealthy: ''
            }
         }
        stage ('Publication & Sonar') {
            parallel {
                stage('Publication sur Nexus') {
                    when {
                        anyOf { branch 'master'; branch 'develop'; branch "release/*" }
                    }
                    steps {
                        gitlabCommitStatus('Publication sur Nexus') {
                            sh './gradlew publish'
                        }
                    }
                    post {
                        failure {
                            slackSend channel: '#devteam', color: 'danger', message: "Publication ${currentBuild.fullDisplayName} KO"
                        }
                        success {
                            slackSend channel: '#devteam', color: 'good', message: "Publication ${currentBuild.fullDisplayName} (version V${versionInGradle}) OK"
                            step([$class: 'JiraIssueUpdater', issueSelector: [$class: 'DefaultIssueSelector'], scm: scm])
                        }
                    }
                }
                stage('SonarQube') {
                    when { not { branch 'master' } }
                    steps {
                        withSonarQubeEnv('SonarMonext') {
                            script {
                                if (BRANCH_NAME == 'develop') {
                                    sh './gradlew sonarqube -Dsonar.branch.name=${BRANCH_NAME}  --info --stacktrace'
                                }
                                if (BRANCH_NAME != 'develop') {
                                    sh './gradlew sonarqube  -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.branch.target=develop --info --stacktrace'
                                }
                            }
                        }
                    }
                    post {
                        failure {
                            slackSend channel: '#devteam', color: 'danger', message: "Analyse Sonar ${currentBuild.fullDisplayName} KO"
                        }
                    }
                }
            }
        }
        //stage ('Tag Git') {
            //On arrive pas tag depuis le gitlab
	    //when {
            //    anyOf { branch 'master'; branch 'develop'; branch "release/*" }
            //}
            //steps {
             //   sh "git tag -f V${versionInGradle}"
             //   sh "git push origin -f V${versionInGradle}"
            //}
       // }
    }
}
