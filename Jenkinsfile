#!/usr/bin/env groovy

node('rhel7'){
	def recipientList = 'jbosstools-builds@lists.jboss.org'
	def javaHome = tool 'openjdk-11'
	env.JAVA_HOME = "${javaHome}"

	try {
		stage('Checkout repo') {
			deleteDir()
			git url: 'https://github.com/redhat-developer/intellij-dependency-analytics',
				branch: "${sha1}"
		}

		def props = readProperties file: 'gradle.properties'
		def isSnapshot = props['projectVersion'].contains('-SNAPSHOT')
		def version = isSnapshot?props['projectVersion'].replace('-SNAPSHOT', ".${env.BUILD_NUMBER}"):props['projectVersion'] + ".${env.BUILD_NUMBER}"

        // github user and token are required for consuming the exhort-java-api module from GHPR in build-time
        withCredentials([[$class: 'StringBinding', credentialsId: 'rhdevelopersci-github-token', variable: 'GITHUB_TOKEN']]) {
            stage('Build') {
                sh "./gradlew assemble  -PprojectVersion=${version} -Pgpr.username=rhdevelopers-ci -Pgpr.token=${GITHUB_TOKEN}"
            }

            stage('Package') {
                sh "./gradlew buildPlugin -PprojectVersion=${version} -Pgpr.username=rhdevelopers-ci -Pgpr.token=${GITHUB_TOKEN}"
            }
        }

		if(params.UPLOAD_LOCATION) {
			stage('Upload') {
				def filesToPush = findFiles(glob: '**/*.zip')
				sh "sftp -C ${UPLOAD_LOCATION}/snapshots/intellij-dependency-analytics/ <<< \$'put -p \"${filesToPush[0].path}\"'"
				stash name:'zip', includes:filesToPush[0].path
			}
		}

		if(publishToMarketPlace.equals('true')){
			timeout(time:5, unit:'DAYS') {
				input message:'Approve deployment?', submitter: 'jmaury, tfigenbl, zgrinber, vbelouso, xiezhang'
			}

			def channel = isSnapshot?"nightly":"stable"

			stage("Publish to Marketplace") {
				unstash 'zip'
				// github user and token are required for consuming the exhort-java-api module from GHPR in build-time
				withCredentials([[$class: 'StringBinding', credentialsId: 'rhdevelopersci-github-token', variable: 'GITHUB_TOKEN']]) {
                    withCredentials([[$class: 'StringBinding', credentialsId: 'JetBrains marketplace token', variable: 'TOKEN']]) {
                        sh "./gradlew publishPlugin -PjetBrainsToken=${TOKEN} -PprojectVersion=${version} -PjetBrainsChannel=${channel} -Pgpr.username=rhdevelopers-ci -Pgpr.token=${GITHUB_TOKEN}"
                    }
                }
				archive includes:"**.zip"

				if (!isSnapshot) {
					stage("Promote the build to stable") {
						def zip = findFiles(glob: '**/*.zip')
						sh "sftp -C ${UPLOAD_LOCATION}/snapshots/intellij-dependency-analytics/ <<< \$'put -p \"${zip[0].path}\"'"
						currentBuild.keepLog = true
						currentBuild.description = "${version}"
					}
				}
			}
		}
	} catch (any) {
		currentBuild.result = 'FAILURE'
		step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "${recipientList}", sendToIndividuals: true])
		throw any
	}
}
