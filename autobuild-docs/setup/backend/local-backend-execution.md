## Backend Application Execution
> **Note:** This configuration is for VS Code only.

1. Locate [backend launch.json](https://github.com/SuvabrataChowdhury/AutoBuild/blob/launch-json-template/pipeline/.vscode/launch.json.template) and rename it to just `launch.json`.
2. From VS Code `Run and Debug` (keybind SHIFT+CMD+D for Mac) select `AutoBuild: Local` and run the backend service in debug mode.
![VS Code Execution](./images/vs-code-execution.png)
    Ensure the application is running fine by hitting the endpoints through bruno.

Optional:
3. To generate Protobuf classes locally (without Docker):
  From the `pipeline/` directory, run: `mvn compile -s maven-settings.xml`
  Without `-s maven-settings.xml`, Maven falls back to repository mentioned at `~/.m2/repository/settings.xml` and might fail if it does not have the necessary dependencies. Mentioning it like that ensures the dependencies are pulled from open maven repository.
  (The files are generated at the `generate-sources` and writes the Java files into target/generated-sources/. Then compile picks them up alongside your hand-written code and compiles everything together.)
4. Recommended VS Code extension for .proto files:
  **Buf** (`bufbuild.vscode-buf`) — syntax highlighting and linting
  https://marketplace.visualstudio.com/items?itemName=bufbuild.vscode-buf
5. To understand Protocol Buffers (the format used for DTO generation):
   Protocol Buffers Crash Course — https://youtu.be/46O73On0gyI
