// ==== Cấu hình Docker Hub ====
// Đổi thành namespace (username/organization) Docker Hub của bạn.
def DOCKERHUB_NS = 'nhanleeq'
// ID của credential kiểu "Username with password" đã tạo trong Jenkins
// (password nên dùng Docker Hub Access Token, không dùng mật khẩu tài  khoản).
def DOCKERHUB_CRED = 'dockerhub'

// Build image từ jar CI đã đóng gói, tag = <short-commit> + latest, rồi push Docker Hub.
def dockerBuildPush(String service, String ns, String credId) {
    // env.GIT_COMMIT = commit đang build = commit cuối cùng của branch.
    def sha = env.GIT_COMMIT.take(7)
    def image = "${ns}/${service}"
    withCredentials([usernamePassword(
            credentialsId: credId,
            usernameVariable: 'DH_USER',
            passwordVariable: 'DH_PASS')]) {
        sh """
            echo "\$DH_PASS" | docker login -u "\$DH_USER" --password-stdin
            docker build \
                -f ${service}/docker/Dockerfile \
                -t ${image}:${sha} \
                -t ${image}:latest \
                ${service}
            docker push ${image}:${sha}
            docker push ${image}:latest
            docker logout
        """
    }
    echo "Pushed ${image}:${sha} and ${image}:latest"
}

pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
        timestamps()
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
                    env.PRODUCT_CHANGED = 'false'
                    env.ORDER_CHANGED = 'false'
                    def changedFiles = ''
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
                        def files = changedFiles.tokenize('\n')
                        echo "Parsed ${files.size()} file(s): ${files}"

                        boolean commonChanged = false
                        boolean productChanged = false
                        boolean orderChanged = false

                        for (line in files) {
                            def f = line.trim()
                            if (f == 'pom.xml' || f == 'Jenkinsfile' ||
                                f == 'docker-compose.yml' || f.startsWith('.mvn/')) {
                                commonChanged = true
                            }
                            if (f.startsWith('product-service/')) productChanged = true
                            if (f.startsWith('order-service/'))   orderChanged   = true
                        }

                        if (commonChanged) {
                            productChanged = true
                            orderChanged = true
                        }

                        echo "commonChanged=${commonChanged} product=${productChanged} order=${orderChanged}"
                        env.PRODUCT_CHANGED = productChanged.toString()
                        env.ORDER_CHANGED = orderChanged.toString()
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

        stage('Docker Build & Push') {
            parallel {
                stage('Push Product Image') {
                    when {
                        expression {
                            env.PRODUCT_CHANGED == 'true'
                        }
                    }
                    steps {
                        script {
                            dockerBuildPush('product-service', DOCKERHUB_NS, DOCKERHUB_CRED)
                        }
                    }
                }

                stage('Push Order Image') {
                    when {
                        expression {
                            env.ORDER_CHANGED == 'true'
                        }
                    }
                    steps {
                        script {
                            dockerBuildPush('order-service', DOCKERHUB_NS, DOCKERHUB_CRED)
                        }
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