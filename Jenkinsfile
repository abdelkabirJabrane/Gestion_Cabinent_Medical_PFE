pipeline {
    agent any

    stages {
        stage('Trigger Microservices Pipelines') {
            parallel {
                stage('config-service') {
                    steps {
                        build job: 'config-service-pipeline', wait: true
                    }
                }
                stage('discovery-service') {
                    steps {
                        build job: 'discovery-service-pipeline', wait: true
                    }
                }
                stage('api-gateway') {
                    steps {
                        build job: 'api-gateway-pipeline', wait: true
                    }
                }
                stage('auth-service') {
                    steps {
                        build job: 'auth-service-pipeline', wait: true
                    }
                }
                stage('patient-service') {
                    steps {
                        build job: 'patient-service-pipeline', wait: true
                    }
                }
                stage('appointment-service') {
                    steps {
                        build job: 'appointment-service-pipeline', wait: true
                    }
                }
                stage('billing-service') {
                    steps {
                        build job: 'billing-service-pipeline', wait: true
                    }
                }
                stage('medical-record-service') {
                    steps {
                        build job: 'medical-record-service-pipeline', wait: true
                    }
                }
                stage('ordonnance-service') {
                    steps {
                        build job: 'ordonnance-service-pipeline', wait: true
                    }
                }
                stage('ai-service') {
                    steps {
                        build job: 'ai-service-pipeline', wait: true
                    }
                }
            }
        }
    }
}
