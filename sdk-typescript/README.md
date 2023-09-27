# SDK Typescript
The TypeScript compiled proto files where generated with [ts-proto](https://github.com/stephenh/ts-proto) library.

# Git Sub modules
Given that this project need the LittleHorse TypeScript compiled proto buffs, we have added the LittleHorse repo as a submodule.
The first time you clone the project the littlehorse directory will be empty, in order to fix that please run:
```
git submodule init
git submodule update
```
More information about git submodules [can be found here.](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
