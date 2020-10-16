# solc-sjw
### solc standard json writer

CLI tool to help you build a `solc --standard-json` compatible file for verifying deployed contracts if your only alternative is opinionated js-specific frameworks heavily integrated into their own ecosystems

### Usage
`solc-sjw --dir [base-directory] [--optimized [--runs num]]`

where
* `--dir` `-d` *(required)* is the base directory
* `--no-optimization` `-no-opt` *(optional)* flag for whether to exclude optimization in the output
* `--runs` `-r` *(optional)* number of optimization runs, only used if optimization is enabled, by default 200
* `--test-ext` `-t` *(optional)* file extension for testing solidity files, by default `.t.sol`
