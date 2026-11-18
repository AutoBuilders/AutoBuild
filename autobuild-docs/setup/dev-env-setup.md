# Dev Environment Setup

Ensure you have atleast JAVA 17, Node 22 and Apache Maven 3.9 installed and configured.

## Steps
1. Clone [AutoBuild](https://github.com/SuvabrataChowdhury/AutoBuild)
2. From `AutoBuild/pipeline` execute
    ```bash
    mvn clean install
    ```
3. From `AutoBuild/pipeline-ui` execute
    ```bash
    npm ci
    ```
See [Local Backend Execution](./backend/local-backend-execution.md) for running the application locally.
See [Running Backend Container](./backend/running-backend-containers.md) for running containerized backend with db.

For the ease of local development install [VS Code](https://code.visualstudio.com/) and the following extensions,
1. [JAVA Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
2. [Docker Extension Pack](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-docker)
3. [ESLint](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint)

## Running the Docs Site Locally

From `AutoBuild/autobuild-docs-site` execute:

```bash
npm ci
npm run start
```

The docs site will be available at [http://localhost:3000/AutoBuild](http://localhost:3000/AutoBuild).