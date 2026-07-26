def call() {
    properties([
        parameters([
            choice(
                name: 'ENVIRONMENT',
                choices: ['local', 'aks'],
                description: 'Select deployment target'
            )
        ])
    ])
}