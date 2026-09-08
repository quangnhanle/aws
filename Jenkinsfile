pipeline {
    options {
        skipDefaultCheckout(true)
        timestamps()
    }
    environment {
        PRODUCT_CHANGED = 'false'
        ORDER_CHANGED = 'false'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect changes') {
            steps {
                script {
                    if (env.CHANGE_TARGET) {
                        echo "Pull Request target: ${env.CHANGE_TARGET}"
                        sh """
                            git fetch origin \
                              +refs/heads/${env.CHANGE_TARGET}:refs/remotes/origin/${env.CHANGE_TARGET}
                        """

                        changedFiles = sh(
                            script: """
                                git diff --name-only \
                                origin/${env.CHANGE_TARGET}...HEAD
                            """,
                            returnStdout: true
                        ).trim()

                    } else if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT) {
                        changedFiles = sh(
                            script: """
                                git diff --name-only \
                                ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT} HEAD
                            """,
                            returnStdout: true
                        ).trim()

                    } else if (env.GIT_PREVIOUS_COMMIT) {
                        changedFiles = sh(
                            script: """
                                git diff --name-only \
                                ${env.GIT_PREVIOUS_COMMIT} HEAD
                            """,
                            returnStdout: true
                        ).trim()
                    } else {
                        env.PRODUCT_CHANGED = 'true'
                        env.ORDER_CHANGED = 'true'
                    }

                    echo """
                    Changed files:
                    ${changedFiles}
                    """

                    if (changedFiles) {

                        def files = changedFiles.split('\n')

                        boolean commonChanged = files.any {
                            it == 'pom.xml' ||
                            it == 'Jenkinsfile' ||
                            it == 'docker-compose.yml' ||
                            it.startsWith('.mvn/')
                        }

                        boolean productChanged = files.any {
                            it.startsWith('product-service/')
                        }

                        boolean orderChanged = files.any {
                            it.startsWith('order-service/')
                        }

                        if (commonChanged) {
                            productChanged = true
                            orderChanged = true
                        }

                        env.PRODUCT_CHANGED =
                            productChanged.toString()

                        env.ORDER_CHANGED =
                            orderChanged.toString()
                    }
                    echo """
                        Services to build:

                        PRODUCT_CHANGED = ${env.PRODUCT_CHANGED}
                        ORDER_CHANGED   = ${env.ORDER_CHANGED}
                    """
                }
            }
        }

        stage('Test') {
            parallel {
                stage('Test Product Service') {
                    when {
                        expression {
                            env.PRODUCT_CHANGED == 'true'
                        }
                    }
                    steps {
                        sh '''
                            mvn -B \
                                -pl product-service \
                                -am \
                                test
                        '''
                    }
                    post {
                        always {
                            junit(
                                testResults:
                                    'product-service/target/surefire-reports/*.xml',
                                allowEmptyResults: true
                            )

//                            recordCoverage(
//                                id: 'product-coverage',
//                                name: 'Product Service Coverage',
//                                enabledForFailure: true,
//                                failOnError: false,
//                                tools: [[
//                                    parser: 'JACOCO',
//                                    pattern:
//                                        'product-service/target/site/jacoco/jacoco.xml'
//                                ]]
//                            )
                        }
                    }
                }

                stage('Test Order Service') {
                    when {
                        expression {
                            env.ORDER_CHANGED == 'true'
                        }
                    }
                    steps {
                        sh '''
                            mvn -B \
                                -pl order-service \
                                -am \
                                test
                        '''
                    }

                    post {
                        always {
                            junit(
                                testResults:
                                    'order-service/target/surefire-reports/*.xml',
                                allowEmptyResults: true
                            )
//                            recordCoverage(
//                                id: 'order-coverage',
//                                name: 'Order Service Coverage',
//                                enabledForFailure: true,
//                                failOnError: false,
//                                tools: [[
//                                    parser: 'JACOCO',
//                                    pattern:
//                                        'order-service/target/site/jacoco/jacoco.xml'
//                                ]]
//                            )
                        }
                    }
                }
            }
        }

        stage('Build') {
            parallel {
                 stage('Build Product Service') {
                     when {
                         expression {
                             env.PRODUCT_CHANGED == 'true'
                         }
                     }

                     steps {
                         sh '''
                             mvn -B \
                                 -pl product-service \
                                 -am \
                                 package \
                                 -DskipTests
                         '''
                         archiveArtifacts(
                             artifacts:
                                 'product-service/target/*.jar',
                             fingerprint: true
                         )
                     }
                 }

                 stage('Build Order Service') {
                     when {
                         expression {
                             env.ORDER_CHANGED == 'true'
                         }
                     }
                     steps {
                         sh '''
                             mvn -B \
                                 -pl order-service \
                                 -am \
                                 package \
                                 -DskipTests
                         '''
                         archiveArtifacts(
                             artifacts:
                                 'order-service/target/*.jar',
                             fingerprint: true
                         )
                     }
                }
            }
        }
    }

    post {
        success {
            echo 'CI PASSED'
        }
        failure {
            echo 'CI FAILED'
        }
    }
}