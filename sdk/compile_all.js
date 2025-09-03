const cp = require('child_process');
const targets = require('../build.json');

const compileScript = (process.platform == 'win32') ? "sdk\\compile.bat" : "sdk/compile.sh";
const classpathJoiner = (process.platform == 'win32') ? ";" : ":";

const compileTarget = (target) => {
  return new Promise((resolve, reject) => {
    process.env.JAR_NAME = target.name;
    process.env.DEFINES = "-D" + target.defines.join(" -D");
    process.env.BOOTCLASSPATH = target.bootclasspath.join(classpathJoiner);
    process.env.EXCLUDES = (target.excludes || []).join(" ");

    console.log(`${"_".repeat(80)}\n`)
    console.log(` Compiling: ${target.name}`)
    console.log(`${"_".repeat(80)}\n`)

    const compileProcess = cp.spawn(compileScript, [], { stdio: 'inherit', shell: true });

    compileProcess.on('close', (code) => {
      if (code !== 0) {
        console.log("Compilation failed");
        reject(new Error("Compilation failed"));
      } else {
        resolve();
      }
    });
  });
};

const compileAll = async () => {
  for (const target of targets) {
    try {
      await compileTarget(target);
    } catch (error) {
      process.exit(1);
    }
  }
};

compileAll();