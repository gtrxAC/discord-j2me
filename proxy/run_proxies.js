try {
    require('dotenv').config();
}
catch (e) {
    cp.spawnSync("npm", ["i"], SPAWN_OPTIONS);
    require('dotenv').config();
}

const fs = require('fs');
const cp = require('child_process');
const net = require('net');

const API_PROXY_PORT = process.env.PORT || 8080;
const GATEWAY_PROXY_PORT = process.env.GATEWAY_PROXY_PORT || 8081;
const GATEWAY_PROXY_GIT = "https://github.com/gtrxAC/discord-j2me-server";
const SPAWN_OPTIONS = { stdio: 'inherit' };

function npmInstallIfNeeded() {
    try {
        fs.accessSync("node_modules", fs.constants.R_OK);
    } catch (e) {
        cp.spawnSync("npm", ["i"], SPAWN_OPTIONS);
    }
}

function printLine() {
    console.log("_".repeat(80) + "\n");
}

function printHeading(text) {
    printLine();
    console.log(" " + text);
    printLine();
}

// check that gateway proxy exists, try to download via git if needed
try {
    fs.accessSync("gateway_proxy", fs.constants.R_OK);
} catch (e) {
    console.log("Downloading gateway proxy...");
    cp.spawnSync("git", ["clone", GATEWAY_PROXY_GIT, "gateway_proxy"], SPAWN_OPTIONS);

    try {
        fs.accessSync("gateway_proxy", fs.constants.R_OK);
    } catch (ee) {
        console.error('');
        console.error("Failed to automatically download gateway proxy as you don't have git installed.");
        console.error('');
        console.error("Cannot access 'gateway_proxy' folder. That folder probably doesn't exist.");
        console.error(`Check that the gateway proxy (${GATEWAY_PROXY_GIT})`);
        console.error("is downloaded to the 'gateway_proxy' folder inside the 'proxy' folder.");
        process.exit(1);
    }
}

printHeading("Setting up proxies");
process.chdir("gateway_proxy");
npmInstallIfNeeded();
cp.spawnSync("npm", ["run", "build"], SPAWN_OPTIONS);

printHeading("Starting gateway proxy");
cp.spawn("node", ["out"], SPAWN_OPTIONS);

setTimeout(() => {
    printHeading("Starting API proxy");
    process.chdir("..");
    cp.spawn("node", ["index.js"], SPAWN_OPTIONS);
}, 500);

setTimeout(() => {
    if (process.env.IN_DOCKER) {
        printStartedMessage();
    } else {
        // https://stackoverflow.com/questions/3653065/get-local-ip-address-in-node-js
        const socket = net.createConnection(80, 'www.google.com');
        socket.on('connect', function() {
            printStartedMessage(socket.address().address);
            socket.end();
        });
        socket.on('error', function(e) {
            printStartedMessage();
        });
    }
}, 1500);

function printStartedMessage(ip) {
    const ipWasFound = Boolean(ip);
    if (!ip) ip = "(your IP address)";

    printLine();
    console.log(" Proxies started.");
    console.log(" Change your connection URLs in the app accordingly.");
    console.log("");
    console.log(" If your J2ME device is connected to the same Wi-Fi as this PC,");
    console.log(" use your LOCAL IP address:");
    console.log("");
    console.log(` API URL:     http://${ip}:${API_PROXY_PORT}`);
    console.log(` Gateway URL: socket://${ip}:${GATEWAY_PROXY_PORT}`);
    if (!ipWasFound) {
        console.log("");
        console.log(" Your LOCAL IP address can be found by running ipconfig (Windows) or");
        console.log(" ifconfig (Linux) in a command prompt or terminal. It often begins with 192.168.")
    }
    console.log("");
    console.log(" If your J2ME device is not on the same Wi-Fi, you will need to make sure that");
    console.log(" the server is accessible from the public internet.");
    console.log(" - If the proxy is running on a VPS server, use your PUBLIC IP address, which");
    console.log("   you can usually find from your VPS provider's dashboard.");
    console.log(` - If you're on a PC and you have forwarded TCP ports ${API_PROXY_PORT} and ${GATEWAY_PROXY_PORT} on your`);
    console.log(`   router, use your PUBLIC IP address, which you can find by searching "what is`)
    console.log(`   my ip".`);
    console.log(` - If you're not using a VPS and you can't forward ports, you must use an API`);
    // https://github.com/NovelProfessor/whatsapp-server#get-a-public-endpoint-from-api-gateway
    console.log(`   gateway service such as ngrok, localtunnel.me, pinggy.io, onionpipe, or`);
    console.log(`   tunnelmole, and use the IP address provided by them. Note that with an API`);
    console.log(`   gateway service, you might not be able to use the gateway (socket) connection`);
    console.log(`   in Discord J2ME.`);
    printLine();
}