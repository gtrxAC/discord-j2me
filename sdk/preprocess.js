const fs = require('fs');
const path = require('path');
const readline = require('readline');

const defines = [];

function preprocessFile(inputFile) {
  inputFile = path.resolve(inputFile);

  // Create directory for output file if needed
  const dir = path.dirname(inputFile).replace(process.cwd(), "build");
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  
  const defineSet = new Set(defines);
  const input = fs.createReadStream(inputFile);
  const outputFile = inputFile.replace(process.cwd(), "build");
  const output = fs.createWriteStream(outputFile);
  const rl = readline.createInterface({ input });

  const skipStack = []; // Stack to track active/inactive blocks
  let shouldSkip = false; // Indicates if we are in a block that should be skipped

  const emptyLine = () => {
    // Empty lines are not accepted in manifest files
    if (path.basename(inputFile).toLowerCase() != "manifest.mf") output.write("\n");
  }

  rl.on('line', (line) => {
    lineTrim = line.trimEnd();

    // Check for //#ifdef
    if (/^\/\/#ifdef\s+\S+$/.test(lineTrim)) {
      const lineWords = lineTrim.split(/\s+/g);
      const defineName = lineWords[lineWords.length - 1];
      
      // Determine if we should skip this block based on the define
      const isActiveBlock = defineSet.has(defineName) && !shouldSkip;
      skipStack.push(isActiveBlock);

      // Update shouldSkip based on this block
      shouldSkip = shouldSkip || !isActiveBlock;

      // Write empty line (so that error messages will have the correct line number)
      emptyLine();
      return;
    }

    // Check for //#ifndef (same as ifdef, but inverse condition)
    if (/^\/\/#ifndef\s+\S+$/.test(lineTrim)) {
      const lineWords = lineTrim.split(/\s+/g);
      const defineName = lineWords[lineWords.length - 1];
      
      // Determine if we should skip this block based on the define
      const isActiveBlock = !defineSet.has(defineName) && !shouldSkip;
      skipStack.push(isActiveBlock);

      // Update shouldSkip based on this block
      shouldSkip = shouldSkip || !isActiveBlock;

      // Write empty line (so that error messages will have the correct line number)
      emptyLine();
      return;
    }

    // Check for //#else
    if (/^\/\/#else/.test(lineTrim)) {
      if (skipStack.length > 0) {
        skipStack.push(!skipStack.pop());
        shouldSkip = skipStack.some((skip) => !skip);
      }

      emptyLine();
      return;
    }

    // Check for //#endif
    if (/^\/\/#endif/.test(lineTrim)) {
      if (skipStack.length > 0) {
        skipStack.pop(); // Pop the last //#ifdef context

        // Update shouldSkip to reflect the new stack state
        shouldSkip = skipStack.some((skip) => !skip);
      }

      emptyLine();
      return;
    }

    // Write the line to the output if not skipping
    if (!shouldSkip) {
      output.write(line + '\n');
    } else {
      emptyLine();
    }
  });

  rl.on('close', () => {
    output.end();
  });
}

const files = [];

process.argv.slice(2).forEach(arg => {
  if (arg.startsWith("-D")) {
    defines.push(arg.slice(2).trim());
  } else {
    files.push(arg);
  }
})

files.forEach(preprocessFile)