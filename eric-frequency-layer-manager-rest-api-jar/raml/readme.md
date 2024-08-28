RAML
        ----
        * RAML files are text files, although it is recommended to open them with Atom editor,
<https://atom.io/> and the API Workbench plugin, <https://atom.io/packages/api-workbench>
        * Intellij has a plugin called RAML Plugin For IntelliJ <https://plugins.jetbrains.com/plugin/8459-raml-plugin-for-intellij>
        * To generate the HTML files you need nodejs at least version v5.1.0, <https://nodejs.org/en/>.
        * (Advanced Users Only) This might conflict with the version required for working with UI. To solve that it is advised to use nvm (node version manager) and you will be able to swap different node versions. <https://github.com/creationix/nvm>, or <https://github.com/coreybutler/nvm-windows>. Install the raml2html npm document generator <https://www.npmjs.com/package/raml2html>.
        * Once npm is installed, you can run:
        * npm i -g raml2html\@3.0.1
        * Then to generate the HTML run
        * raml2html flm-algorithm.raml > flm-algorithm.html
        * raml2html eric-frequency-layer-manager-rest-api-jar/raml/v1/eric-frequency-layer-manager.raml > eric-frequency-layer-manager-rest-api-jar/raml/v1/eric-frequency-layer-manager.html