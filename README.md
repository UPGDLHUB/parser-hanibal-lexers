[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/qKNbimKm)
# TheParser

A Java-based parser that analyzes token sequences and constructs a syntax tree based on a context-free grammar (CFG).

## Project Structure

- `src/main/java`: Contains the Java source code.
- `src/main/resources`: Contains a number of test cases
- `pom.xml`: Maven configuration file.

## Prerequisites

- Java 21
- Maven

## Building the Project

To build the project, run the following command:

```sh
mvn clean install
```

| +           | int    | float  | boolean | char   | string | binary | octal  | hexadecimal | void |
| ----------- | ------ | ------ | ------- | ------ | ------ | ------ | ------ | ----------- | ---- |
| int         | int    | float  | x       | x      | string | int    | int    | int         | x    |
| float       | float  | float  | x       | x      | string | float  | float  | float       | x    |
| boolean     | x      | x      | x       | x      | string | x      | x      | x           | x    |
| char        | x      | x      | x       | x      | string | x      | x      | x           | x    |
| string      | string | string | string  | string | string | string | string | string      | x    |
| binary      | int    | float  | x       | x      | string | int    | int    | int         | x    |
| octal       | int    | float  | x       | x      | string | int    | int    | int         | x    |
| hexadecimal | int    | float  | x       | x      | string | int    | int    | int         | x    |
| void        | x      | x      | x       | x      | x      | x      | x      | x           | x    |

| -           | int   | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ----- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | int   | float | x       | x    | x      | int    | int   | int         | x    |
| float       | float | float | x       | x    | x      | float  | float | float       | x    |
| boolean     | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | int   | float | x       | x    | x      | int    | int   | int         | x    |
| octal       | int   | float | x       | x    | x      | int    | int   | int         | x    |
| hexadecimal | int   | float | x       | x    | x      | int    | int   | int         | x    |
| void        | x     | x     | x       | x    | x      | x      | x     | x           | x    |

| *           | int   | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ----- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | int   | float | x       | x    | x      | int    | int   | int         | x    |
| float       | float | float | x       | x    | x      | float  | float | float       | x    |
| boolean     | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | int   | float | x       | x    | x      | int    | int   | int         | x    |
| octal       | int   | float | x       | x    | x      | int    | int   | int         | x    |
| hexadecimal | int   | float | x       | x    | x      | int    | int   | int         | x    |
| void        | x     | x     | x       | x    | x      | x      | x     | x           | x    |

| /           | int   | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ----- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | float | float | x       | x    | x      | float  | float | float       | x    |
| float       | float | float | x       | x    | x      | float  | float | float       | x    |
| boolean     | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x     | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | float | float | x       | x    | x      | float  | float | float       | x    |
| octal       | float | float | x       | x    | x      | float  | float | float       | x    |
| hexadecimal | float | float | x       | x    | x      | float  | float | float       | x    |
| void        | x     | x     | x       | x    | x      | x      | x     | x           | x    |

| %           | int | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | --- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | int | x     | x       | x    | x      | int    | int   | int         | x    |
| float       | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| boolean     | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | int | x     | x       | x    | x      | int    | int   | int         | x    |
| octal       | int | x     | x       | x    | x      | int    | int   | int         | x    |
| hexadecimal | int | x     | x       | x    | x      | int    | int   | int         | x    |
| void        | x   | x     | x       | x    | x      | x      | x     | x           | x    |

| =           | int | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | --- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | OK  | OK    | x       | x    | x      | OK     | OK    | OK          | x    |
| float       | OK  | OK    | x       | x    | x      | OK     | OK    | OK          | x    |
| boolean     | x   | x     | OK      | x    | x      | x      | x     | x           | x    |
| char        | x   | x     | x       | OK   | x      | x      | x     | x           | x    |
| string      | x   | x     | x       | x    | OK     | x      | x     | x           | x    |
| binary      | OK  | x     | x       | x    | x      | OK     | OK    | OK          | x    |
| octal       | OK  | x     | x       | x    | x      | OK     | OK    | OK          | x    |
| hexadecimal | OK  | x     | x       | x    | x      | OK     | OK    | OK          | x    |
| void        | x   | x     | x       | x    | x      | x      | x     | x           | x    |

| &&          | int | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | --- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| float       | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| boolean     | x   | x     | boolean | x    | x      | x      | x     | x           | x    |
| char        | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| octal       | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| hexadecimal | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| void        | x   | x     | x       | x    | x      | x      | x     | x           | x    |

| Or operator | int | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | --- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| float       | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| boolean     | x   | x     | boolean | x    | x      | x      | x     | x           | x    |
| char        | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| octal       | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| hexadecimal | x   | x     | x       | x    | x      | x      | x     | x           | x    |
| void        | x   | x     | x       | x    | x      | x      | x     | x           | x    |

| <           | int  | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ---- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| float       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| boolean     | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| octal       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| hexadecimal | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| void        | x    | x     | x       | x    | x      | x      | x     | x           | x    |

| >           | int  | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ---- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| float       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| boolean     | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| octal       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| hexadecimal | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| void        | x    | x     | x       | x    | x      | x      | x     | x           | x    |

| <=          | int  | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ---- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| float       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| boolean     | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| octal       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| hexadecimal | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| void        | x    | x     | x       | x    | x      | x      | x     | x           | x    |

| >=          | int  | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ---- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| float       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| boolean     | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| char        | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| string      | x    | x     | x       | x    | x      | x      | x     | x           | x    |
| binary      | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| octal       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| hexadecimal | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| void        | x    | x     | x       | x    | x      | x      | x     | x           | x    |

| ==          | int  | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ---- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| float       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| boolean     | x    | x     | bool    | x    | x      | x      | x     | x           | x    |
| char        | x    | x     | x       | bool | x      | x      | x     | x           | x    |
| string      | x    | x     | x       | x    | bool   | x      | x     | x           | x    |
| binary      | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| octal       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| hexadecimal | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| void        | x    | x     | x       | x    | x      | x      | x     | x           | x    |

| !=          | int  | float | boolean | char | string | binary | octal | hexadecimal | void |
| ----------- | ---- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| int         | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| float       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| boolean     | x    | x     | bool    | x    | x      | x      | x     | x           | x    |
| char        | x    | x     | x       | bool | x      | x      | x     | x           | x    |
| string      | x    | x     | x       | x    | bool   | x      | x     | x           | x    |
| binary      | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| octal       | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| hexadecimal | bool | bool  | x       | x    | x      | bool   | bool  | bool        | x    |
| void        | x    | x     | x       | x    | x      | x      | x     | x           | x    |

| Operator | int | float | boolean | char | string | binary | octal | hexadecimal | void |
| -------- | --- | ----- | ------- | ---- | ------ | ------ | ----- | ----------- | ---- |
| !        | x   | x     | boolean | x    | x      | x      | x     | x           | x    |
