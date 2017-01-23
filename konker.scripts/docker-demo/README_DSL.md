# konker-platform

## domain specific language

A [domain specific language](https://en.wikipedia.org/wiki/Domain-specific_language) helps you to manage some features in a system. So, we provide the following commands to manage users and database:

```
usage: konker <command> [<args>]

The commands available are:
    user     User management
    database Database management
    

Konker Platform Management

positional arguments:
  command     Subcommand to run

optional arguments:
  -h, --help  show this help message and exit
```


**User Management**


If you do not have a database containing Konker users, we provide one as default:

User: admin

Password: changeme


###### Creating an user

Besides, you can create more users according to example below (without quotes, the org parameter is optional):


```konker user create "username" "password" --org "organization name" ```
###### Updating an user password

If necessary, you can update an user password, using the following command (without quotes):

```konker user update "username" "password"```

**Database Management**

May you need to update the database version, in this case, just use the following command (version parameter is optional, we upgrade to the latest version):
###### Updating database version
```konker database upgrade --version 0.1```
