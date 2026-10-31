# AutoBuild

AutoBuild is platform to build your CI/CD pipelines at ease.

## Requirements:
- **Functional Requirements:**
    1. **As a developer**, I should be able to define my own pipeline with low-code i.e., I should be able to define the pipeline almost in its entirity from the UI itself.
    2. **As a developer**, I should be able to connect my VCS to the AutoBuild platform.
    3. **As a developer**, I should be able to run the pipeline on specific events i.e, push, commit or schedule the pipeline to run for specific time period.
    4. **As a developer**, I should be able to see the status of build from VCS.
    5. **As a developer**, I should be able to see detailed status of build i.e., stages and logs in the application.
    6. **As a developer**, I should be able to build manually from the Application UI.
- **Technical Requirements:**
    1. Application must respond back an acknowledgement within 10 second after receiving a VCS event (Especially for GitHub as mentioned [here](https://docs.github.com/en/webhooks/using-webhooks/handling-webhook-deliveries#javascript-example)).

## Technology Stack:
- **Frontend:**
    - React
    - HTML, CSS, JavaScript
- **Backend:**
    - **Business Logic:**
        - Java, SpringBoot
    - **Virtualization:**
        - Docker
    - **Message Brocker:**
        - RabbitMQ
- **Database:**
    - MySQL
- **DevOps:**
    - Github Actions

![tech stack](./autobuild-docs/architecture/drawio/img/techstack.svg)

## Design Tools:
1. [Figma](https://www.figma.com/design/2Uv5PkfkJ2xkceuN9gf75z/Untitled?t=kS7SzIYWNZSZ6t2r-0): Figma Link.
1. [PenPot](https://design.penpot.app/#/dashboard/recent?team-id=e7c79b0d-7aa0-808c-8006-bb3c3d557ad4): Figma Alternative for UI design. 
2. [TAM](https://help.sap.com/docs/SAP_POWERDESIGNER/1cc460ad80f446e6a9d19303919ee269/c818cfa96e1b1014abb5d137d4620b1e.html): SAP specified architectural modelling standard.

## Quick Links
1. [Architecture](./autobuild-docs/architecture/architecture.md)
2. [Local Dev Environment Setup](./autobuild-docs/setup/dev-env-setup.md) 

> Note: All mentioned technologies and requirements might change in future
