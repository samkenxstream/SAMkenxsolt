# solc-sjw
### solc standard json writer

### Install

#### Linux
```bash
wget https://github.com/hjubb/solc-sjw/releases/download/v0.3.0/solc-sjw-linux-x64 -O ~/.local/bin/solc-sjw
chmod +x ~/.local/bin/solc-sjw
```

### Usage
`solc-sjw --dir [base-directory] [--no-optimization] [--runs num] [--ignore-ext extension to ignore] [--truffle]`

where
* `--dir` `-d` *(required)* is the base directory
* `--no-optimization` `-no-opt` *(optional)* flag for whether to exclude optimization in the output
* `--runs` `-r` *(optional)* number of optimization runs, only used when optimizing, by default 200
* `--ignore-ext` `-i` *(optional)* ignore files with the file extension, by default `.t.sol`
* `--truffle` *(optional)* attempts to resolve npm installed dependencies referenced by your contracts


### Context

CLI tool to help you generate a `solc --standard-json` compatible file to **deterministically** verify deployed contracts on [etherscan](https://etherscan.io).

### Why does this exist?

Flattening your solidity code before verifying them on etherscan [is not recommended](https://twitter.com/ethchris/status/1296121526601875456). Yet it seems to be the most common approach for verification on etherscan.

While this is fine for small projects, you run into weird edge cases where flattening your code will [yield different bytecodes from the deployed ones](https://github.com/UMAprotocol/protocol/issues/1807) as the complexity of your code grows. This could be due to the usage of experimental features such as `ABIEncoderV2` in your source code, and/or the usage of abosolute file paths when compiling your contracts (see [here](https://github.com/kendricktan/etherscan-verification-horrors)).

One approach that has worked consistently was using the [compiler standard-json](https://solidity.readthedocs.io/en/v0.6.12/using-the-compiler.html#compiler-input-and-output-json-description) input method, which is also [used by buidler](https://github.com/nomiclabs/buidler/pull/416) behind the scenes.

`solc-sjw` was built as we wanted a static-binary that could:

1. Generate a `standard-json` file for `solc`
2. Be configured without any `.config.js` file

### How can I verify my contracts?

Simply download the binary, make it executable and run:

```bash
solc-sjw --dir <contracts-dir>
```

A new file called `solc-input.json` should appear. You can upload this file to [etherscan](https://etherscan.io) and verify it via the `standard json-input` method.
