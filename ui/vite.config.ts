import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import { Plugin } from "vite";
import { rollup } from "rollup";
import { nodeResolve } from "@rollup/plugin-node-resolve";
import { minify } from "terser";
import fs from "fs/promises";
import path from "path";
import dotenv from "dotenv";

// Load environment variables
dotenv.config({ path: ".env.local" });

// Custom plugin to minify JS files from public folder
function minifyPublicJS(): Plugin {
  const processFile = async (file: string, publicDir: string) => {
    if (file.endsWith(".template.js")) {
      const inputPath = path.join(publicDir, file);
      const outputPath = path.join(
        publicDir,
        file.replace(".template.js", ".js"),
      );

      // Read the template file
      let content = await fs.readFile(inputPath, "utf-8");

      // Replace the import.meta.env references with actual values
      content = content.replace(
        "import.meta.env.VITE_API_URL",
        JSON.stringify(process.env.VITE_API_URL),
      );

      const bundle = await rollup({
        input: "virtual-entry",
        plugins: [
          {
            name: "virtual",
            resolveId(id) {
              if (id === "virtual-entry") return id;
              return null;
            },
            load(id) {
              if (id === "virtual-entry") return content;
              return null;
            },
          },
          nodeResolve(),
        ],
      });

      const { output } = await bundle.generate({ format: "iife" });
      const minified = await minify(output[0].code);

      await fs.writeFile(outputPath, minified.code);
    }
  };

  return {
    name: "minify-public-js",
    async buildStart() {
      const publicDir = path.resolve(__dirname, "public");
      const files = await fs.readdir(publicDir);

      for (const file of files) {
        await processFile(file, publicDir);
      }
    },
    async handleHotUpdate({ file, server }) {
      const publicDir = path.resolve(__dirname, "public");
      if (file.startsWith(publicDir) && file.endsWith(".template.js")) {
        const fileName = path.basename(file);
        await processFile(fileName, publicDir);
        server.ws.send({ type: "full-reload" });
        return [];
      }
    },
  };
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [minifyPublicJS()],
  server: {
    port: 3080,
  },
});
