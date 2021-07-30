# Contributing

- [Contributing](#contributing)
    - [How to contribute](#how-to-contribute)
    - [Getting code](#getting-code)
    - [Code reviews](#code-reviews)
    - [Code style](#code-style)

## How to contribute

First of all, thank you for your interest in Daedalus!
When contributing to this repository, please first discuss the change you wish to make via issue, email, or any other method before making a change.

Please note we have a code of conduct, follow it in all your interactions with the project.

## Getting code

1 - Clone this repository

```bash
git clone https://github.com/arturfigueira/daedalus.git
cd daedalus
```

2 - Build and run all tests

```bash
./gradlew build
```



## Code reviews

All submissions, including submissions by project members, require review. We
use GitHub pull requests for this purpose. Consult
[GitHub Help](https://help.github.com/articles/about-pull-requests/) for more
information on using pull requests.

Also stick to these basic rules:

- One change per PR: Pull-requests should be small and easy to read. Do not mix different subjects, fixes, features;
- Add unit tests to cover your changes;
- Write short, detailed commit messages. Write your summary in present tense, limit the subject line to 50 characters, and always leave the second line blank;
- Don’t forget about backward compatibility and Javadoc for public methods.
- To save your time, run tests locally and guarantee that it builds;
- Remove unnecessary files; [`.gitignore`](.gitignore) is pretty complete, and will cover almost any unnecessary file type, but it's not bulletproof.

## Tests
We use [Spock Framework](https://spockframework.org/) for them. Please write tests using the built-in Given-When-Then blocks. Keep Act (When) section as small as possible.

## Code style
We use [Google's java style guide](https://google.github.io/styleguide/javaguide.html) formatting. It`s strongly recommended configuring your IDE to automatically format and/or check your coding style.

## Licence
By contributing, you agree that your contributions will be licensed under its [MIT License](https://github.com/arturfigueira/daedalus/blob/main/LICENSE). 