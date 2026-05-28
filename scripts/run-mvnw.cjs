const { spawn } = require("node:child_process");
const path = require("node:path");

const isWindows = process.platform === "win32";
const mvnwName = isWindows ? "mvnw.cmd" : "mvnw";
const backendDir = path.join(__dirname, "..", "backend");
const mvnwPath = path.join(backendDir, mvnwName);

const args = process.argv.slice(2);

const command = isWindows ? mvnwPath : "sh";
const commandArgs = isWindows ? args : [mvnwPath, ...args];

const child = spawn(command, commandArgs, {
  cwd: backendDir,
  stdio: "inherit",
  shell: isWindows,
});

child.on("exit", (code) => {
  process.exit(code ?? 1);
});

child.on("error", (error) => {
  console.error("Failed to start Maven wrapper:", error.message);
  process.exit(1);
});
