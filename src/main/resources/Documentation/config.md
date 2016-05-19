Tag Commit Validator Configuration
===================================

The regexp template should be placed in project config file `project.config`.

File example:

```
[plugin "tag-commit-validator"]
  regexp = "^\\[.*\\].*"
  errorMessage = "Commit message should start from tags: '[]'"
```

tag-commit-validator.regexp
:	Regexp template for check commit message
	not specified, defaults to "".

tag-commit-validator.errorMessage
:	Error message when 'match' is fails
	not specified, defaults to "".
