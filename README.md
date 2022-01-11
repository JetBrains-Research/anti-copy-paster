# AntiCopyPaster

AntiCopyPaster is a plugin for IntelliJ IDEA that tracks the copying and pasting carried out by the developer and suggests extracting duplicates into a new method as soon as they are introduced in the code.

**Important**: _Please note that AntiCopyPaster is a prototype and a work in progress. We would appreciate any feedback on the concept itself, as well as the implementation._

### How to install

AntiCopyPaster requires IntelliJ IDEA of version 2021.3 to work. To install the plugin:

1. Download the pre-built version of the plugin from [here](https://drive.google.com/file/d/10hiL4aMq9gtnlGeaiXJ7_2NOxGSC4KK_/view?usp=sharing); 
2. Open IntelliJ IDEA and go to `File`/`Settings`/`Plugins`;
3. Select the gear icon, and choose `Install Plugin from Disk...`;
4. Choose the downloaded ZIP archive;
5. Click `Apply`;
6. Restart the IDE.

### How it works

The plugin monitors the copying and pasting that takes place inside the IDE. As soon as a code fragment is pasted, the plugin checks if it introduces code duplication, and if it does, the plugin calculates a set of code metrics for it, and a pre-installed Gradient Boosting Classifier model makes a decision whether this piece of code is suitable for `Extract Method` refactoring. If it is, the plugin suggests the developer to perform the `Extract Method` refactoring and applies the refactoring if necessary.

The scripts and tools that were used for data gathering and model training could be found [here](https://github.com/JetBrains-Research/extract-method-experiments).

## Contacts

If you have any questions or propositions, do not hesitate to contact Yaroslav Golubev at yaroslav.golubev@jetbrains.com.
