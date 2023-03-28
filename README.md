# AntiCopyPaster

AntiCopyPaster is a plugin for IntelliJ IDEA that tracks the copying and pasting carried out by the developer and suggests extracting duplicates into a new method as soon as they are introduced in the code.

**Important**: _Please note that AntiCopyPaster is a prototype and a work in progress. We would appreciate any feedback on the concept itself, as well as the implementation._

### How to install

AntiCopyPaster requires IntelliJ IDEA of version 2022.3 to work. To install the plugin:

1. Download the pre-built version of the plugin from [here](https://drive.google.com/file/d/1ULBHbUmoiM3qE-qxomSYzWVlu7aiaqZc/view?usp=share_link);
2. Open IntelliJ IDEA and go to `File`/`Settings`/`Plugins`;
3. Select the gear icon, and choose `Install Plugin from Disk...`;
4. Choose the downloaded ZIP archive;
5. Click `Apply`;
6. Restart the IDE.

### How it works

The plugin monitors the copying and pasting that takes place inside the IDE. As soon as a code fragment is pasted, the plugin checks if it introduces code duplication, and if it does, the plugin calculates a set of code metrics for it, and a pre-installed CNN model makes a decision whether this piece of code is suitable for `Extract Method` refactoring. If it is, the plugin suggests the developer to perform the `Extract Method` refactoring and applies the refactoring if necessary.

The scripts and tools that were used for data gathering and model training could be found [here](https://github.com/JetBrains-Research/extract-method-experiments).

### Experiments

The tool validation and embedded models are available here: https://github.com/JetBrains-Research/extract-method-experiments.

## Contacts

If you have any questions or propositions, do not hesitate to contact Yaroslav Golubev at yaroslav.golubev@jetbrains.com.

### How to cite?
Please, use the following bibtex entry:

```tex
@inproceedings{alomar2022anticopypaster,
  title={AntiCopyPaster: Extracting Code Duplicates As Soon As They Are Introduced in the IDE},
  author={Alomar, Eman Abdullah and Ivanov, Anton and Kurbatova, Zarina and Golubev, Yaroslav and Mkaouer, Mohamed Wiem and Ouni, Ali and Bryksin, Timofey and Nguyen, Le and Kini, Amit and Thakur, Aditya},
  booktitle={37th IEEE/ACM International Conference on Automated Software Engineering (ASE)},
  pages={1--4},
  year={2022}
}
```

Also, refer to our extended version of this work here:
```tex
@article{alomar2023just,
  title={Just-in-time code duplicates extraction},
  author={AlOmar, Eman Abdullah and Ivanov, Anton and Kurbatova, Zarina and Golubev, Yaroslav and Mkaouer, Mohamed Wiem and Ouni, Ali and Bryksin, Timofey and Nguyen, Le and Kini, Amit and Thakur, Aditya},
  journal={Information and Software Technology},
  volume={158},
  pages={107169},
  year={2023},
  publisher={Elsevier}
}
```

