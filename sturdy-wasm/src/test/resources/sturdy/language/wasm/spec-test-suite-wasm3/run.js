const fs = require('fs');

(async () => {
  const bytes = fs.readFileSync('mytry.wast');

  // Instantiate the module
  const wasmModule = await WebAssembly.instantiate(new Uint8Array(bytes));
  const exports = wasmModule.instance.exports;

  // Call an exported function
  console.log(exports.add(5, 7)); // Replace 'add' with your exported function
})();
